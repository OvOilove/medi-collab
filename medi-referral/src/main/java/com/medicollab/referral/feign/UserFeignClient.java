package com.medicollab.referral.feign;

import com.medicollab.common.R;
import com.medicollab.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medi-user", path = "/api/user")
public interface UserFeignClient {
    @GetMapping("/dto/{id}")
    R<UserDTO> getUserDTO(@PathVariable("id") Long id);
}
