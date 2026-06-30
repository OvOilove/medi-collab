package com.medicollab.user;

import com.medicollab.common.R;
import com.medicollab.common.dto.LoginRequest;
import com.medicollab.common.dto.LoginResponse;
import com.medicollab.common.dto.UserDTO;
import com.medicollab.user.controller.UserController;
import com.medicollab.user.entity.User;
import com.medicollab.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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

/**
 * 用户服务单元测试 (Mockito)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceMockTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private LoginResponse testLoginResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setRole("PATIENT");

        testLoginResponse = new LoginResponse();
        testLoginResponse.setToken("mock-jwt-token-xxxxx");
        testLoginResponse.setUserId(1L);
        testLoginResponse.setUsername("testuser");
        testLoginResponse.setRealName("测试用户");
        testLoginResponse.setRole("PATIENT");
    }

    @Test
    @DisplayName("1. 用户注册")
    void testRegister() {
        when(userService.register(any(User.class))).thenReturn(testUser);
        R<User> result = userController.register(new User());
        assertEquals(200, result.getCode());
        assertEquals("testuser", result.getData().getUsername());
        System.out.println("✅ Mock 注册测试通过");
    }

    @Test
    @DisplayName("2. 用户登录")
    void testLogin() {
        LoginRequest req = new LoginRequest("testuser", "123456");
        when(userService.login(req)).thenReturn(testLoginResponse);
        R<LoginResponse> result = userController.login(req);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData().getToken());
        System.out.println("✅ Mock 登录测试通过: Token=" + result.getData().getToken());
    }

    @Test
    @DisplayName("3. 根据ID查询用户")
    void testGetById() {
        when(userService.getById(1L)).thenReturn(testUser);
        R<User> result = userController.getUser(1L);
        assertEquals(200, result.getCode());
        assertEquals("testuser", result.getData().getUsername());
        System.out.println("✅ Mock 查询用户测试通过");
    }

    @Test
    @DisplayName("4. 查询不存在的用户返回404")
    void testGetByIdNotFound() {
        when(userService.getById(999L)).thenReturn(null);
        R<User> result = userController.getUser(999L);
        assertEquals(404, result.getCode());
        System.out.println("✅ Mock 404测试通过");
    }

    @Test
    @DisplayName("5. 分页查询用户列表")
    void testListUsers() {
        when(userService.listUsers(1, 10, null)).thenReturn(List.of(testUser));
        when(userService.countUsers(null)).thenReturn(1L);
        R<Map<String, Object>> result = userController.listUsers(1, 10, null);
        assertEquals(200, result.getCode());
        List<?> records = (List<?>) result.getData().get("records");
        assertEquals(1, records.size());
        System.out.println("✅ Mock 分页查询测试通过: 总数=" + result.getData().get("total"));
    }

    @Test
    @DisplayName("6. 更新用户信息")
    void testUpdateUser() {
        User updated = new User();
        updated.setId(1L);
        updated.setRealName("已修改");
        when(userService.updateUser(any(User.class))).thenReturn(updated);
        R<User> result = userController.updateUser(1L, new User());
        assertEquals(200, result.getCode());
        assertEquals("已修改", result.getData().getRealName());
        System.out.println("✅ Mock 更新用户测试通过");
    }

    @Test
    @DisplayName("7. 删除用户")
    void testDeleteUser() {
        doNothing().when(userService).deleteUser(1L);
        R<Void> result = userController.deleteUser(1L);
        assertEquals(200, result.getCode());
        verify(userService).deleteUser(1L);
        System.out.println("✅ Mock 删除用户测试通过");
    }

    @Test
    @DisplayName("8. 获取用户DTO (跨服务调用)")
    void testGetUserDTO() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("testuser");
        dto.setRole("PATIENT");
        when(userService.getUserDTO(1L)).thenReturn(dto);
        R<UserDTO> result = userController.getUserDTO(1L);
        assertEquals(200, result.getCode());
        assertEquals("testuser", result.getData().getUsername());
        System.out.println("✅ Mock 跨服务DTO查询通过");
    }

    @Test
    @DisplayName("9. 批量查询用户")
    void testBatchQuery() {
        when(userService.getUserDTOs(anyList())).thenReturn(List.of(new UserDTO()));
        R<List<UserDTO>> result = userController.batchQuery(List.of(1L, 2L));
        assertEquals(200, result.getCode());
        System.out.println("✅ Mock 批量查询测试通过");
    }

    @Test
    @DisplayName("10. 登录失败-错误密码")
    void testLoginFail() {
        LoginRequest req = new LoginRequest("testuser", "wrong");
        when(userService.login(req)).thenThrow(new RuntimeException("密码错误"));
        assertThrows(RuntimeException.class, () -> userController.login(req));
        System.out.println("✅ Mock 登录失败测试通过");
    }
}
