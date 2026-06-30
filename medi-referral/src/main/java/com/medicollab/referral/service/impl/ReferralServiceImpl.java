package com.medicollab.referral.service.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicollab.common.exception.BusinessException;
import com.medicollab.referral.entity.Referral;
import com.medicollab.referral.mapper.ReferralMapper;
import com.medicollab.referral.service.ReferralService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReferralServiceImpl implements ReferralService {

    private static final Logger log = LoggerFactory.getLogger(ReferralServiceImpl.class);

    private final ReferralMapper referralMapper;
    private final RabbitTemplate rabbitTemplate;

    public ReferralServiceImpl(ReferralMapper referralMapper, RabbitTemplate rabbitTemplate) {
        this.referralMapper = referralMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    private static final String EXCHANGE = "medicollab.referral";
    private static final String ROUTING_KEY = "referral.notify";

    @Override
    @Transactional
    public Referral createReferral(Referral referral) {
        referral.setStatus("PENDING");
        referralMapper.insert(referral);
        // 发送 MQ 通知目标医院
        sendNotification(referral, "NEW_REFERRAL");
        log.info("转诊申请已提交: {}, MQ通知已发送", referral.getId());
        return referral;
    }

    @Override
    @Transactional
    public Referral approveReferral(Long id, Long doctorId, String doctorName) {
        Referral ref = referralMapper.selectById(id);
        if (ref == null) throw new BusinessException("转诊记录不存在");
        if (!"PENDING".equals(ref.getStatus())) throw new BusinessException("转诊状态不允许审批");
        ref.setStatus("APPROVED");
        ref.setToDoctorId(doctorId);
        ref.setToDoctorName(doctorName);
        referralMapper.updateById(ref);
        sendNotification(ref, "REFERRAL_APPROVED");
        return ref;
    }

    @Override
    @Transactional
    public Referral rejectReferral(Long id, String reason) {
        Referral ref = referralMapper.selectById(id);
        if (ref == null) throw new BusinessException("转诊记录不存在");
        ref.setStatus("REJECTED");
        ref.setRejectReason(reason);
        referralMapper.updateById(ref);
        sendNotification(ref, "REFERRAL_REJECTED");
        return ref;
    }

    @Override
    @Transactional
    public Referral completeReferral(Long id, String feedback) {
        Referral ref = referralMapper.selectById(id);
        if (ref == null) throw new BusinessException("转诊记录不存在");
        ref.setStatus("COMPLETED");
        ref.setFeedback(feedback);
        referralMapper.updateById(ref);
        sendNotification(ref, "REFERRAL_COMPLETED");
        return ref;
    }

    @Override
    @Transactional
    public Referral cancelReferral(Long id) {
        Referral ref = referralMapper.selectById(id);
        if (ref == null) throw new BusinessException("转诊记录不存在");
        if (!"PENDING".equals(ref.getStatus())) throw new BusinessException("只有待审批的转诊可以撤回");
        ref.setStatus("CANCELLED"); // 使用逻辑删除或自定义状态
        referralMapper.updateById(ref);
        return ref;
    }

    @Override
    public Referral getReferral(Long id) {
        return referralMapper.selectById(id);
    }

    @Override
    public List<Referral> listReferrals(int page, int size, Long patientId, String status,
                                         Long fromHospitalId, Long toHospitalId) {
        LambdaQueryWrapper<Referral> w = new LambdaQueryWrapper<>();
        if (patientId != null) w.eq(Referral::getPatientId, patientId);
        if (status != null) w.eq(Referral::getStatus, status);
        if (fromHospitalId != null) w.eq(Referral::getFromHospitalId, fromHospitalId);
        if (toHospitalId != null) w.eq(Referral::getToHospitalId, toHospitalId);
        w.orderByDesc(Referral::getCreateTime);
        return referralMapper.selectPage(new Page<>(page, size), w).getRecords();
    }

    @Override
    public long countReferrals(Long patientId, String status, Long fromHospitalId, Long toHospitalId) {
        LambdaQueryWrapper<Referral> w = new LambdaQueryWrapper<>();
        if (patientId != null) w.eq(Referral::getPatientId, patientId);
        if (status != null) w.eq(Referral::getStatus, status);
        if (fromHospitalId != null) w.eq(Referral::getFromHospitalId, fromHospitalId);
        if (toHospitalId != null) w.eq(Referral::getToHospitalId, toHospitalId);
        return referralMapper.selectCount(w);
    }

    private void sendNotification(Referral ref, String eventType) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("referralId", ref.getId());
            msg.put("patientName", ref.getPatientName());
            msg.put("fromHospital", ref.getFromHospitalName());
            msg.put("toHospital", ref.getToHospitalName());
            msg.put("status", ref.getStatus());
            msg.put("eventType", eventType);
            msg.put("timestamp", LocalDateTime.now().toString());
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, JSONUtil.toJsonStr(msg));
        } catch (Exception e) {
            log.error("MQ消息发送失败: {}", e.getMessage());
        }
    }
}
