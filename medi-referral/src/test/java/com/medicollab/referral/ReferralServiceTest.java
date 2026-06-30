package com.medicollab.referral;

import com.medicollab.referral.entity.Referral;
import com.medicollab.referral.service.ReferralService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 转诊服务集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReferralServiceTest {

    @Autowired
    private ReferralService referralService;

    private static Long referralId;

    @Test
    @Order(1)
    @DisplayName("1. 提交转诊申请")
    void testCreateReferral() {
        Referral ref = new Referral();
        ref.setPatientId(5L);
        ref.setPatientName("赵患者");
        ref.setFromDoctorId(4L);
        ref.setFromDoctorName("王医生");
        ref.setFromDeptId(4L);
        ref.setFromDeptName("全科");
        ref.setFromHospitalId(3L);
        ref.setFromHospitalName("社区医院");
        ref.setToDeptId(1L);
        ref.setToDeptName("心内科");
        ref.setToHospitalId(1L);
        ref.setToHospitalName("第一人民医院");
        ref.setReferralReason("患者胸闷加重，社区医院条件有限，建议转上级医院进一步诊治");
        ref.setDiagnosis("冠心病可疑");
        Referral saved = referralService.createReferral(ref);
        assertNotNull(saved.getId());
        assertEquals("PENDING", saved.getStatus());
        referralId = saved.getId();
        System.out.println("✅ 转诊申请提交成功: ID=" + referralId);
    }

    @Test
    @Order(2)
    @DisplayName("2. 查询转诊详情")
    void testGetReferral() {
        Referral ref = referralService.getReferral(referralId);
        assertNotNull(ref);
        assertEquals("赵患者", ref.getPatientName());
        System.out.println("✅ 转诊查询成功: 状态=" + ref.getStatus());
    }

    @Test
    @Order(3)
    @DisplayName("3. 审批通过转诊")
    void testApproveReferral() {
        Referral ref = referralService.approveReferral(referralId, 2L, "张医生");
        assertEquals("APPROVED", ref.getStatus());
        assertEquals("张医生", ref.getToDoctorName());
        System.out.println("✅ 转诊已批准: 接收医生=" + ref.getToDoctorName());
    }

    @Test
    @Order(4)
    @DisplayName("4. 完成转诊（回转反馈）")
    void testCompleteReferral() {
        Referral ref = referralService.completeReferral(referralId, "患者已完成冠脉造影，确诊为冠心病，治疗后病情稳定，建议回社区医院随访");
        assertEquals("COMPLETED", ref.getStatus());
        assertNotNull(ref.getFeedback());
        System.out.println("✅ 转诊已完成: 反馈=" + ref.getFeedback().substring(0, 20) + "...");
    }

    @Test
    @Order(5)
    @DisplayName("5. 转诊列表查询")
    void testListReferrals() {
        var list = referralService.listReferrals(1, 10, 5L, null, null, null);
        assertFalse(list.isEmpty());
        System.out.println("✅ 转诊列表查询成功: " + list.size() + "条");
    }
}
