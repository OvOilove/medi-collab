package com.medicollab.record;

import com.medicollab.common.R;
import com.medicollab.record.controller.RecordController;
import com.medicollab.record.entity.ExamReport;
import com.medicollab.record.entity.MedicalRecord;
import com.medicollab.record.service.RecordService;
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
class RecordMockTest {

    @Mock private RecordService recordService;
    @InjectMocks private RecordController controller;

    @Test @DisplayName("1. 创建病历")
    void testCreateRecord() {
        MedicalRecord r = new MedicalRecord();
        r.setId(1L); r.setDiagnosis("冠心病");
        r.setAuditHash("abc123def456");
        when(recordService.createRecord(any())).thenReturn(r);
        R<MedicalRecord> result = controller.create(new MedicalRecord());
        assertEquals(200, result.getCode());
        assertNotNull(result.getData().getAuditHash());
        System.out.println("✅ 病历创建: 审计哈希=" + result.getData().getAuditHash().substring(0, 8) + "...");
    }

    @Test @DisplayName("2. 创建检查报告 (哈希链)")
    void testCreateReport() {
        ExamReport rpt = new ExamReport();
        rpt.setId(1L); rpt.setExamType("CT");
        rpt.setPrevHash("GENESIS");
        rpt.setCurrentHash("hash_chain_001");
        when(recordService.createReport(any())).thenReturn(rpt);
        R<ExamReport> result = controller.createReport(new ExamReport());
        assertEquals(200, result.getCode());
        assertEquals("GENESIS", result.getData().getPrevHash());
        System.out.println("✅ 报告创建: prevHash=GENESIS, currentHash=" + result.getData().getCurrentHash());
    }

    @Test @DisplayName("3. 审计链校验")
    void testVerifyAuditChain() {
        when(recordService.verifyAuditChain(5L)).thenReturn(true);
        R<Boolean> result = controller.verifyAuditChain(5L);
        assertTrue(result.getData());
        System.out.println("✅ 审计链校验通过");
    }

    @Test @DisplayName("4. 跨机构分享报告")
    void testShareReport() {
        when(recordService.shareReport(1L, "HOSP_002")).thenReturn("tok_abc123");
        R<String> result = controller.shareReport(1L, "HOSP_002");
        assertTrue(result.getData().startsWith("tok_"));
        System.out.println("✅ 报告分享: 令牌=" + result.getData());
    }
}
