-- 这里的用户主键需要改成自己的测试的小程序用户主键（user_id）

-- 1. 插入笔记数据（3条）
INSERT INTO note (note_id, user_id, category_id, title, content, is_public, status, view_count) VALUES
(1001, 114, 3, 'Vue3 组合式API详解', '# Vue3 组合式API\n\n## 响应式 API\n- ref()\n- reactive()\n- computed()', 1, 1, 128),
(1002, 114, 4, '微信小程序开发踩坑记录', '# 踩坑总结\n\n1. 样式隔离问题\n2. 生命周期执行顺序\n3. 分包加载限制', 1, 1, 89),
(1003, 114, 5, 'TypeScript 泛型使用指南', '# 泛型基础\n\n```typescript\nfunction identity<T>(arg: T): T {\n  return arg\n}\n```', 1, 1, 56);

-- 2. 插入用户标签
INSERT INTO note_tag (tag_id, name, user_id) VALUES
(1001, 'Vue', 114),
(1002, '小程序', 114),
(1003, 'TypeScript', 114),
(1004, '前端', 114);

-- 3. 关联笔记与标签
INSERT INTO note_tag_rel (note_id, tag_id) VALUES
(1001, 1001),  -- Vue3笔记 → Vue标签
(1001, 1004),  -- Vue3笔记 → 前端标签
(1002, 1002),  -- 小程序笔记 → 小程序标签
(1002, 1004),  -- 小程序笔记 → 前端标签
(1003, 1003),  -- TS笔记 → TypeScript标签
(1003, 1004);  -- TS笔记 → 前端标签

-- 4. 插入互动数据（点赞 + 收藏）
INSERT INTO note_reaction (note_id, user_id, attitude, is_favorite) VALUES
(1001, 114, 1, 1),  -- 点赞并收藏了自己的笔记
(1002, 114, 1, 1),  -- 点赞并收藏
(1003, 114, 1, 1);  -- 点赞并收藏