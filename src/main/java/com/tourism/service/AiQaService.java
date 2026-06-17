package com.tourism.service;

import com.tourism.dto.AiAskRequest;
import com.tourism.vo.AiAnswerVO;

public interface AiQaService {

    AiAnswerVO ask(AiAskRequest request);
}
