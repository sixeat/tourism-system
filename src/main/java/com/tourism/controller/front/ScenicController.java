package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.entity.ScenicSpot;
import com.tourism.service.ScenicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scenic")
public class ScenicController {

    @Autowired
    private ScenicService scenicService;

    @GetMapping("/list")
    public ApiResponse<List<ScenicSpot>> list() {
        return ApiResponse.success(scenicService.listAll());
    }
}