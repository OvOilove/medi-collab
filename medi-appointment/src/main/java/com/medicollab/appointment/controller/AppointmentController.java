package com.medicollab.appointment.controller;

import com.medicollab.appointment.entity.Appointment;
import com.medicollab.appointment.entity.Department;
import com.medicollab.appointment.entity.DoctorSchedule;
import com.medicollab.appointment.service.AppointmentService;
import com.medicollab.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "预约挂号", description = "预约、科室、排班管理")
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }


    private final AppointmentService appointmentService;

    // ===== 预约 =====

    @Operation(summary = "创建预约")
    @PostMapping
    public R<Appointment> create(@RequestBody Appointment apt) {
        return R.ok("预约成功", appointmentService.createAppointment(apt));
    }

    @Operation(summary = "查询预约详情")
    @GetMapping("/{id}")
    public R<Appointment> get(@PathVariable Long id) {
        Appointment apt = appointmentService.getAppointment(id);
        return apt != null ? R.ok(apt) : R.notFound("预约不存在");
    }

    @Operation(summary = "分页查询预约列表")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status) {
        List<Appointment> list = appointmentService.listAppointments(page, size, patientId, status);
        long total = appointmentService.countAppointments(patientId, status);
        return R.ok(Map.of("records", list, "total", total, "page", page, "size", size));
    }

    @Operation(summary = "取消预约")
    @PutMapping("/{id}/cancel")
    public R<Appointment> cancel(@PathVariable Long id) {
        return R.ok("已取消", appointmentService.cancelAppointment(id));
    }

    @Operation(summary = "完成就诊")
    @PutMapping("/{id}/complete")
    public R<Appointment> complete(@PathVariable Long id) {
        return R.ok("就诊完成", appointmentService.completeAppointment(id));
    }

    // ===== 科室 =====

    @Operation(summary = "新增科室")
    @PostMapping("/dept")
    public R<Department> createDept(@RequestBody Department dept) {
        return R.ok(appointmentService.createDept(dept));
    }

    @Operation(summary = "修改科室")
    @PutMapping("/dept/{id}")
    public R<Department> updateDept(@PathVariable Long id, @RequestBody Department dept) {
        dept.setId(id);
        return R.ok(appointmentService.updateDept(dept));
    }

    @Operation(summary = "删除科室")
    @DeleteMapping("/dept/{id}")
    public R<Void> deleteDept(@PathVariable Long id) {
        appointmentService.deleteDept(id);
        return R.okMsg("删除成功");
    }

    @Operation(summary = "查询科室详情")
    @GetMapping("/dept/{id}")
    public R<Department> getDept(@PathVariable Long id) {
        Department dept = appointmentService.getDept(id);
        return dept != null ? R.ok(dept) : R.notFound("科室不存在");
    }

    @Operation(summary = "科室列表")
    @GetMapping("/dept/list")
    public R<List<Department>> listDepts(@RequestParam(required = false) String keyword) {
        return R.ok(appointmentService.listDepts(keyword));
    }

    // ===== 排班 =====

    @Operation(summary = "新增排班")
    @PostMapping("/schedule")
    public R<DoctorSchedule> createSchedule(@RequestBody DoctorSchedule s) {
        return R.ok(appointmentService.createSchedule(s));
    }

    @Operation(summary = "修改排班")
    @PutMapping("/schedule/{id}")
    public R<DoctorSchedule> updateSchedule(@PathVariable Long id, @RequestBody DoctorSchedule s) {
        s.setId(id);
        return R.ok(appointmentService.updateSchedule(s));
    }

    @Operation(summary = "删除排班")
    @DeleteMapping("/schedule/{id}")
    public R<Void> deleteSchedule(@PathVariable Long id) {
        appointmentService.deleteSchedule(id);
        return R.okMsg("删除成功");
    }

    @Operation(summary = "排班列表")
    @GetMapping("/schedule/list")
    public R<Map<String, Object>> listSchedules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long doctorId) {
        List<DoctorSchedule> list = appointmentService.listSchedules(page, size, deptId, doctorId);
        long total = appointmentService.countSchedules(deptId, doctorId);
        return R.ok(Map.of("records", list, "total", total, "page", page, "size", size));
    }

    // ===== Redis 热度排行 =====

    @Operation(summary = "科室热度排行 (Redis ZSet)")
    @GetMapping("/dept/hot")
    public R<List<Map<String, Object>>> deptHotRank(
            @RequestParam(defaultValue = "10") int topN) {
        return R.ok(appointmentService.getDeptHotRank(topN));
    }
}
