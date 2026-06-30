package com.medicollab.appointment.service;

import com.medicollab.appointment.entity.Appointment;
import com.medicollab.appointment.entity.Department;
import com.medicollab.appointment.entity.DoctorSchedule;

import java.util.List;
import java.util.Map;

public interface AppointmentService {

    // 预约
    Appointment createAppointment(Appointment appointment);
    Appointment getAppointment(Long id);
    List<Appointment> listAppointments(int page, int size, Long patientId, String status);
    long countAppointments(Long patientId, String status);
    Appointment cancelAppointment(Long id);
    Appointment completeAppointment(Long id);

    // 科室
    Department createDept(Department dept);
    Department updateDept(Department dept);
    void deleteDept(Long id);
    Department getDept(Long id);
    List<Department> listDepts(String keyword);

    // 排班
    DoctorSchedule createSchedule(DoctorSchedule schedule);
    DoctorSchedule updateSchedule(DoctorSchedule schedule);
    void deleteSchedule(Long id);
    List<DoctorSchedule> listSchedules(int page, int size, Long deptId, Long doctorId);
    long countSchedules(Long deptId, Long doctorId);

    // Redis - 热度排行
    void incrementDeptHot(Long deptId);
    List<Map<String, Object>> getDeptHotRank(int topN);

    // Redis - 号源锁定
    boolean lockSlot(Long scheduleId);
    void unlockSlot(Long scheduleId);
}
