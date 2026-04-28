package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.vo.MessageVO;

/**
 * 站内消息服务接口
 */
public interface MessageService {

    /** 查询未读消息数 */
    int getUnreadCount(Long userId);

    /** 分页查询消息列表（查询后自动标记已读） */
    IPage<MessageVO> listMessages(Long userId, int page, int size);

    /** 全部标记已读 */
    void markAllRead(Long userId);

    /** 删除单条消息（仅本人） */
    void deleteMessage(Long userId, Long messageId);

    /** 发送消息（评论时调用） */
    void sendMessage(Long receiverId, Long senderId, Long noteId, Long commentId, int type, String content);
}
