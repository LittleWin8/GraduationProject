package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.dto.AdminTagQueryDTO;
import com.littlewin.note.domain.vo.AdminTagVO;
import com.littlewin.note.mapper.NoteTagMapper;
import com.littlewin.note.service.AdminTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private final NoteTagMapper noteTagMapper;

    @Override
    public IPage<AdminTagVO> listTags(AdminTagQueryDTO queryDTO) {
        Page<AdminTagVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return noteTagMapper.selectAdminTagPage(page, queryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        noteTagMapper.deleteTagRelByTagId(tagId);
        int rows = noteTagMapper.deleteTagById(tagId);
        if (rows == 0) {
            throw new ServiceException("标签不存在");
        }
    }
}
