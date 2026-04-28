package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.entity.UserMessage;
import com.littlewin.note.domain.vo.MessageVO;
import com.littlewin.note.mapper.UserMessageMapper;
import com.littlewin.note.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 站内消息服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final UserMessageMapper userMessageMapper;

    @Override
    public int getUnreadCount(Long userId) {
        return userMessageMapper.countUnread(userId);
    }

    @Override
    public IPage<MessageVO> listMessages(Long userId, int page, int size) {
        Page<MessageVO> p = new Page<>(page, size);
        IPage<MessageVO> result = userMessageMapper.selectMessagePage(p, userId);

        List<Long> unreadIds = result.getRecords().stream()
                .filter(vo -> !vo.getIsRead())
                .map(MessageVO::getId)
                .collect(Collectors.toList());
        if (!unreadIds.isEmpty()) {
            userMessageMapper.markReadByIds(unreadIds);
            for (MessageVO vo : result.getRecords()) {
                vo.setIsRead(true);
            }
        }

        return result;
    }

    @Override
    public void markAllRead(Long userId) {
        userMessageMapper.markAllRead(userId);
    }

    @Override
    public void deleteMessage(Long userId, Long messageId) {
        UserMessage msg = userMessageMapper.selectById(messageId);
        if (msg == null) {
            throw new ServiceException("消息不存在");
        }
        if (!msg.getReceiverId().equals(userId)) {
            throw new ServiceException("无权删除此消息");
        }
        userMessageMapper.deleteById(messageId);
    }

    @Override
    public void sendMessage(Long receiverId, Long senderId, Long noteId, Long commentId, int type, String content) {
        UserMessage msg = new UserMessage();
        msg.setReceiverId(receiverId);
        msg.setSenderId(senderId);
        msg.setNoteId(noteId);
        msg.setCommentId(commentId);
        msg.setType(type);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        userMessageMapper.insert(msg);
    }
}
