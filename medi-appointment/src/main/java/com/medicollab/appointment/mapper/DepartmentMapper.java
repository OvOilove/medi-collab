package com.medicollab.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medicollab.appointment.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
