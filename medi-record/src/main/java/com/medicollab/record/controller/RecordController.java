package com.medicollab.record.controller;

import com.medicollab.common.R;
import com.medicollab.record.entity.ExamReport;
import com.medicollab.record.entity.MedicalRecord;
import com.medicollab.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "电子病历", description = "病历、检查报告、跨机构分享、审计链校验")
@RestController
@RequestMapping("/api/record")
public class RecordController {
    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }


    private final RecordService recordService;

    // ===== 病历 =====

    @Operation(summary = "创建病历")
    @PostMapping
    public R<MedicalRecord> create(@RequestBody MedicalRecord record) {
        return R.ok("创建成功", recordService.createRecord(record));
    }

    @Operation(summary = "修改病历")
    @PutMapping("/{id}")
    public R<MedicalRecord> update(@PathVariable Long id, @RequestBody MedicalRecord record) {
        record.setId(id);
        return R.ok("更新成功", recordService.updateRecord(record));
    }

    @Operation(summary = "删除病历")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return R.okMsg("删除成功");
    }

    @Operation(summary = "查询病历详情")
    @GetMapping("/{id}")
    public R<MedicalRecord> get(@PathVariable Long id) {
        MedicalRecord r = recordService.getRecord(id);
        return r != null ? R.ok(r) : R.notFound("病历不存在");
    }

    @Operation(summary = "分页查询病历")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        List<MedicalRecord> list = recordService.listRecords(page, size, patientId, doctorId);
        long total = recordService.countRecords(patientId, doctorId);
        return R.ok(Map.of("records", list, "total", total));
    }

    // ===== 检查报告 =====

    @Operation(summary = "创建检查报告")
    @PostMapping("/report")
    public R<ExamReport> createReport(@RequestBody ExamReport report) {
        return R.ok("创建成功", recordService.createReport(report));
    }

    @Operation(summary = "修改检查报告")
    @PutMapping("/report/{id}")
    public R<ExamReport> updateReport(@PathVariable Long id, @RequestBody ExamReport report) {
        report.setId(id);
        return R.ok("更新成功", recordService.updateReport(report));
    }

    @Operation(summary = "删除检查报告")
    @DeleteMapping("/report/{id}")
    public R<Void> deleteReport(@PathVariable Long id) {
        recordService.deleteReport(id);
        return R.okMsg("删除成功");
    }

    @Operation(summary = "查询报告详情")
    @GetMapping("/report/{id}")
    public R<ExamReport> getReport(@PathVariable Long id) {
        ExamReport r = recordService.getReport(id);
        return r != null ? R.ok(r) : R.notFound("报告不存在");
    }

    @Operation(summary = "分页查询报告")
    @GetMapping("/report/list")
    public R<Map<String, Object>> listReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String examType) {
        List<ExamReport> list = recordService.listReports(page, size, patientId, examType);
        long total = recordService.countReports(patientId, examType);
        return R.ok(Map.of("records", list, "total", total));
    }

    // ===== 跨机构分享 =====

    @Operation(summary = "分享报告给其他机构")
    @PostMapping("/report/{id}/share")
    public R<String> shareReport(@PathVariable Long id, @RequestParam String hospitalCode) {
        String token = recordService.shareReport(id, hospitalCode);
        return R.ok("分享成功，令牌: " + token, token);
    }

    // ===== 审计链校验 =====

    @Operation(summary = "验证患者报告的审计链完整性")
    @GetMapping("/audit/verify/{patientId}")
    public R<Boolean> verifyAuditChain(@PathVariable Long patientId) {
        boolean valid = recordService.verifyAuditChain(patientId);
        return R.ok(valid ? "审计链完整，数据未被篡改" : "警告：审计链断裂，数据可能被篡改！", valid);
    }
}
