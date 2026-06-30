package com.medicollab.referral;

import com.medicollab.common.R;
import com.medicollab.referral.controller.ReferralController;
import com.medicollab.referral.entity.Referral;
import com.medicollab.referral.service.ReferralService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferralMockTest {

    @Mock private ReferralService referralService;
    @InjectMocks private ReferralController controller;

    @Test @DisplayName("1. 提交转诊申请")
    void testCreateReferral() {
        Referral ref = new Referral();
        ref.setId(1L); ref.setPatientName("赵患者"); ref.setStatus("PENDING");
        when(referralService.createReferral(any())).thenReturn(ref);
        R<Referral> r = controller.create(new Referral());
        assertEquals(200, r.getCode());
        assertEquals("PENDING", r.getData().getStatus());
        System.out.println("✅ 转诊申请: 状态=" + r.getData().getStatus());
    }

    @Test @DisplayName("2. 审批通过转诊")
    void testApproveReferral() {
        Referral ref = new Referral();
        ref.setId(1L); ref.setStatus("APPROVED"); ref.setToDoctorName("张医生");
        when(referralService.approveReferral(1L, 2L, "张医生")).thenReturn(ref);
        R<Referral> r = controller.approve(1L, 2L, "张医生");
        assertEquals("APPROVED", r.getData().getStatus());
        System.out.println("✅ 转诊已批准: 接收医生=" + r.getData().getToDoctorName());
    }

    @Test @DisplayName("3. 拒绝转诊")
    void testRejectReferral() {
        Referral ref = new Referral();
        ref.setId(1L); ref.setStatus("REJECTED"); ref.setRejectReason("床位已满");
        when(referralService.rejectReferral(1L, "床位已满")).thenReturn(ref);
        R<Referral> r = controller.reject(1L, "床位已满");
        assertEquals("REJECTED", r.getData().getStatus());
        System.out.println("✅ 转诊已拒绝: 原因=" + r.getData().getRejectReason());
    }

    @Test @DisplayName("4. 完成转诊 (回转反馈)")
    void testCompleteReferral() {
        Referral ref = new Referral();
        ref.setId(1L); ref.setStatus("COMPLETED"); ref.setFeedback("病情稳定");
        when(referralService.completeReferral(1L, "病情稳定")).thenReturn(ref);
        R<Referral> r = controller.complete(1L, "病情稳定");
        assertEquals("COMPLETED", r.getData().getStatus());
        System.out.println("✅ 转诊已完成: 反馈=" + r.getData().getFeedback());
    }
}
