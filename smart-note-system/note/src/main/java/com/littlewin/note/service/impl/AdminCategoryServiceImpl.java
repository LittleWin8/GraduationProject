package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.TreeUtils;
import com.littlewin.note.domain.dto.CategoryDTO;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.entity.SysCategory;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.mapper.SysCategoryMapper;
import com.littlewin.note.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final SysCategoryMapper categoryMapper;
    private final NoteMapper noteMapper;

    @Override
    public List<SysCategory> listCategories(Long parentId) {
        if (parentId != null) {
            QueryWrapper<SysCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id", parentId)
                    .orderByAsc("sort_order");
            return categoryMapper.selectList(wrapper);
        }
        QueryWrapper<SysCategory> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order");
        List<SysCategory> all = categoryMapper.selectList(wrapper);
        return TreeUtils.build(all, 0L, Comparator.comparingInt(SysCategory::getSortOrder));
    }

    @Override
    public void addCategory(CategoryDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ServiceException("分类名称不能为空");
        }
        SysCategory category = SysCategory.builder()
                .name(dto.getName().trim())
                .parentId(dto.getParentId() != null ? dto.getParentId() : 0L)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status(1)
                .build();
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long categoryId, CategoryDTO dto) {
        SysCategory existing = categoryMapper.selectById(categoryId);
        if (existing == null) {
            throw new ServiceException("分类不存在");
        }
        if (dto.getParentId() != null && dto.getParentId().equals(categoryId)) {
            throw new ServiceException("不能将自己设为子分类");
        }
        SysCategory update = new SysCategory();
        update.setCategoryId(categoryId);
        if (dto.getName() != null) {
            update.setName(dto.getName().trim());
        }
        if (dto.getParentId() != null) {
            update.setParentId(dto.getParentId());
        }
        if (dto.getSortOrder() != null) {
            update.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            update.setStatus(dto.getStatus());
        }
        categoryMapper.updateById(update);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        SysCategory existing = categoryMapper.selectById(categoryId);
        if (existing == null) {
            throw new ServiceException("分类不存在");
        }
        QueryWrapper<SysCategory> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", categoryId);
        Long childCount = categoryMapper.selectCount(childWrapper);
        if (childCount > 0) {
            throw new ServiceException("该分类下有子分类，无法删除");
        }
        QueryWrapper<Note> noteWrapper = new QueryWrapper<>();
        noteWrapper.eq("category_id", categoryId)
                .eq("del_flag", 0);
        Long noteCount = noteMapper.selectCount(noteWrapper);
        if (noteCount > 0) {
            throw new ServiceException("该分类下有笔记关联，无法删除");
        }
        categoryMapper.deleteById(categoryId);
    }

    @Override
    public void toggleStatus(Long categoryId) {
        SysCategory existing = categoryMapper.selectById(categoryId);
        if (existing == null) {
            throw new ServiceException("分类不存在");
        }
        SysCategory update = new SysCategory();
        update.setCategoryId(categoryId);
        update.setStatus(existing.getStatus() == 1 ? 0 : 1);
        categoryMapper.updateById(update);
    }
}
