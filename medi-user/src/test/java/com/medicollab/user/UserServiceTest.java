package com.medicollab.user;

import com.medicollab.user.entity.User;
import com.medicollab.user.mapper.UserMapper;
import com.medicollab.user.service.UserService;
import com.medicollab.common.dto.LoginRequest;
import com.medicollab.common.dto.LoginResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private static Long testUserId;

    @Test
    @Order(1)
    @DisplayName("1. 用户注册测试")
    void testRegister() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");
        user.setRealName("测试用户");
        user.setPhone("13800138000");
        user.setRole("PATIENT");

        User saved = userService.register(user);
        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
        testUserId = saved.getId();
        System.out.println("✅ 用户注册成功: ID=" + testUserId);
    }

    @Test
    @Order(2)
    @DisplayName("2. 重复用户名注册应抛出异常")
    void testDuplicateRegister() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");
        assertThrows(Exception.class, () -> userService.register(user));
        System.out.println("✅ 重复注册校验通过");
    }

    @Test
    @Order(3)
    @DisplayName("3. 用户登录测试")
    void testLogin() {
        LoginRequest req = new LoginRequest("testuser", "123456");
        LoginResponse resp = userService.login(req);
        assertNotNull(resp.getToken());
        assertEquals("testuser", resp.getUsername());
        assertTrue(resp.getToken().length() > 20);
        System.out.println("✅ 登录成功: Token=" + resp.getToken().substring(0, 30) + "...");
    }

    @Test
    @Order(4)
    @DisplayName("4. 错误密码登录应失败")
    void testLoginWrongPassword() {
        LoginRequest req = new LoginRequest("testuser", "wrongpassword");
        assertThrows(Exception.class, () -> userService.login(req));
        System.out.println("✅ 错误密码校验通过");
    }

    @Test
    @Order(5)
    @DisplayName("5. 根据ID查询用户")
    void testGetById() {
        User user = userService.getById(testUserId);
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        System.out.println("✅ 根据ID查询成功: " + user.getRealName());
    }

    @Test
    @Order(6)
    @DisplayName("6. 更新用户信息")
    void testUpdateUser() {
        User user = userService.getById(testUserId);
        user.setRealName("测试用户_已修改");
        user.setPhone("13900139000");
        User updated = userService.updateUser(user);
        assertEquals("测试用户_已修改", updated.getRealName());
        System.out.println("✅ 更新用户成功: " + updated.getRealName());
    }

    @Test
    @Order(7)
    @DisplayName("7. 修改密码")
    void testChangePassword() {
        userService.changePassword(testUserId, "123456", "654321");
        // 用新密码登录验证
        LoginRequest req = new LoginRequest("testuser", "654321");
        LoginResponse resp = userService.login(req);
        assertNotNull(resp.getToken());
        System.out.println("✅ 密码修改成功，新密码登录验证通过");
    }

    @Test
    @Order(8)
    @DisplayName("8. 分页查询用户列表")
    void testListUsers() {
        var list = userService.listUsers(1, 10, null);
        assertFalse(list.isEmpty());
        System.out.println("✅ 用户列表查询成功: 总数=" + userService.countUsers(null));
    }

    @Test
    @Order(9)
    @DisplayName("9. 模糊搜索用户")
    void testSearchUsers() {
        var list = userService.listUsers(1, 10, "测试");
        assertFalse(list.isEmpty());
        System.out.println("✅ 模糊搜索成功: 匹配数=" + userService.countUsers("测试"));
    }

    @Test
    @Order(10)
    @DisplayName("10. 删除用户")
    void testDeleteUser() {
        userService.deleteUser(testUserId);
        User deleted = userMapper.selectById(testUserId);
        assertNull(deleted); // 逻辑删除后MP默认不返回
        System.out.println("✅ 删除用户成功");
    }
}
