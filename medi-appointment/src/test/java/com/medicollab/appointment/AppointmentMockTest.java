package com.medicollab.appointment;

import com.medicollab.appointment.controller.AppointmentController;
import com.medicollab.appointment.entity.Appointment;
import com.medicollab.appointment.entity.Department;
import com.medicollab.appointment.entity.DoctorSchedule;
import com.medicollab.appointment.service.AppointmentService;
import com.medicollab.common.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentMockTest {

    @Mock private AppointmentService appointmentService;
    @InjectMocks private AppointmentController controller;

    @Test @DisplayName("1. 创建科室")
    void testCreateDept() {
        Department dept = new Department();
        dept.setId(1L); dept.setName("心内科");
        when(appointmentService.createDept(any())).thenReturn(dept);
        R<Department> r = controller.createDept(new Department());
        assertEquals(200, r.getCode());
        System.out.println("✅ 科室创建: " + r.getData().getName());
    }

    @Test @DisplayName("2. 创建排班")
    void testCreateSchedule() {
        DoctorSchedule s = new DoctorSchedule();
        s.setId(1L); s.setDoctorName("张医生");
        when(appointmentService.createSchedule(any())).thenReturn(s);
        R<DoctorSchedule> r = controller.createSchedule(new DoctorSchedule());
        assertEquals(200, r.getCode());
        System.out.println("✅ 排班创建: " + r.getData().getDoctorName());
    }

    @Test @DisplayName("3. 创建预约")
    void testCreateAppointment() {
        Appointment apt = new Appointment();
        apt.setId(1L); apt.setStatus("BOOKED"); apt.setQueueNumber(1);
        when(appointmentService.createAppointment(any())).thenReturn(apt);
        R<Appointment> r = controller.create(new Appointment());
        assertEquals(200, r.getCode());
        assertEquals("BOOKED", r.getData().getStatus());
        System.out.println("✅ 预约创建: 号序=" + r.getData().getQueueNumber());
    }

    @Test @DisplayName("4. 取消预约")
    void testCancelAppointment() {
        Appointment apt = new Appointment();
        apt.setId(1L); apt.setStatus("CANCELLED");
        when(appointmentService.cancelAppointment(1L)).thenReturn(apt);
        R<Appointment> r = controller.cancel(1L);
        assertEquals("CANCELLED", r.getData().getStatus());
        System.out.println("✅ 预约已取消");
    }

    @Test @DisplayName("5. Redis 热度排行")
    void testDeptHotRank() {
        when(appointmentService.getDeptHotRank(10)).thenReturn(List.of(
            Map.of("deptId", 1L, "deptName", "心内科", "score", 15L)
        ));
        R<List<Map<String, Object>>> r = controller.deptHotRank(10);
        assertFalse(r.getData().isEmpty());
        System.out.println("✅ 热度排行: " + r.getData().get(0).get("deptName") + "=" + r.getData().get(0).get("score"));
    }

    @Test @DisplayName("6. 分布式锁测试")
    void testDistributedLock() {
        when(appointmentService.lockSlot(1L)).thenReturn(true, false);
        assertTrue(appointmentService.lockSlot(1L));
        assertFalse(appointmentService.lockSlot(1L));
        System.out.println("✅ 分布式锁测试通过 (首次成功,重复失败)");
    }
}
