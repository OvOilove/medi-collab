package com.medicollab.appointment.service.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicollab.appointment.entity.Appointment;
import com.medicollab.appointment.entity.Department;
import com.medicollab.appointment.entity.DoctorSchedule;
import com.medicollab.appointment.feign.UserFeignClient;
import com.medicollab.appointment.mapper.AppointmentMapper;
import com.medicollab.appointment.mapper.DepartmentMapper;
import com.medicollab.appointment.mapper.ScheduleMapper;
import com.medicollab.appointment.service.AppointmentService;
import com.medicollab.common.R;
import com.medicollab.common.dto.UserDTO;
import com.medicollab.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RefreshScope  // 支持 Nacos 配置热刷新
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentMapper appointmentMapper;
    private final DepartmentMapper departmentMapper;
    private final ScheduleMapper scheduleMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserFeignClient userFeignClient;

    @Value("${appointment.max-per-day:100}")
    private int maxPerDay;

    @Value("${appointment.lock-timeout:30}")
    private int lockTimeout;

    public AppointmentServiceImpl(AppointmentMapper appointmentMapper, DepartmentMapper departmentMapper,
                                  ScheduleMapper scheduleMapper, RedisTemplate<String, Object> redisTemplate,
                                  UserFeignClient userFeignClient) {
        this.appointmentMapper = appointmentMapper;
        this.departmentMapper = departmentMapper;
        this.scheduleMapper = scheduleMapper;
        this.redisTemplate = redisTemplate;
        this.userFeignClient = userFeignClient;
    }

    // ========== 预约 ==========

    @Override
    @Transactional
    public Appointment createAppointment(Appointment apt) {
        // 校验排班是否存在
        DoctorSchedule schedule = scheduleMapper.selectById(apt.getScheduleId());
        if (schedule == null || schedule.getStatus() == 0) {
            throw new BusinessException("该排班已停诊或不存在");
        }
        // 校验是否超过每日上限
        int todayCount = appointmentMapper.countByDeptAndDate(apt.getDepartmentId(), apt.getAppointmentDate());
        if (todayCount >= maxPerDay) {
            throw new BusinessException("该科室今日预约已满 (阈值: " + maxPerDay + "，可通过Nacos配置中心实时调整)");
        }
        // 分布式锁：锁定号源
        boolean locked = lockSlot(apt.getScheduleId());
        if (!locked) {
            throw new BusinessException("该号源正在被抢占，请稍后重试");
        }
        try {
            // 远程调用用户服务校验患者
            try {
                R<UserDTO> r = userFeignClient.getUserDTO(apt.getPatientId());
                if (r.getCode() != 200 || r.getData() == null) {
                    throw new BusinessException("患者信息不存在");
                }
            } catch (Exception e) {
                log.warn("Feign调用降级: {}", e.getMessage());
                // 降级处理：不阻塞业务流程
            }
            // 生成排队号
            int queueNum = appointmentMapper.countBookedBySchedule(apt.getDoctorId(), apt.getScheduleId()) + 1;
            apt.setQueueNumber(queueNum);
            apt.setStatus("BOOKED");
            appointmentMapper.insert(apt);
            // 更新排班已约人数
            schedule.setBookedCount(schedule.getBookedCount() + 1);
            scheduleMapper.updateById(schedule);
            // Redis 热度计数
            incrementDeptHot(apt.getDepartmentId());
            log.info("预约成功: 患者={}, 医生={}, 号序={}", apt.getPatientName(), apt.getDoctorName(), queueNum);
            return apt;
        } finally {
            unlockSlot(apt.getScheduleId());
        }
    }

    @Override
    public Appointment getAppointment(Long id) {
        return appointmentMapper.selectById(id);
    }

    @Override
    public List<Appointment> listAppointments(int page, int size, Long patientId, String status) {
        LambdaQueryWrapper<Appointment> w = new LambdaQueryWrapper<>();
        if (patientId != null) w.eq(Appointment::getPatientId, patientId);
        if (status != null) w.eq(Appointment::getStatus, status);
        w.orderByDesc(Appointment::getCreateTime);
        return appointmentMapper.selectPage(new Page<>(page, size), w).getRecords();
    }

    @Override
    public long countAppointments(Long patientId, String status) {
        LambdaQueryWrapper<Appointment> w = new LambdaQueryWrapper<>();
        if (patientId != null) w.eq(Appointment::getPatientId, patientId);
        if (status != null) w.eq(Appointment::getStatus, status);
        return appointmentMapper.selectCount(w);
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(Long id) {
        Appointment apt = appointmentMapper.selectById(id);
        if (apt == null) throw new BusinessException("预约不存在");
        if (!"BOOKED".equals(apt.getStatus())) throw new BusinessException("预约状态不允许取消");
        apt.setStatus("CANCELLED");
        appointmentMapper.updateById(apt);
        // 释放排班名额
        DoctorSchedule schedule = scheduleMapper.selectById(apt.getScheduleId());
        if (schedule != null && schedule.getBookedCount() > 0) {
            schedule.setBookedCount(schedule.getBookedCount() - 1);
            scheduleMapper.updateById(schedule);
        }
        return apt;
    }

    @Override
    @Transactional
    public Appointment completeAppointment(Long id) {
        Appointment apt = appointmentMapper.selectById(id);
        if (apt == null) throw new BusinessException("预约不存在");
        apt.setStatus("COMPLETED");
        appointmentMapper.updateById(apt);
        return apt;
    }

    // ========== 科室 ==========

    @Override
    public Department createDept(Department dept) {
        departmentMapper.insert(dept);
        return dept;
    }

    @Override
    public Department updateDept(Department dept) {
        departmentMapper.updateById(dept);
        return departmentMapper.selectById(dept.getId());
    }

    @Override
    public void deleteDept(Long id) { departmentMapper.deleteById(id); }

    @Override
    public Department getDept(Long id) { return departmentMapper.selectById(id); }

    @Override
    public List<Department> listDepts(String keyword) {
        LambdaQueryWrapper<Department> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) w.like(Department::getName, keyword);
        return departmentMapper.selectList(w);
    }

    // ========== 排班 ==========

    @Override
    public DoctorSchedule createSchedule(DoctorSchedule s) {
        s.setBookedCount(0);
        s.setStatus(1);
        scheduleMapper.insert(s);
        return s;
    }

    @Override
    public DoctorSchedule updateSchedule(DoctorSchedule s) {
        scheduleMapper.updateById(s);
        return scheduleMapper.selectById(s.getId());
    }

    @Override
    public void deleteSchedule(Long id) { scheduleMapper.deleteById(id); }

    @Override
    public List<DoctorSchedule> listSchedules(int page, int size, Long deptId, Long doctorId) {
        LambdaQueryWrapper<DoctorSchedule> w = new LambdaQueryWrapper<>();
        if (deptId != null) w.eq(DoctorSchedule::getDepartmentId, deptId);
        if (doctorId != null) w.eq(DoctorSchedule::getDoctorId, doctorId);
        w.orderByDesc(DoctorSchedule::getWorkDate);
        return scheduleMapper.selectPage(new Page<>(page, size), w).getRecords();
    }

    @Override
    public long countSchedules(Long deptId, Long doctorId) {
        LambdaQueryWrapper<DoctorSchedule> w = new LambdaQueryWrapper<>();
        if (deptId != null) w.eq(DoctorSchedule::getDepartmentId, deptId);
        if (doctorId != null) w.eq(DoctorSchedule::getDoctorId, doctorId);
        return scheduleMapper.selectCount(w);
    }

    // ========== Redis 热度排行 (ZSet) ==========

    @Override
    public void incrementDeptHot(Long deptId) {
        redisTemplate.opsForZSet().incrementScore("medi:dept:hot", String.valueOf(deptId), 1);
    }

    @Override
    public List<Map<String, Object>> getDeptHotRank(int topN) {
        Set<ZSetOperations.TypedTuple<Object>> set =
                redisTemplate.opsForZSet().reverseRangeWithScores("medi:dept:hot", 0, topN - 1);
        if (set == null) return Collections.emptyList();
        List<Map<String, Object>> rank = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : set) {
            Long deptId = Long.valueOf(tuple.getValue().toString());
            Department dept = departmentMapper.selectById(deptId);
            Map<String, Object> m = new HashMap<>();
            m.put("deptId", deptId);
            m.put("deptName", dept != null ? dept.getName() : "未知");
            m.put("score", tuple.getScore().longValue());
            rank.add(m);
        }
        return rank;
    }

    // ========== Redis 分布式锁 ==========

    @Override
    public boolean lockSlot(Long scheduleId) {
        String key = "medi:slot:lock:" + scheduleId;
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(lockTimeout));
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public void unlockSlot(Long scheduleId) {
        redisTemplate.delete("medi:slot:lock:" + scheduleId);
    }
}
