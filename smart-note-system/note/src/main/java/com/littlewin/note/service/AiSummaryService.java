package com.littlewin.note.service;

import java.util.Map;

public interface AiSummaryService {

    Map<String, String> generateSummary(Long noteId, Long userId);
}
