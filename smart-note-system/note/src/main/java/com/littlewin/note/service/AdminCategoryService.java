package com.littlewin.note.service;

import com.littlewin.note.domain.dto.CategoryDTO;
import com.littlewin.note.domain.entity.SysCategory;

import java.util.List;

public interface AdminCategoryService {

    List<SysCategory> listCategories(Long parentId, Integer status);

    void addCategory(CategoryDTO dto);

    void updateCategory(Long categoryId, CategoryDTO dto);

    void deleteCategory(Long categoryId);

    void toggleStatus(Long categoryId);
}
