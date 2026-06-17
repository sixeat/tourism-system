package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.dto.FavoriteRequest;
import com.tourism.entity.UserFavorite;
import com.tourism.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/user/favorites")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    @GetMapping
    public ApiResponse<List<UserFavorite>> list(HttpSession session) {
        return ApiResponse.success(userFavoriteService.list(currentUserId(session)));
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody FavoriteRequest request, HttpSession session) {
        userFavoriteService.add(currentUserId(session), request);
        return ApiResponse.success("收藏成功", "OK");
    }

    @DeleteMapping
    public ApiResponse<String> remove(@RequestParam String favoriteType, @RequestParam String targetId, HttpSession session) {
        userFavoriteService.remove(currentUserId(session), favoriteType, targetId);
        return ApiResponse.success("已取消收藏", "OK");
    }

    private Long currentUserId(HttpSession session) {
        return Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));
    }
}
