package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.dto.AdminTagQueryDTO;
import com.littlewin.note.domain.vo.AdminTagVO;

public interface AdminTagService {

    IPage<AdminTagVO> listTags(AdminTagQueryDTO queryDTO);

    void deleteTag(Long tagId);
}
