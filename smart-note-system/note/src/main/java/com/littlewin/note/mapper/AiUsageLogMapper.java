package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.dto.AiUsageLogQueryDTO;
import com.littlewin.note.domain.entity.AiUsageLog;
import com.littlewin.note.domain.vo.AiUsageLogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AiUsageLogMapper extends BaseMapper<AiUsageLog> {

    int sumTokensByUserThisMonth(@Param("userId") Long userId);

    int countRequestsByUserThisMonth(@Param("userId") Long userId);

    IPage<AiUsageLogVO> selectLogPage(Page<AiUsageLogVO> page, @Param("query") AiUsageLogQueryDTO query);

    Map<String, Object> selectGlobalStats();

    List<Map<String, Object>> selectUserRanking(@Param("limit") int limit,
                                                @Param("startTime") String startTime,
                                                @Param("endTime") String endTime);
}
