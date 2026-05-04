package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.dto.AiUserQuotaQueryDTO;
import com.littlewin.note.domain.entity.AiUserQuota;
import com.littlewin.note.domain.vo.AiUserQuotaVO;
import org.apache.ibatis.annotations.Param;

public interface AiUserQuotaMapper extends BaseMapper<AiUserQuota> {

    IPage<AiUserQuotaVO> selectQuotaPage(Page<AiUserQuotaVO> page, @Param("query") AiUserQuotaQueryDTO query);
}
