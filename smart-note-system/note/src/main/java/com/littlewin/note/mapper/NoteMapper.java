package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.dto.AdminNoteQueryDTO;
import com.littlewin.note.domain.dto.NoteCreateDTO;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.vo.AdminNoteVO;
import com.littlewin.note.domain.vo.MyNoteVO;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.domain.vo.NoteListVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NoteMapper extends BaseMapper<Note> {

    IPage<MyNoteVO> selectMyNoteVOPage(Page<Note> page,
                                       @Param("userId") Long userId,
                                       @Param("query") NoteQueryDTO query,
                                       @Param("categoryIds") List<Long> categoryIds);

    NoteDetailVO selectNoteDetailById(@Param("noteId") Long noteId,
                                      @Param("userId") Long userId);

    /**
     * 分页查询笔记列表（公开社区 / 我的笔记）
     *
     * @param page         分页对象
     * @param userId       当前登录用户ID
     * @param type         查询类型：public-公开社区，my-我的笔记
     * @param query        查询条件
     * @param categoryIds  分类ID列表（含子分类）
     * @return 笔记列表分页结果
     */
    IPage<NoteListVO> selectNoteListPage(Page<NoteListVO> page,
                                         @Param("userId") Long userId,
                                         @Param("type") String type,
                                         @Param("query") NoteQueryDTO query,
                                         @Param("categoryIds") List<Long> categoryIds);

    /**
     * 选择性更新笔记字段（含归属校验）
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（防越权）
     * @param dto    更新内容
     * @return 影响行数
     */
    int updateNoteById(@Param("noteId") Long noteId,
                       @Param("userId") Long userId,
                       @Param("dto") NoteCreateDTO dto);

    /**
     * 将笔记移入回收站（status → 2）
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID
     * @return 影响行数
     */
    int moveToRecycleBin(@Param("noteId") Long noteId,
                         @Param("userId") Long userId);

    /**
     * 从回收站恢复笔记（status → 1）
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID
     * @return 影响行数
     */
    int restoreFromRecycleBin(@Param("noteId") Long noteId,
                              @Param("userId") Long userId);

    /**
     * 永久删除笔记（del_flag → 1）
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID
     * @return 影响行数
     */
    int permanentDeleteById(@Param("noteId") Long noteId,
                            @Param("userId") Long userId);

    IPage<AdminNoteVO> selectAdminNotePage(Page<AdminNoteVO> page,
                                           @Param("query") AdminNoteQueryDTO query);

    NoteDetailVO selectAdminNoteDetailById(@Param("noteId") Long noteId);

    int auditNote(@Param("noteId") Long noteId,
                  @Param("status") Integer status);

    int adminForceDelete(@Param("noteId") Long noteId);

    void incrementViewCount(@Param("noteId") Long noteId);
}