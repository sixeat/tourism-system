package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.dto.AiAskRequest;
import com.tourism.service.AiQaService;
import com.tourism.vo.AiAnswerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiQaController {

    @Autowired
    private AiQaService aiQaService;

    @PostMapping("/ask")
    public ApiResponse<AiAnswerVO> ask(@RequestBody AiAskRequest request) {
        return ApiResponse.success(aiQaService.ask(request));
    }
}
