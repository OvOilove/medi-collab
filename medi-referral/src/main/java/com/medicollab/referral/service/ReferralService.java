package com.medicollab.referral.service;

import com.medicollab.referral.entity.Referral;

import java.util.List;

public interface ReferralService {

    Referral createReferral(Referral referral);
    Referral approveReferral(Long id, Long doctorId, String doctorName);
    Referral rejectReferral(Long id, String reason);
    Referral completeReferral(Long id, String feedback);
    Referral cancelReferral(Long id);
    Referral getReferral(Long id);
    List<Referral> listReferrals(int page, int size, Long patientId, String status, Long fromHospitalId, Long toHospitalId);
    long countReferrals(Long patientId, String status, Long fromHospitalId, Long toHospitalId);
}
