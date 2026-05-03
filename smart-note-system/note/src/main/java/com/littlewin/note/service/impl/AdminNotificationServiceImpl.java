package com.littlewin.note.service.impl;

import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.dto.AdminNotificationDTO;
import com.littlewin.note.mapper.UserMessageMapper;
import com.littlewin.note.service.AdminNotificationService;
import com.littlewin.note.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final MessageService messageService;
    private final UserMessageMapper userMessageMapper;

    @Override
    public void sendNotification(Long senderId, AdminNotificationDTO dto) {
        List<Long> targets;
        if (!CollectionUtils.isEmpty(dto.getUserIds())) {
            targets = dto.getUserIds();
        } else {
            targets = userMessageMapper.selectAllUserIds();
        }

        if (CollectionUtils.isEmpty(targets)) {
            throw new ServiceException("没有可发送的目标用户");
        }

        for (Long receiverId : targets) {
            messageService.sendMessage(
                    receiverId, senderId, dto.getNoteId(), null,
                    dto.getType(), dto.getTitle(), dto.getContent()
            );
        }
    }
}
