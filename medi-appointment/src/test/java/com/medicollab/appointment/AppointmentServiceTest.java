package com.medicollab.appointment;

import com.medicollab.appointment.entity.Appointment;
import com.medicollab.appointment.entity.Department;
import com.medicollab.appointment.entity.DoctorSchedule;
import com.medicollab.appointment.mapper.AppointmentMapper;
import com.medicollab.appointment.mapper.DepartmentMapper;
import com.medicollab.appointment.mapper.ScheduleMapper;
import com.medicollab.appointment.service.AppointmentService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 预约服务集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    private static Long deptId;
    private static Long scheduleId;
    private static Long appointmentId;

    @Test
    @Order(1)
    @DisplayName("1. 创建科室")
    void testCreateDept() {
        Department dept = new Department();
        dept.setName("心内科(测试)");
        dept.setDescription("心血管疾病专科");
        dept.setHospitalId(1L);
        dept.setHospitalName("第一人民医院");
        dept.setLocation("门诊3层A区");
        dept.setMaxDailyAppointments(50);
        Department saved = appointmentService.createDept(dept);
        assertNotNull(saved.getId());
        deptId = saved.getId();
        System.out.println("✅ 科室创建成功: ID=" + deptId);
    }

    @Test
    @Order(2)
    @DisplayName("2. 创建医生排班")
    void testCreateSchedule() {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(2L);
        schedule.setDoctorName("张医生");
        schedule.setDepartmentId(deptId);
        schedule.setDepartmentName("心内科(测试)");
        schedule.setHospitalId(1L);
        schedule.setHospitalName("第一人民医院");
        schedule.setWorkDate(LocalDate.now());
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(12, 0));
        schedule.setMaxPatients(30);
        DoctorSchedule saved = appointmentService.createSchedule(schedule);
        assertNotNull(saved.getId());
        scheduleId = saved.getId();
        System.out.println("✅ 排班创建成功: ID=" + scheduleId);
    }

    @Test
    @Order(3)
    @DisplayName("3. 创建预约 (含分布式锁)")
    void testCreateAppointment() {
        Appointment apt = new Appointment();
        apt.setPatientId(5L);
        apt.setPatientName("赵患者");
        apt.setDoctorId(2L);
        apt.setDoctorName("张医生");
        apt.setDepartmentId(deptId);
        apt.setDepartmentName("心内科(测试)");
        apt.setHospitalId(1L);
        apt.setHospitalName("第一人民医院");
        apt.setScheduleId(scheduleId);
        apt.setAppointmentDate(LocalDate.now());
        apt.setTimeSlot(LocalTime.of(9, 0));
        Appointment saved = appointmentService.createAppointment(apt);
        assertNotNull(saved.getId());
        assertEquals("BOOKED", saved.getStatus());
        assertTrue(saved.getQueueNumber() > 0);
        appointmentId = saved.getId();
        System.out.println("✅ 预约创建成功: ID=" + appointmentId + ", 号序=" + saved.getQueueNumber());
    }

    @Test
    @Order(4)
    @DisplayName("4. 查询预约详情")
    void testGetAppointment() {
        Appointment apt = appointmentService.getAppointment(appointmentId);
        assertNotNull(apt);
        assertEquals("赵患者", apt.getPatientName());
        System.out.println("✅ 预约查询成功");
    }

    @Test
    @Order(5)
    @DisplayName("5. 取消预约")
    void testCancelAppointment() {
        Appointment apt = appointmentService.cancelAppointment(appointmentId);
        assertEquals("CANCELLED", apt.getStatus());
        System.out.println("✅ 预约已取消");
    }

    @Test
    @Order(6)
    @DisplayName("6. Redis 科室热度排行")
    void testDeptHotRank() {
        // 模拟热度计数
        appointmentService.incrementDeptHot(deptId);
        appointmentService.incrementDeptHot(deptId);
        appointmentService.incrementDeptHot(deptId + 1);
        List<Map<String, Object>> rank = appointmentService.getDeptHotRank(10);
        assertNotNull(rank);
        System.out.println("✅ 热度排行: " + rank);
    }

    @Test
    @Order(7)
    @DisplayName("7. Redis 分布式锁测试")
    void testDistributedLock() {
        boolean locked = appointmentService.lockSlot(999L);
        assertTrue(locked, "首次加锁应成功");
        boolean lockedAgain = appointmentService.lockSlot(999L);
        assertFalse(lockedAgain, "重复加锁应失败");
        appointmentService.unlockSlot(999L);
        System.out.println("✅ 分布式锁测试通过");
    }

    @Test
    @Order(8)
    @DisplayName("8. 科室列表查询")
    void testListDepts() {
        List<Department> list = appointmentService.listDepts("心内");
        assertFalse(list.isEmpty());
        System.out.println("✅ 科室搜索成功: 匹配=" + list.size());
    }

    @Test
    @Order(9)
    @DisplayName("9. 排班分页查询")
    void testListSchedules() {
        List<DoctorSchedule> list = appointmentService.listSchedules(1, 10, deptId, null);
        assertFalse(list.isEmpty());
        System.out.println("✅ 排班查询成功");
    }
}
