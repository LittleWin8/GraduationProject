package com.littlewin.note.service;

import java.util.Map;

public interface AiAnalyzeService {

    Map<String, Object> analyze(Long userId, String question);
}
