package com.medicollab.user.service.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicollab.common.dto.LoginRequest;
import com.medicollab.common.dto.LoginResponse;
import com.medicollab.common.dto.UserDTO;
import com.medicollab.common.exception.BusinessException;
import com.medicollab.common.util.JwtUtils;
import com.medicollab.user.entity.User;
import com.medicollab.user.mapper.UserMapper;
import com.medicollab.user.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserServiceImpl(UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    private static final String USER_CACHE_KEY = "medi:user:";

    @Override
    @Transactional
    public User register(User user) {
        // 校验用户名唯一
        User exist = getByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        // 密码加密
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setStatus(1);
        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
        return user;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 生成 JWT
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        // 缓存 token
        redisTemplate.opsForValue().set("medi:token:" + user.getId(), token, 7, TimeUnit.DAYS);
        // 缓存用户信息
        cacheUser(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public User getById(Long id) {
        // 先从缓存获取
        String key = USER_CACHE_KEY + id;
        User cached = (User) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        User user = userMapper.selectById(id);
        if (user != null) {
            cacheUser(user);
        }
        return user;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public List<User> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);
        return pageResult.getRecords();
    }

    @Override
    public long countUsers(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }
        return userMapper.selectCount(wrapper);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User exist = userMapper.selectById(user.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.updateById(user);
        // 清除缓存
        redisTemplate.delete(USER_CACHE_KEY + user.getId());
        // 重新缓存
        User updated = userMapper.selectById(user.getId());
        cacheUser(updated);
        return updated;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteById(id);
        redisTemplate.delete(USER_CACHE_KEY + id);
        redisTemplate.delete("medi:token:" + id);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPwd, String newPwd) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!BCrypt.checkpw(oldPwd, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(BCrypt.hashpw(newPwd, BCrypt.gensalt()));
        userMapper.updateById(user);
    }

    @Override
    public UserDTO getUserDTO(Long id) {
        User user = getById(id);
        if (user == null) return null;
        return toDTO(user);
    }

    @Override
    public List<UserDTO> getUserDTOs(List<Long> ids) {
        List<User> users = userMapper.selectBatchIds(ids);
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private void cacheUser(User user) {
        redisTemplate.opsForValue().set(USER_CACHE_KEY + user.getId(), user, Duration.ofHours(24));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setHospitalId(user.getHospitalId());
        dto.setHospitalName(user.getHospitalName());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setDepartmentName(user.getDepartmentName());
        return dto;
    }
}
