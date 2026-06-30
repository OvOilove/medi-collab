package com.medicollab.user.controller;

import com.medicollab.common.R;
import com.medicollab.common.dto.LoginRequest;
import com.medicollab.common.dto.LoginResponse;
import com.medicollab.common.dto.UserDTO;
import com.medicollab.user.entity.User;
import com.medicollab.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户注册、登录、CRUD 接口")
@RestController
@RequestMapping("/api/user")
public class UserController {
    public UserController(UserService userService) {
        this.userService = userService;
    }


    private final UserService userService;

    // ========== 认证相关 ==========

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<User> register(@Valid @RequestBody User user) {
        return R.ok("注册成功", userService.register(user));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok("登录成功", userService.login(request));
    }

    @Operation(summary = "Token 校验")
    @GetMapping("/verify")
    public R<UserDTO> verifyToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = com.medicollab.common.util.JwtUtils.getUserId(token);
        return R.ok(userService.getUserDTO(userId));
    }

    // ========== 用户 CRUD ==========

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public R<User> getUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.notFound("用户不存在");
        }
        return R.ok(user);
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/list")
    public R<Map<String, Object>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        List<User> list = userService.listUsers(page, size, keyword);
        long total = userService.countUsers(keyword);
        return R.ok(Map.of("records", list, "total", total, "page", page, "size", size));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public R<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return R.ok("更新成功", userService.updateUser(user));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.okMsg("删除成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/{id}/password")
    public R<Void> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        userService.changePassword(id, params.get("oldPassword"), params.get("newPassword"));
        return R.okMsg("密码修改成功");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public R<UserDTO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(userService.getUserDTO(userId));
    }

    // ========== 跨服务调用接口 ==========

    @Operation(summary = "批量查询用户 (Feign调用)")
    @PostMapping("/batch")
    public R<List<UserDTO>> batchQuery(@RequestBody List<Long> ids) {
        return R.ok(userService.getUserDTOs(ids));
    }

    @Operation(summary = "查询用户DTO (Feign调用)")
    @GetMapping("/dto/{id}")
    public R<UserDTO> getUserDTO(@PathVariable Long id) {
        UserDTO dto = userService.getUserDTO(id);
        if (dto == null) {
            return R.notFound("用户不存在");
        }
        return R.ok(dto);
    }
}
