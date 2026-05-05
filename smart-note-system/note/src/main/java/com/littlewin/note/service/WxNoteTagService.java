package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.entity.NoteTag;
import com.littlewin.note.domain.vo.TagNoteVO;

import java.util.List;

public interface WxNoteTagService {
    /**
     * 获取当前用户的标签列表（含统计）
     */
    List<NoteTag> listMyTags(Long userId);

    /**
     * 创建标签（检查重名）
     */
    NoteTag saveTag(String name, Long userId);

    /**
     * 删除标签（MyBatis Plus 会自动处理）
     */
    void removeTag(Long tagId, Long userId);

    /**
     * 分页查询标签下的笔记
     */
    IPage<TagNoteVO> listNotesByTag(Long tagId, Long userId, long pageNum, long pageSize);
}
