package com.tourism.service;

import com.tourism.dto.LoginRequest;
import com.tourism.dto.ProfileUpdateRequest;
import com.tourism.dto.RegisterRequest;
import com.tourism.vo.LoginUserVO;

public interface AuthService {
    LoginUserVO login(LoginRequest request);

    LoginUserVO register(RegisterRequest request);

    LoginUserVO profile(Long userId);

    LoginUserVO updateProfile(Long userId, ProfileUpdateRequest request);
}
