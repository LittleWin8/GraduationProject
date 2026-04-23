package com.littlewin.note.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteStatsVO {

    private Integer notes;

    private Integer likes;

    private Integer favorites;
}