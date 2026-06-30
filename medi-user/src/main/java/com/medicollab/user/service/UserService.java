package com.medicollab.user.service;

import com.medicollab.common.dto.LoginRequest;
import com.medicollab.common.dto.LoginResponse;
import com.medicollab.common.dto.UserDTO;
import com.medicollab.user.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /** 用户注册 */
    User register(User user);

    /** 用户登录 */
    LoginResponse login(LoginRequest request);

    /** 根据 ID 查询用户 */
    User getById(Long id);

    /** 根据用户名查询 */
    User getByUsername(String username);

    /** 分页查询用户列表 */
    List<User> listUsers(int page, int size, String keyword);

    /** 统计用户总数 */
    long countUsers(String keyword);

    /** 更新用户信息 */
    User updateUser(User user);

    /** 删除用户 */
    void deleteUser(Long id);

    /** 修改密码 */
    void changePassword(Long userId, String oldPwd, String newPwd);

    /** 获取用户 DTO (跨服务调用) */
    UserDTO getUserDTO(Long id);

    /** 批量查询用户 DTO */
    List<UserDTO> getUserDTOs(List<Long> ids);
}
