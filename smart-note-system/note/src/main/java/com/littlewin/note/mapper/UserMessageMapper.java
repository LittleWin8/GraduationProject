package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.entity.UserMessage;
import com.littlewin.note.domain.vo.MessageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内消息Mapper
 */
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    /** 分页查询消息列表（关联sys_user和note） */
    IPage<MessageVO> selectMessagePage(Page<MessageVO> page,
                                       @Param("receiverId") Long receiverId);

    /** 按类型分页查询消息列表 */
    IPage<MessageVO> selectMessagePageByGroup(Page<MessageVO> page,
                                              @Param("receiverId") Long receiverId,
                                              @Param("types") List<Integer> types);

    /** 查询未读消息数 */
    int countUnread(@Param("receiverId") Long receiverId);

    /** 按类型组查询未读消息数 */
    int countUnreadGrouped(@Param("receiverId") Long receiverId,
                           @Param("types") List<Integer> types);

    /** 全部标记已读 */
    int markAllRead(@Param("receiverId") Long receiverId);

    /** 批量标记已读（按ID列表） */
    int markReadByIds(@Param("ids") List<Long> ids);

    /** 查询所有有效用户ID（未逻辑删除） */
    List<Long> selectAllUserIds();
}
