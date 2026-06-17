package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.dto.LoginRequest;
import com.tourism.dto.ProfileUpdateRequest;
import com.tourism.dto.RegisterRequest;
import com.tourism.service.AuthService;
import com.tourism.vo.LoginUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@RequestBody LoginRequest request, HttpSession session) {
        LoginUserVO user = authService.login(request);
        session.setAttribute(SessionConstants.LOGIN_USER_ID, user.getId());
        session.setAttribute(SessionConstants.LOGIN_USERNAME, user.getUsername());
        session.setAttribute(SessionConstants.LOGIN_USER_ROLE, user.getRole());
        return ApiResponse.success("login success", user);
    }

    @PostMapping("/register")
    public ApiResponse<LoginUserVO> register(@RequestBody RegisterRequest request, HttpSession session) {
        LoginUserVO user = authService.register(request);
        session.setAttribute(SessionConstants.LOGIN_USER_ID, user.getId());
        session.setAttribute(SessionConstants.LOGIN_USERNAME, user.getUsername());
        session.setAttribute(SessionConstants.LOGIN_USER_ROLE, user.getRole());
        return ApiResponse.success("注册成功", user);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.success("logout success", "OK");
    }

    @GetMapping("/me")
    public ApiResponse<LoginUserVO> me(HttpSession session) {
        Object userId = session.getAttribute(SessionConstants.LOGIN_USER_ID);
        if (userId == null) {
            return ApiResponse.fail("not logged in");
        }
        return ApiResponse.success(authService.profile(Long.valueOf(String.valueOf(userId))));
    }

    @PostMapping("/profile")
    public ApiResponse<LoginUserVO> updateProfile(@RequestBody ProfileUpdateRequest request, HttpSession session) {
        Object userId = session.getAttribute(SessionConstants.LOGIN_USER_ID);
        if (userId == null) {
            return ApiResponse.fail("not logged in");
        }
        LoginUserVO user = authService.updateProfile(Long.valueOf(String.valueOf(userId)), request);
        session.setAttribute(SessionConstants.LOGIN_USERNAME, user.getUsername());
        session.setAttribute(SessionConstants.LOGIN_USER_ROLE, user.getRole());
        return ApiResponse.success("资料已保存", user);
    }
}
