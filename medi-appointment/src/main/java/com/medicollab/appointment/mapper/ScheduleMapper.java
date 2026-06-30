package com.medicollab.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medicollab.appointment.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper extends BaseMapper<DoctorSchedule> {
}
