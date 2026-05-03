package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.vo.MessageVO;

import java.util.Map;

/**
 * 站内消息服务接口
 */
public interface MessageService {

    /** 查询未读消息数 */
    int getUnreadCount(Long userId);

    /** 按类型分组查询未读数（互动消息 type∈(1,2,7,8)、系统通知 type∈(3,4,5,6)） */
    Map<String, Integer> getUnreadCountGrouped(Long userId);

    /** 分页查询消息列表（查询后自动标记已读） */
    IPage<MessageVO> listMessages(Long userId, int page, int size);

    /** 分页查询消息列表（支持按类型组过滤，查询后自动标记已读） */
    IPage<MessageVO> listMessages(Long userId, String group, int page, int size);

    /** 全部标记已读 */
    void markAllRead(Long userId);

    /** 删除单条消息（仅本人） */
    void deleteMessage(Long userId, Long messageId);

    /** 发送消息（评论时调用） */
    void sendMessage(Long receiverId, Long senderId, Long noteId, Long commentId, int type, String content);

    /** 发送消息（带标题，系统通知用） */
    void sendMessage(Long receiverId, Long senderId, Long noteId, Long commentId, int type, String title, String content);
}
