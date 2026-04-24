package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.littlewin.note.domain.entity.NoteTag;
import com.littlewin.note.mapper.NoteTagMapper;
import com.littlewin.note.service.NoteTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NoteTagServiceImpl implements NoteTagService {

    private final NoteTagMapper noteTagMapper;
    @Override
    public List<NoteTag> listMyTags(Long userId) {
        return noteTagMapper.selectTagListWithCount(userId);
    }

    @Override
    public boolean saveTag(String name, Long userId) {
        // 使用 QueryWrapper 检查该用户下是否已有同名标签
        QueryWrapper<NoteTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.eq("user_id", userId);

        if (noteTagMapper.selectOne(queryWrapper) != null) {
            return false; // 已存在
        }

        NoteTag tag = new NoteTag();
        tag.setName(name);
        tag.setUserId(userId);
        return noteTagMapper.insert(tag) > 0;
    }

    @Override
    public void removeTag(Long tagId) {
        noteTagMapper.deleteById(tagId);
    }
}
