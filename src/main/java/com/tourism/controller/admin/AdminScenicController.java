package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.entity.ScenicSpot;
import com.tourism.service.AdminResourceService;
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
@RequestMapping("/api/admin/scenic")
public class AdminScenicController {

    @Autowired
    private AdminResourceService adminResourceService;

    @GetMapping("/list")
    public ApiResponse<List<ScenicSpot>> list() {
        return ApiResponse.success(adminResourceService.listScenicSpots());
    }

    @PostMapping("/save")
    public ApiResponse<ScenicSpot> save(@RequestBody ScenicSpot scenicSpot) {
        return ApiResponse.success("景点保存成功", adminResourceService.saveScenicSpot(scenicSpot));
    }

    @PutMapping("/update")
    public ApiResponse<ScenicSpot> update(@RequestBody ScenicSpot scenicSpot) {
        return ApiResponse.success("景点修改成功", adminResourceService.updateScenicSpot(scenicSpot));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminResourceService.deleteScenicSpot(id);
        return ApiResponse.success("景点删除成功", "OK");
    }
}
