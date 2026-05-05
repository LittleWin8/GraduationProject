package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.vo.FavoriteNoteVO;
import com.littlewin.note.domain.vo.LikedNoteVO;
import com.littlewin.note.domain.vo.MyNoteVO;
import com.littlewin.note.domain.vo.NoteStatsVO;

public interface WxNoteStatsService {

    /**
     * 获取用户统计数据
     */
    NoteStatsVO getUserStats(Long userId);

    /**
     * 查询我的笔记列表（支持搜索/筛选/排序）
     */
    IPage<MyNoteVO> queryMyNotes(Long userId, NoteQueryDTO queryDTO);

    /**
     * 查询我收藏的笔记列表（支持搜索/筛选/排序）
     */
    IPage<FavoriteNoteVO> queryFavorites(Long userId, NoteQueryDTO queryDTO);

    /**
     * 查询我点赞的笔记列表（支持搜索/筛选/排序）
     */
    IPage<LikedNoteVO> queryLiked(Long userId, NoteQueryDTO queryDTO);
}