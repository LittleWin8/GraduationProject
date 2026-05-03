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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站内消息服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final List<Integer> INTERACTION_TYPES = Arrays.asList(1, 2, 7, 8);
    private static final List<Integer> NOTICE_TYPES = Arrays.asList(3, 4, 5, 6);

    private final UserMessageMapper userMessageMapper;

    @Override
    public int getUnreadCount(Long userId) {
        return userMessageMapper.countUnread(userId);
    }

    @Override
    public Map<String, Integer> getUnreadCountGrouped(Long userId) {
        int interactionCount = userMessageMapper.countUnreadGrouped(userId, INTERACTION_TYPES);
        int noticeCount = userMessageMapper.countUnreadGrouped(userId, NOTICE_TYPES);
        Map<String, Integer> result = new HashMap<>();
        result.put("interactionCount", interactionCount);
        result.put("noticeCount", noticeCount);
        result.put("totalCount", interactionCount + noticeCount);
        return result;
    }

    @Override
    public IPage<MessageVO> listMessages(Long userId, int page, int size) {
        return listMessages(userId, null, page, size);
    }

    @Override
    public IPage<MessageVO> listMessages(Long userId, String group, int page, int size) {
        Page<MessageVO> p = new Page<>(page, size);
        List<Integer> types = resolveTypes(group);

        IPage<MessageVO> result;
        if (types == null) {
            result = userMessageMapper.selectMessagePage(p, userId);
        } else {
            result = userMessageMapper.selectMessagePageByGroup(p, userId, types);
        }

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
        sendMessage(receiverId, senderId, noteId, commentId, type, null, content);
    }

    @Override
    public void sendMessage(Long receiverId, Long senderId, Long noteId, Long commentId, int type, String title, String content) {
        UserMessage msg = new UserMessage();
        msg.setReceiverId(receiverId);
        msg.setSenderId(senderId);
        msg.setNoteId(noteId);
        msg.setCommentId(commentId);
        msg.setType(type);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        userMessageMapper.insert(msg);
    }

    private List<Integer> resolveTypes(String group) {
        if (group == null || group.isEmpty()) {
            return null;
        }
        switch (group) {
            case "interaction":
                return INTERACTION_TYPES;
            case "notice":
                return NOTICE_TYPES;
            default:
                return null;
        }
    }
}
