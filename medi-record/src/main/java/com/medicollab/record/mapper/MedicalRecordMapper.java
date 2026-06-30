package com.medicollab.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medicollab.record.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
}
