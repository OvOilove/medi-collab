package com.medicollab.appointment.feign;

import com.medicollab.common.R;
import com.medicollab.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 用户服务远程调用接口
 */
@FeignClient(name = "medi-user", path = "/api/user")
public interface UserFeignClient {

    @GetMapping("/dto/{id}")
    R<UserDTO> getUserDTO(@PathVariable("id") Long id);

    @PostMapping("/batch")
    R<List<UserDTO>> batchQuery(@RequestBody List<Long> ids);
}
