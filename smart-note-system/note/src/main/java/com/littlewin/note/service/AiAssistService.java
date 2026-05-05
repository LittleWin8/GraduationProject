package com.littlewin.note.service;

import java.util.List;

public interface AiAssistService {

    String assist(Long userId, String content, String action);

    List<String> recommendTags(Long userId, String content);
}
