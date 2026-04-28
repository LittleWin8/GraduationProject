package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.dto.NoteCreateDTO;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.domain.vo.NoteListVO;

import java.util.Map;

public interface NoteDetailService {
    /**
     * 获取笔记详情（公开笔记或本人笔记）
     */
    NoteDetailVO getNoteDetail(Long noteId, Long userId);

    /**
     * 创建笔记（含标签关联）
     *
     * @param dto    笔记创建请求体
     * @param userId 当前登录用户ID
     * @return 包含 noteId 和 createTime 的结果
     */
    Map<String, Object> createNote(NoteCreateDTO dto, Long userId);

    /**
     * 分页查询笔记列表（公开社区 / 我的笔记）
     *
     * @param userId  当前登录用户ID
     * @param type    查询类型：public-公开社区，my-我的笔记
     * @param queryDTO 查询条件（分页、筛选等）
     * @return 笔记列表分页结果
     */
    IPage<NoteListVO> listNotes(Long userId, String type, NoteQueryDTO queryDTO);

    /**
     * 更新笔记（含标签关联替换）
     *
     * @param noteId 笔记ID
     * @param dto    更新请求体（title、content、categoryId、isPublic、tagIds）
     * @param userId 当前登录用户ID
     */
    void updateNote(Long noteId, NoteCreateDTO dto, Long userId);

    /**
     * 删除笔记
     *
     * @param noteId    笔记ID
     * @param permanent true-永久删除(del_flag=1)，false-移入回收站(status=2)
     * @param userId    当前登录用户ID
     */
    void deleteNote(Long noteId, boolean permanent, Long userId);

    /**
     * 恢复笔记（从回收站恢复为正常状态）
     *
     * @param noteId 笔记ID
     * @param userId 当前登录用户ID
     */
    void restoreNote(Long noteId, Long userId);
}
