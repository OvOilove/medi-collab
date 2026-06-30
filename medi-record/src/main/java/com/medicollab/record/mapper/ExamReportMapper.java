package com.medicollab.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medicollab.record.entity.ExamReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExamReportMapper extends BaseMapper<ExamReport> {

    @Select("SELECT * FROM exam_report WHERE patient_id = #{patientId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    ExamReport findLatestByPatient(Long patientId);
}
