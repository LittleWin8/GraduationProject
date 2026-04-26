package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.entity.NoteTag;
import com.littlewin.note.domain.entity.NoteTagRel;
import com.littlewin.note.mapper.NoteTagMapper;
import com.littlewin.note.mapper.NoteTagRelMapper;
import com.littlewin.note.service.NoteTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class NoteTagServiceImpl implements NoteTagService {

    private final NoteTagMapper noteTagMapper;
    private final NoteTagRelMapper noteTagRelMapper;
    @Override
    public List<NoteTag> listMyTags(Long userId) {
        return noteTagMapper.selectTagListWithCount(userId);
    }

    @Override
    public NoteTag saveTag(String name, Long userId) {
        if (name.length() > 50) {
            throw new ServiceException("标签名称不能超过50个字符");
        }
        // 使用 QueryWrapper 检查该用户下是否已有同名标签
        QueryWrapper<NoteTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.eq("user_id", userId);

        if (noteTagMapper.selectOne(queryWrapper) != null) {
            throw new ServiceException("标签已存在");
        }

        NoteTag tag = new NoteTag();
        tag.setName(name);
        tag.setUserId(userId);
        tag.setCreateTime(LocalDateTime.now()); 

        try {
            int rows = noteTagMapper.insert(tag);
            if (rows <= 0) {
                throw new ServiceException("创建标签失败");
            }
        } catch (DuplicateKeyException e) {
            // 数据库唯一约束兜底（并发场景）
            throw new ServiceException("标签已存在");
        }
        return tag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTag(Long tagId, Long userId) {
        // 1) 先校验标签是否存在且归属于当前用户，防越权删除
        NoteTag tag = noteTagMapper.selectOne(new LambdaQueryWrapper<NoteTag>()
                .eq(NoteTag::getTagId, tagId)
                .eq(NoteTag::getUserId, userId));
        if (tag == null) {
            throw new ServiceException("标签不存在或无权限删除");
        }
        // 2) 先清理关联表，避免脏数据/外键冲突
        noteTagRelMapper.delete(new LambdaQueryWrapper<NoteTagRel>()
                .eq(NoteTagRel::getTagId, tagId));
        // 3) 再删除标签本体，并校验删除结果
        int rows = noteTagMapper.deleteById(tagId);
        if (rows <= 0) {
            throw new ServiceException("删除失败，请重试");
        }
    }
}
