package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.vo.FavoriteNoteVO;
import com.littlewin.note.domain.vo.LikedNoteVO;
import com.littlewin.note.domain.vo.MyNoteVO;
import com.littlewin.note.domain.vo.NoteStatsVO;

public interface NoteStatsService {

    /**
     * 获取用户统计数据
     * @param userId 用户ID
     * @return 统计数据
     */
    NoteStatsVO getUserStats(Long userId);

    /**
     * 获取我的笔记列表（分页）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    IPage<MyNoteVO> getMyNotes(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户收藏的笔记列表（分页）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    IPage<FavoriteNoteVO> getFavorites(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户点赞的笔记列表（分页）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    IPage<LikedNoteVO> getLiked(Long userId, Integer pageNum, Integer pageSize);
}