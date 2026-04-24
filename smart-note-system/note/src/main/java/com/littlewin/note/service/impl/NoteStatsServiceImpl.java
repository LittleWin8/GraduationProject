package com.littlewin.note.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.utils.TreeUtils;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.entity.NoteReaction;
import com.littlewin.note.domain.entity.SysCategory;
import com.littlewin.note.domain.vo.*;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.mapper.NoteReactionMapper;
import com.littlewin.note.mapper.NoteTagRelMapper;
import com.littlewin.note.mapper.SysCategoryMapper;
import com.littlewin.note.service.NoteStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteStatsServiceImpl implements NoteStatsService {

    private final NoteMapper noteMapper;
    private final NoteReactionMapper noteReactionMapper;
    private final NoteTagRelMapper noteTagRelMapper;
    private final SysCategoryMapper sysCategoryMapper;

    @Override
    public NoteStatsVO getUserStats(Long userId) {
        // 1. 查询笔记数量 (MP基本操作保留Lambda)
        Long notesCount = noteMapper.selectCount(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .eq(Note::getStatus, 1)
                .eq(Note::getDelFlag, 0));

        // 2. 查询获赞数量 (调用XML自定义SQL)
        Long likesCount = noteReactionMapper.countLikesByUserNotes(userId);

        // 3. 查询收藏数量
        Long favoritesCount = noteReactionMapper.selectCount(new LambdaQueryWrapper<NoteReaction>()
                .eq(NoteReaction::getUserId, userId)
                .eq(NoteReaction::getIsFavorite, 1));

        return NoteStatsVO.builder()
                .notes(notesCount.intValue())
                .likes(likesCount.intValue())
                .favorites(favoritesCount.intValue())
                .build();
    }

    @Override
    public IPage<MyNoteVO> queryMyNotes(Long userId, NoteQueryDTO queryDTO) {
        Page<Note> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 处理分类递归查询
        List<Long> categoryIds = null;
        if (queryDTO.getCategoryId() != null) {
            categoryIds = getAllCategoryIds(queryDTO.getCategoryId());
        }

        // 调用 XML 实现复杂分页查询
        IPage<MyNoteVO> resultPage = noteMapper.selectMyNoteVOPage(page, userId, queryDTO, categoryIds);
        return resultPage;
    }

    @Override
    public IPage<FavoriteNoteVO> queryFavorites(Long userId, NoteQueryDTO queryDTO) {
        Page<FavoriteNoteVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        List<Long> categoryIds = null;
        if (queryDTO.getCategoryId() != null) {
            categoryIds = getAllCategoryIds(queryDTO.getCategoryId());
        }

        return noteReactionMapper.selectFavoriteNotePage(page, userId, queryDTO, categoryIds);
    }

    @Override
    public IPage<LikedNoteVO> queryLiked(Long userId, NoteQueryDTO queryDTO) {
        Page<LikedNoteVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        List<Long> categoryIds = null;
        if (queryDTO.getCategoryId() != null) {
            categoryIds = getAllCategoryIds(queryDTO.getCategoryId());
        }

        return noteReactionMapper.selectLikedNotePage(page, userId, queryDTO, categoryIds);
    }

    // ==================== 私有辅助方法 ====================

    private List<Long> getAllCategoryIds(Long categoryId) {
        // 1. 一次性查出所有分类（或按需过滤），避免循环查库
        List<SysCategory> allCategories = sysCategoryMapper.selectList(null);

        // 2. 调用工具类直接获取所有子 ID
        // 参数：全量列表, 目标起始ID, 是否包含自身
        return TreeUtils.findAllChildIds(allCategories, categoryId, true);
    }
}