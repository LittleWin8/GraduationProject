package com.littlewin.note.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞/收藏切换结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionResultVO {

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

    /** 当前用户是否已收藏 */
    private Boolean isCollected;

    /** 该笔记点赞总数 */
    private Integer likeCount;

    /** 该笔记收藏总数 */
    private Integer collectCount;
}
