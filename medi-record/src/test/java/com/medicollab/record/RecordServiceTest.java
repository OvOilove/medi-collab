package com.medicollab.record;

import com.medicollab.record.entity.ExamReport;
import com.medicollab.record.entity.MedicalRecord;
import com.medicollab.record.service.RecordService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 病历服务集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecordServiceTest {

    @Autowired
    private RecordService recordService;

    private static Long recordId;
    private static Long reportId1;
    private static Long reportId2;
    private static final Long PATIENT_ID = 5L;

    @Test
    @Order(1)
    @DisplayName("1. 创建电子病历")
    void testCreateRecord() {
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(PATIENT_ID);
        record.setPatientName("赵患者");
        record.setDoctorId(2L);
        record.setDoctorName("张医生");
        record.setDepartmentId(1L);
        record.setDepartmentName("心内科");
        record.setHospitalId(1L);
        record.setHospitalName("第一人民医院");
        record.setChiefComplaint("胸闷气短3天");
        record.setDiagnosis("冠心病 不稳定型心绞痛");
        record.setTreatmentPlan("住院治疗，行冠脉造影检查");
        MedicalRecord saved = recordService.createRecord(record);
        assertNotNull(saved.getId());
        assertNotNull(saved.getAuditHash());
        assertEquals(64, saved.getAuditHash().length());
        recordId = saved.getId();
        System.out.println("✅ 病历创建成功: ID=" + recordId + ", 审计哈希=" + saved.getAuditHash().substring(0, 16) + "...");
    }

    @Test
    @Order(2)
    @DisplayName("2. 创建检查报告（哈希链第1条）")
    void testCreateReport() {
        ExamReport report = new ExamReport();
        report.setPatientId(PATIENT_ID);
        report.setPatientName("赵患者");
        report.setDoctorId(2L);
        report.setDoctorName("张医生");
        report.setExamType("CT");
        report.setExamPart("胸部");
        report.setFindings("双肺纹理增多，未见明显实质性病变");
        report.setConclusion("心肺未见明显异常");
        report.setRecordId(recordId);
        ExamReport saved = recordService.createReport(report);
        assertNotNull(saved.getId());
        assertEquals("GENESIS", saved.getPrevHash());
        assertNotNull(saved.getCurrentHash());
        reportId1 = saved.getId();
        System.out.println("✅ 报告1创建: prevHash=GENESIS, currentHash=" + saved.getCurrentHash().substring(0, 16) + "...");
    }

    @Test
    @Order(3)
    @DisplayName("3. 创建检查报告（哈希链第2条，链接到报告1）")
    void testCreateReportChain() {
        ExamReport report = new ExamReport();
        report.setPatientId(PATIENT_ID);
        report.setPatientName("赵患者");
        report.setDoctorId(3L);
        report.setDoctorName("李医生");
        report.setExamType("血常规");
        report.setExamPart("血液");
        report.setFindings("白细胞正常，红细胞略低");
        report.setConclusion("轻度贫血");
        report.setRecordId(recordId);
        ExamReport saved = recordService.createReport(report);
        assertNotNull(saved.getId());
        // 验证哈希链：第2条报告的prevHash应等于第1条的currentHash
        ExamReport first = recordService.getReport(reportId1);
        assertEquals(first.getCurrentHash(), saved.getPrevHash());
        reportId2 = saved.getId();
        System.out.println("✅ 报告2创建: prevHash=" + saved.getPrevHash().substring(0, 16) + " (链到报告1)");
    }

    @Test
    @Order(4)
    @DisplayName("4. 审计链完整性校验")
    void testVerifyAuditChain() {
        boolean valid = recordService.verifyAuditChain(PATIENT_ID);
        assertTrue(valid);
        System.out.println("✅ 审计链校验通过 - 数据完整未被篡改");
    }

    @Test
    @Order(5)
    @DisplayName("5. 跨机构分享报告")
    void testShareReport() {
        String token = recordService.shareReport(reportId1, "HOSPITAL_002");
        assertNotNull(token);
        assertEquals(32, token.length());
        System.out.println("✅ 报告分享成功: 令牌=" + token);
    }

    @Test
    @Order(6)
    @DisplayName("6. 病历分页查询")
    void testListRecords() {
        List<MedicalRecord> list = recordService.listRecords(1, 10, PATIENT_ID, null);
        assertFalse(list.isEmpty());
        System.out.println("✅ 病历查询: 共" + recordService.countRecords(PATIENT_ID, null) + "条");
    }

    @Test
    @Order(7)
    @DisplayName("7. 报告分页查询")
    void testListReports() {
        List<ExamReport> list = recordService.listReports(1, 10, PATIENT_ID, null);
        assertEquals(2, list.size());
        System.out.println("✅ 报告查询: 共" + list.size() + "条");
    }
}
