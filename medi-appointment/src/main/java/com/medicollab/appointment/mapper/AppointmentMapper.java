package com.medicollab.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medicollab.appointment.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    @Select("SELECT COUNT(*) FROM appointment WHERE department_id = #{deptId} AND appointment_date = #{date} AND deleted = 0")
    int countByDeptAndDate(Long deptId, LocalDate date);

    @Select("SELECT COUNT(*) FROM appointment WHERE doctor_id = #{doctorId} AND schedule_id = #{scheduleId} AND status = 'BOOKED' AND deleted = 0")
    int countBookedBySchedule(Long doctorId, Long scheduleId);
}
