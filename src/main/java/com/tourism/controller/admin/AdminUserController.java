package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.entity.User;
import com.tourism.service.AdminResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private AdminResourceService adminResourceService;

    @GetMapping("/list")
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(adminResourceService.listUsers());
    }

    @PutMapping("/update")
    public ApiResponse<User> update(@RequestBody User user) {
        return ApiResponse.success("用户资料修改成功", adminResourceService.updateUser(user));
    }

    @PostMapping("/status")
    public ApiResponse<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        adminResourceService.updateUserStatus(id, status);
        return ApiResponse.success(status != null && status == 1 ? "用户已启用" : "用户已禁用", "OK");
    }
}
