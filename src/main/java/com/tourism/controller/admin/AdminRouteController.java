package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.dto.RouteManageRequest;
import com.tourism.service.AdminRouteService;
import com.tourism.vo.RouteManageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/route")
public class AdminRouteController {

    @Autowired
    private AdminRouteService adminRouteService;

    @GetMapping("/list")
    public ApiResponse<List<RouteManageVO>> list() {
        return ApiResponse.success(adminRouteService.listRoutes());
    }

    @PostMapping("/save")
    public ApiResponse<RouteManageVO> save(@RequestBody RouteManageRequest request) {
        return ApiResponse.success("路线保存成功", adminRouteService.saveRoute(request));
    }

    @PutMapping("/update")
    public ApiResponse<RouteManageVO> update(@RequestBody RouteManageRequest request) {
        return ApiResponse.success("路线修改成功", adminRouteService.updateRoute(request));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminRouteService.deleteRoute(id);
        return ApiResponse.success("路线删除成功", "OK");
    }
}
