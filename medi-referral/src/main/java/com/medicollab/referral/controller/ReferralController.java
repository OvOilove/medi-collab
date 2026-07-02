package com.medicollab.referral.controller;

import com.medicollab.common.R;
import com.medicollab.referral.entity.Referral;
import com.medicollab.referral.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "双向转诊", description = "转诊申请、审批、撤回、完成")
@RestController
@RequestMapping("/api/referral")
public class ReferralController {
    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }


    private final ReferralService referralService;

    @Operation(summary = "提交转诊申请")
    @PostMapping
    public R<Referral> create(@RequestBody Referral referral) {
        return R.ok("转诊申请已提交", referralService.createReferral(referral));
    }

    @Operation(summary = "审批通过")
    @PutMapping("/{id}/approve")
    public R<Referral> approve(@PathVariable Long id,
                                @RequestParam Long doctorId,
                                @RequestParam String doctorName) {
        return R.ok("已批准转诊", referralService.approveReferral(id, doctorId, doctorName));
    }

    @Operation(summary = "拒绝转诊")
    @PutMapping("/{id}/reject")
    public R<Referral> reject(@PathVariable Long id, @RequestParam String reason) {
        return R.ok("已拒绝转诊", referralService.rejectReferral(id, reason));
    }

    @Operation(summary = "完成转诊（回转反馈）")
    @PutMapping("/{id}/complete")
    public R<Referral> complete(@PathVariable Long id, @RequestParam String feedback) {
        return R.ok("转诊已完成", referralService.completeReferral(id, feedback));
    }

    @Operation(summary = "撤回转诊申请")
    @PutMapping("/{id}/cancel")
    public R<Referral> cancel(@PathVariable Long id) {
        return R.ok("已撤回", referralService.cancelReferral(id));
    }

    @Operation(summary = "查询转诊详情")
    @GetMapping("/{id}")
    public R<Referral> get(@PathVariable Long id) {
        Referral r = referralService.getReferral(id);
        return r != null ? R.ok(r) : R.notFound("转诊记录不存在");
    }

    @Operation(summary = "分页查询转诊列表")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long fromHospitalId,
            @RequestParam(required = false) Long toHospitalId) {
        List<Referral> list = referralService.listReferrals(page, size, patientId, status, fromHospitalId, toHospitalId);
        long total = referralService.countReferrals(patientId, status, fromHospitalId, toHospitalId);
        return R.ok(Map.of("records", list, "total", total));
    }
}