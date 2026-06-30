package com.medicollab.record.service.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicollab.common.exception.BusinessException;
import com.medicollab.record.entity.ExamReport;
import com.medicollab.record.entity.MedicalRecord;
import com.medicollab.record.mapper.ExamReportMapper;
import com.medicollab.record.mapper.MedicalRecordMapper;
import com.medicollab.record.service.RecordService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class RecordServiceImpl implements RecordService {

    private static final Logger log = LoggerFactory.getLogger(RecordServiceImpl.class);

    private final MedicalRecordMapper recordMapper;
    private final ExamReportMapper reportMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public RecordServiceImpl(MedicalRecordMapper recordMapper, ExamReportMapper reportMapper,
                             RedisTemplate<String, Object> redisTemplate) {
        this.recordMapper = recordMapper;
        this.reportMapper = reportMapper;
        this.redisTemplate = redisTemplate;
    }

    // ========== 病历 ==========

    @Override
    @Transactional
    public MedicalRecord createRecord(MedicalRecord record) {
        // 计算审计哈希
        String raw = record.getPatientId() + record.getDiagnosis() + record.getTreatmentPlan() + System.currentTimeMillis();
        record.setAuditHash(DigestUtil.sha256Hex(raw));
        recordMapper.insert(record);
        // Redis 缓存
        cacheRecord(record);
        return record;
    }

    @Override
    @Transactional
    public MedicalRecord updateRecord(MedicalRecord record) {
        MedicalRecord exist = recordMapper.selectById(record.getId());
        if (exist == null) throw new BusinessException("病历不存在");
        // 重新计算审计哈希（任何修改都会改变哈希）
        String raw = record.getPatientId() + record.getDiagnosis() + record.getTreatmentPlan() + System.currentTimeMillis();
        record.setAuditHash(DigestUtil.sha256Hex(raw));
        recordMapper.updateById(record);
        redisTemplate.delete("medi:record:" + record.getId());
        cacheRecord(recordMapper.selectById(record.getId()));
        return recordMapper.selectById(record.getId());
    }

    @Override
    public void deleteRecord(Long id) {
        recordMapper.deleteById(id);
        redisTemplate.delete("medi:record:" + id);
    }

    @Override
    public MedicalRecord getRecord(Long id) {
        String key = "medi:record:" + id;
        MedicalRecord cached = (MedicalRecord) redisTemplate.opsForValue().get(key);
        if (cached != null) return cached;
        MedicalRecord record = recordMapper.selectById(id);
        if (record != null) cacheRecord(record);
        return record;
    }

    @Override
    public List<MedicalRecord> listRecords(int page, int size, Long patientId, Long doctorId) {
        LambdaQueryWrapper<MedicalRecord> w = new LambdaQueryWrapper<>();
        if (patientId != null) w.eq(MedicalRecord::getPatientId, patientId);
        if (doctorId != null) w.eq(MedicalRecord::getDoctorId, doctorId);
        w.orderByDesc(MedicalRecord::getCreateTime);
        return recordMapper.selectPage(new Page<>(page, size), w).getRecords();
    }

    @Override
    public long countRecords(Long patientId, Long doctorId) {
        LambdaQueryWrapper<MedicalRecord> w = new LambdaQueryWrapper<>();
        if (patientId != null) w.eq(MedicalRecord::getPatientId, patientId);
        if (doctorId != null) w.eq(MedicalRecord::getDoctorId, doctorId);
        return recordMapper.selectCount(w);
    }

    // ========== 检查报告 ==========

    @Override
    @Transactional
    public ExamReport createReport(ExamReport report) {
        // 查找前一条报告，构建哈希链
        ExamReport prev = reportMapper.findLatestByPatient(report.getPatientId());
        String prevHash = prev != null ? prev.getCurrentHash() : "GENESIS";
        report.setPrevHash(prevHash);
        // 计算当前报告哈希
        String raw = report.getPatientId() + report.getExamType() + report.getConclusion() + prevHash;
        report.setCurrentHash(DigestUtil.sha256Hex(raw));
        report.setStatus("PUBLISHED");
        reportMapper.insert(report);
        cacheReport(report);
        return report;
    }

    @Override
    @Transactional
    public ExamReport updateReport(ExamReport report) {
        ExamReport exist = reportMapper.selectById(report.getId());
        if (exist == null) throw new BusinessException("报告不存在");
        String raw = report.getPatientId() + report.getExamType() + report.getConclusion() + report.getPrevHash();
        report.setCurrentHash(DigestUtil.sha256Hex(raw));
        reportMapper.updateById(report);
        redisTemplate.delete("medi:report:" + report.getId());
        return report;
    }

    @Override
    public void deleteReport(Long id) {
        reportMapper.deleteById(id);
        redisTemplate.delete("medi:report:" + id);
    }

    @Override
    public ExamReport getReport(Long id) {
        String key = "medi:report:" + id;
        ExamReport cached = (ExamReport) redisTemplate.opsForValue().get(key);
        if (cached != null) return cached;
        ExamReport report = reportMapper.selectById(id);
        if (report != null) cacheReport(report);
        return report;
    }

    @Override
    public List<ExamReport> listReports(int page, int size, Long patientId, String examType) {
        LambdaQueryWrapper<ExamReport> w = new LambdaQueryWrapper<>();
        if (patientId != null) w.eq(ExamReport::getPatientId, patientId);
        if (examType != null) w.eq(ExamReport::getExamType, examType);
        w.orderByDesc(ExamReport::getCreateTime);
        return reportMapper.selectPage(new Page<>(page, size), w).getRecords();
    }

    @Override
    public long countReports(Long patientId, String examType) {
        LambdaQueryWrapper<ExamReport> w = new LambdaQueryWrapper<>();
        if (patientId != null) w.eq(ExamReport::getPatientId, patientId);
        if (examType != null) w.eq(ExamReport::getExamType, examType);
        return reportMapper.selectCount(w);
    }

    // ========== 跨机构分享 ==========

    @Override
    public String shareReport(Long reportId, String targetHospitalCode) {
        ExamReport report = reportMapper.selectById(reportId);
        if (report == null) throw new BusinessException("报告不存在");
        // 生成分享令牌 (有效期 7 天)
        String shareToken = UUID.randomUUID().toString().replace("-", "");
        String key = "medi:share:" + shareToken;
        redisTemplate.opsForValue().set(key, reportId, Duration.ofDays(7));
        log.info("报告 {} 已分享至医院 {}, 分享令牌: {}", reportId, targetHospitalCode, shareToken);
        return shareToken;
    }

    // ========== 审计链校验 ==========

    @Override
    public boolean verifyAuditChain(Long patientId) {
        // 获取该患者所有报告，按时间排序
        LambdaQueryWrapper<ExamReport> w = new LambdaQueryWrapper<>();
        w.eq(ExamReport::getPatientId, patientId).orderByAsc(ExamReport::getCreateTime);
        List<ExamReport> reports = reportMapper.selectList(w);
        if (reports.isEmpty()) return true;

        String expectedPrev = "GENESIS";
        for (ExamReport r : reports) {
            // 验证 prevHash 是否匹配
            if (!expectedPrev.equals(r.getPrevHash())) {
                log.warn("审计链断裂! 报告ID={}, 期望prevHash={}, 实际={}",
                        r.getId(), expectedPrev, r.getPrevHash());
                return false;
            }
            // 验证 currentHash
            String raw = r.getPatientId() + r.getExamType() + r.getConclusion() + r.getPrevHash();
            String computedHash = DigestUtil.sha256Hex(raw);
            if (!computedHash.equals(r.getCurrentHash())) {
                log.warn("哈希不匹配! 报告ID={}, 存储哈希={}, 计算哈希={}",
                        r.getId(), r.getCurrentHash(), computedHash);
                return false;
            }
            expectedPrev = r.getCurrentHash();
        }
        log.info("审计链校验通过，共 {} 条报告", reports.size());
        return true;
    }

    private void cacheRecord(MedicalRecord record) {
        redisTemplate.opsForValue().set("medi:record:" + record.getId(), record, Duration.ofHours(1));
    }

    private void cacheReport(ExamReport report) {
        redisTemplate.opsForValue().set("medi:report:" + report.getId(), report, Duration.ofHours(1));
    }
}
