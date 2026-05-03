package com.littlewin.note.service;

import com.littlewin.note.domain.dto.AdminNotificationDTO;

public interface AdminNotificationService {

    void sendNotification(Long senderId, AdminNotificationDTO dto);
}
