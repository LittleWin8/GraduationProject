package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.dto.AdminTagQueryDTO;
import com.littlewin.note.domain.entity.NoteTag;
import com.littlewin.note.domain.vo.AdminTagVO;
import com.littlewin.note.domain.vo.TagNoteVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {
    /**
     * 查询用户的所有标签，并统计每个标签关联的笔记数量
     */
    List<NoteTag> selectTagListWithCount(Long userId);

    /**
     * 分页查询标签下的笔记列表
     */
    IPage<TagNoteVO> selectNotesByTag(Page<TagNoteVO> page,
                                      @Param("tagId") Long tagId,
                                      @Param("userId") Long userId);

    IPage<AdminTagVO> selectAdminTagPage(Page<AdminTagVO> page, @Param("query") AdminTagQueryDTO query);

    int deleteTagRelByTagId(@Param("tagId") Long tagId);

    int deleteTagById(@Param("tagId") Long tagId);
}