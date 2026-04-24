package com.littlewin.note.service;

import com.littlewin.note.domain.entity.NoteTag;

import java.util.List;

public interface NoteTagService {
    /**
     * 获取当前用户的标签列表（含统计）
     */
    List<NoteTag> listMyTags(Long userId);

    /**
     * 创建标签（检查重名）
     */
    boolean saveTag(String name, Long userId);

    /**
     * 删除标签（MyBatis Plus 会自动处理）
     */
    void removeTag(Long tagId);
}
