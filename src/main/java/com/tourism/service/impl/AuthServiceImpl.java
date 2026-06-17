package com.tourism.service.impl;

import com.tourism.dto.LoginRequest;
import com.tourism.dto.ProfileUpdateRequest;
import com.tourism.dto.RegisterRequest;
import com.tourism.entity.User;
import com.tourism.mapper.UserMapper;
import com.tourism.service.AuthService;
import com.tourism.vo.LoginUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public LoginUserVO login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Username or password is empty");
        }

        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("User is disabled");
        }
        if (!request.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Username or password is incorrect");
        }

        return toLoginUserVO(user);
    }

    @Override
    public LoginUserVO register(RegisterRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        String username = request.getUsername().trim();
        String password = request.getPassword().trim();
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度应为 3-20 个字符");
        }
        if (password.length() < 6 || password.length() > 30) {
            throw new IllegalArgumentException("密码长度应为 6-30 个字符");
        }
        if (userMapper.selectByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在，请换一个用户名");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(trimToNull(request.getPhone()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        return toLoginUserVO(user);
    }

    @Override
    public LoginUserVO profile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        return toLoginUserVO(user);
    }

    @Override
    public LoginUserVO updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        if (request == null) {
            throw new IllegalArgumentException("Profile request is empty");
        }
        user.setPhone(trimToNull(request.getPhone()));
        user.setEmail(trimToNull(request.getEmail()));
        userMapper.updateProfile(user);

        if (!isBlank(request.getNewPassword())) {
            if (isBlank(request.getOldPassword()) || !request.getOldPassword().equals(user.getPassword())) {
                throw new IllegalArgumentException("原密码不正确");
            }
            if (request.getNewPassword().trim().length() < 6) {
                throw new IllegalArgumentException("新密码至少 6 位");
            }
            user.setPassword(request.getNewPassword().trim());
            userMapper.updatePassword(user);
        }
        return toLoginUserVO(userMapper.selectById(userId));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private LoginUserVO toLoginUserVO(User user) {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        return vo;
    }
}
