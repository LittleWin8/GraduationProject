package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.entity.UserMessage;
import com.littlewin.note.domain.vo.MessageVO;
import org.apache.ibatis.annotations.Param;

/**
 * 站内消息Mapper
 */
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    /** 分页查询消息列表（关联sys_user和note） */
    IPage<MessageVO> selectMessagePage(Page<MessageVO> page,
                                       @Param("receiverId") Long receiverId);

    /** 查询未读消息数 */
    int countUnread(@Param("receiverId") Long receiverId);

    /** 全部标记已读 */
    int markAllRead(@Param("receiverId") Long receiverId);

    /** 批量标记已读（按ID列表） */
    int markReadByIds(@Param("ids") java.util.List<Long> ids);
}
