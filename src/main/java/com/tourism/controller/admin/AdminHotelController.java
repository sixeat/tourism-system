package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.entity.Hotel;
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
@RequestMapping("/api/admin/hotel")
public class AdminHotelController {

    @Autowired
    private AdminResourceService adminResourceService;

    @GetMapping("/list")
    public ApiResponse<List<Hotel>> list() {
        return ApiResponse.success(adminResourceService.listHotels());
    }

    @PostMapping("/save")
    public ApiResponse<Hotel> save(@RequestBody Hotel hotel) {
        return ApiResponse.success("酒店保存成功", adminResourceService.saveHotel(hotel));
    }

    @PutMapping("/update")
    public ApiResponse<Hotel> update(@RequestBody Hotel hotel) {
        return ApiResponse.success("酒店修改成功", adminResourceService.updateHotel(hotel));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminResourceService.deleteHotel(id);
        return ApiResponse.success("酒店删除成功", "OK");
    }
}
