package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.note.domain.entity.NoteReaction;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface NoteReactionMapper extends BaseMapper<NoteReaction> {

    /**
     * 统计用户笔记收到的总点赞数
     * @param userId 用户ID
     * @return 点赞总数
     */
    @Select("SELECT COUNT(*) FROM note_reaction nr " +
            "INNER JOIN note n ON nr.note_id = n.note_id " +
            "WHERE n.user_id = #{userId} " +
            "AND nr.attitude = 1")
    Long countLikesByUserNotes(@Param("userId") Long userId);
}