package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.TreeUtils;
import com.littlewin.note.domain.dto.NoteCreateDTO;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.entity.NoteTag;
import com.littlewin.note.domain.entity.NoteTagRel;
import com.littlewin.note.domain.entity.SysCategory;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.domain.vo.NoteListVO;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.mapper.NoteTagMapper;
import com.littlewin.note.mapper.NoteTagRelMapper;
import com.littlewin.note.mapper.SysCategoryMapper;
import com.littlewin.note.service.NoteDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteDetailServiceImpl implements NoteDetailService {

    private final NoteMapper noteMapper;
    private final NoteTagRelMapper noteTagRelMapper;
    private final NoteTagMapper noteTagMapper;
    private final SysCategoryMapper sysCategoryMapper;

    @Override
    public NoteDetailVO getNoteDetail(Long noteId, Long userId) {
        NoteDetailVO detail = noteMapper.selectNoteDetailById(noteId, userId);
        if (detail == null) {
            throw new ServiceException("笔记不存在或无权限访问");
        }
        noteMapper.incrementViewCount(noteId);
        // 查询笔记关联的标签ID列表
        List<NoteTagRel> relList = noteTagRelMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoteTagRel>()
                        .eq(NoteTagRel::getNoteId, noteId));
        if (relList != null && !relList.isEmpty()) {
            detail.setTagIds(relList.stream()
                    .map(NoteTagRel::getTagId)
                    .collect(Collectors.toList()));
        }
        return detail;
    }

    /**
     * 创建笔记（含标签关联）
     * 1. 参数校验（title、content 必填）
     * 2. 构建 Note 实体并插入
     * 3. 如果传了 tagIds，批量插入 note_tag_rel 关联表
     * 4. 返回 noteId 和 createTime
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createNote(NoteCreateDTO dto, Long userId) {
        // 1. 参数校验
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new ServiceException("笔记标题不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new ServiceException("笔记内容不能为空");
        }

        // 2. 构建笔记实体
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(dto.getTitle().trim());
        note.setContent(dto.getContent().trim());
        note.setCategoryId(dto.getCategoryId());
        note.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 1);
        note.setStatus(1);
        note.setViewCount(0);
        note.setDelFlag(0);
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());

        // 3. 插入笔记
        int rows = noteMapper.insert(note);
        if (rows <= 0) {
            throw new ServiceException("创建笔记失败");
        }

        // 4. 批量插入标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            noteTagRelMapper.batchInsert(note.getNoteId(), dto.getTagIds());
        }

        // 5. 返回 noteId 和 createTime
        Map<String, Object> result = new HashMap<>();
        result.put("noteId", note.getNoteId());
        result.put("createTime", note.getCreateTime());
        return result;
    }

    /**
     * 分页查询笔记列表（公开社区 / 我的笔记）
     * 1. 处理分类递归查询（如果传了 categoryId，则包含子分类）
     * 2. 调用 Mapper 执行分页查询
     * 3. 为每条笔记填充标签列表
     */
    @Override
    public IPage<NoteListVO> listNotes(Long userId, String type, NoteQueryDTO queryDTO) {
        // 1. 处理分类递归查询
        List<Long> categoryIds = null;
        if (queryDTO.getCategoryId() != null) {
            categoryIds = getAllCategoryIds(queryDTO.getCategoryId());
        }

        // 2. 执行分页查询
        Page<NoteListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<NoteListVO> resultPage = noteMapper.selectNoteListPage(page, userId, type, queryDTO, categoryIds);

        // 3. 为每条笔记填充标签列表
        List<NoteListVO> records = resultPage.getRecords();
        if (records != null && !records.isEmpty()) {
            List<Long> noteIds = records.stream()
                    .map(NoteListVO::getNoteId)
                    .collect(Collectors.toList());
            Map<Long, List<NoteListVO.TagItem>> tagMap = batchQueryTags(noteIds);
            for (NoteListVO vo : records) {
                vo.setTags(tagMap.getOrDefault(vo.getNoteId(), List.of()));
            }
        }

        return resultPage;
    }

    /**
     * 批量查询笔记关联的标签信息
     */
    private Map<Long, List<NoteListVO.TagItem>> batchQueryTags(List<Long> noteIds) {
        // 1. 查询所有关联关系
        List<NoteTagRel> relList = noteTagRelMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoteTagRel>()
                        .in(NoteTagRel::getNoteId, noteIds));
        if (relList.isEmpty()) {
            return Map.of();
        }

        // 2. 提取所有 tagId 并批量查询标签
        List<Long> tagIds = relList.stream()
                .map(NoteTagRel::getTagId)
                .distinct()
                .collect(Collectors.toList());
        List<NoteTag> tags = noteTagMapper.selectBatchIds(tagIds);
        Map<Long, String> tagNameMap = tags.stream()
                .collect(Collectors.toMap(NoteTag::getTagId, NoteTag::getName));

        // 3. 按 noteId 分组构建 TagItem
        return relList.stream()
                .collect(Collectors.groupingBy(
                        NoteTagRel::getNoteId,
                        Collectors.mapping(
                                rel -> NoteListVO.TagItem.builder()
                                        .tagId(rel.getTagId())
                                        .tagName(tagNameMap.getOrDefault(rel.getTagId(), ""))
                                        .build(),
                                Collectors.toList()
                        )
                ));
    }

    /**
     * 递归获取分类及所有子分类ID
     */
    private List<Long> getAllCategoryIds(Long categoryId) {
        List<SysCategory> allCategories = sysCategoryMapper.selectList(null);
        return TreeUtils.findAllChildIds(allCategories, categoryId, true);
    }

    /**
     * 更新笔记（含标签关联替换）
     * 1. 校验笔记存在且属于当前用户
     * 2. 执行选择性更新（title、content、categoryId、isPublic）
     * 3. 如果传了 tagIds，先删除旧关联再批量插入新关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNote(Long noteId, NoteCreateDTO dto, Long userId) {
        // 1. 校验笔记存在且属于当前用户
        checkNoteOwnership(noteId, userId);

        // 2. 执行选择性更新
        int rows = noteMapper.updateNoteById(noteId, userId, dto);
        if (rows <= 0) {
            throw new ServiceException("更新笔记失败");
        }

        // 3. 如果传了 tagIds，先删除旧关联再批量插入新关联
        if (dto.getTagIds() != null) {
            noteTagRelMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoteTagRel>()
                    .eq(NoteTagRel::getNoteId, noteId));
            if (!dto.getTagIds().isEmpty()) {
                noteTagRelMapper.batchInsert(noteId, dto.getTagIds());
            }
        }
    }

    /**
     * 删除笔记
     * 1. 校验笔记存在且属于当前用户
     * 2. permanent=false：逻辑删除，将 status 改为 2（回收站）
     * 3. permanent=true：将 del_flag 改为 1（永久删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long noteId, boolean permanent, Long userId) {
        // 1. 校验笔记存在且属于当前用户
        checkNoteOwnership(noteId, userId);

        // 2. 执行删除
        int rows;
        if (permanent) {
            rows = noteMapper.permanentDeleteById(noteId, userId);
        } else {
            rows = noteMapper.moveToRecycleBin(noteId, userId);
        }
        if (rows <= 0) {
            throw new ServiceException("删除笔记失败");
        }
    }

    /**
     * 恢复笔记（从回收站恢复为正常状态）
     * 1. 校验笔记存在且属于当前用户
     * 2. 将 status 从 2（回收站）改回 1（正常）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreNote(Long noteId, Long userId) {
        // 1. 校验笔记存在且属于当前用户
        checkNoteOwnership(noteId, userId);

        // 2. 恢复笔记状态
        int rows = noteMapper.restoreFromRecycleBin(noteId, userId);
        if (rows <= 0) {
            throw new ServiceException("恢复笔记失败，笔记可能不在回收站中");
        }
    }

    /**
     * 校验笔记存在且属于当前用户（防越权操作）
     */
    private void checkNoteOwnership(Long noteId, Long userId) {
        Note note = noteMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Note>()
                        .eq(Note::getNoteId, noteId)
                        .eq(Note::getUserId, userId)
                        .eq(Note::getDelFlag, 0));
        if (note == null) {
            throw new ServiceException("笔记不存在或无权限操作");
        }
    }
}
