package com.littlewin.note.service;

import com.littlewin.note.domain.vo.InteractionResultVO;
import com.littlewin.note.domain.vo.InteractionStatusVO;

import java.util.List;
import java.util.Map;

/**
 * 互动服务接口（点赞/收藏切换、状态查询）
 */
public interface InteractionService {

    /** 点赞或收藏切换，返回切换后完整状态 */
    InteractionResultVO toggle(Long userId, Long noteId, String type);

    /** 查询单条笔记的互动状态 */
    InteractionStatusVO getStatus(Long userId, Long noteId);

    /** 批量查询多条笔记的互动状态，key 为 noteId 字符串 */
    Map<String, InteractionStatusVO> batchGetStatus(Long userId, List<Long> noteIds);
}
