package com.medicollab.record.service;

import com.medicollab.record.entity.ExamReport;
import com.medicollab.record.entity.MedicalRecord;

import java.util.List;

public interface RecordService {

    // 病历
    MedicalRecord createRecord(MedicalRecord record);
    MedicalRecord updateRecord(MedicalRecord record);
    void deleteRecord(Long id);
    MedicalRecord getRecord(Long id);
    List<MedicalRecord> listRecords(int page, int size, Long patientId, Long doctorId);
    long countRecords(Long patientId, Long doctorId);

    // 检查报告
    ExamReport createReport(ExamReport report);
    ExamReport updateReport(ExamReport report);
    void deleteReport(Long id);
    ExamReport getReport(Long id);
    List<ExamReport> listReports(int page, int size, Long patientId, String examType);
    long countReports(Long patientId, String examType);

    // 跨机构分享
    String shareReport(Long reportId, String targetHospitalCode);

    // 审计链校验
    boolean verifyAuditChain(Long patientId);
}
