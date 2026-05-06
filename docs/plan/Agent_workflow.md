# Git Worktree 隔离 Agent 开发工作流

## 概述

本工作流利用 `git worktree` 创建物理隔离的工作区，让 AI Agent 在独立分支上自由开发，完全不影响主项目代码。适合 Monorepo 结构下的 Vibe Coding 场景。

## 工作流程图

┌─────────────────────┐          ┌───────────────┐       ┌─────────────────────┐
│ 第一阶段         │ ─────▶  │         第二阶段     │ ───────▶ │      第三阶段    │ ────────────▶ │ 第四阶段
│ 开辟平行宇宙 │                     │ Agent 独立干活 │                          │ 成果验收合并 │                                        │ 清理回收
└─────────────────────┘          └───────────────┘       └─────────────────────┘ 



---

## 第一阶段：开辟平行宇宙（初始化）

在主项目目录下执行：

```bash
# 创建物理隔离的工作区文件夹，并绑定到一个新分支
git worktree add ../GraduationProject_Agent -b agent-vibe-branch
```

**参数说明：**

- `../GraduationProject_Agent`：在当前项目的上一级创建新文件夹
- `-b agent-vibe-branch`：创建一个新分支，Agent 的所有改动都在这个分支上

**执行后的目录结构：**

```text
~/code/
├── GraduationProject/        # 主项目（稳定分支）
└── GraduationProject_Agent/  # Agent 工作区（agent-vibe-branch）
```



------

## 第二阶段：Agent 独立干活（Vibe Coding 状态）

### 2.1 双开窗口

用 Cursor（或 VS Code）分别打开两个文件夹：

- **窗口 A**：主项目（`GraduationProject/`）
- **窗口 B**：Agent 工作区（`GraduationProject_Agent/`）

### 2.2 安装依赖（重要）

Agent 工作区是新的文件夹，需要重新安装依赖：

```bash
# 1. 进入 Agent 工作区
cd ../GraduationProject_Agent

# 2. 验证依赖是否能用（不需要重新安装！）
pnpm dev  # 或 npm run dev
# 如果能正常启动 → 什么都不用做，直接让 Agent 干活

# 3. 如果报错找不到依赖（极少发生），才需要重装
pnpm install
```



### 2.3 给 Agent 下指令

在窗口 B 的 Cursor Composer 中对 Agent 说：

```text
"你就在这个工作区活动。现在帮我在 smart-note-system 里添加一个用户登录接口，包含：
- 控制器、服务层、数据层
- JWT 生成逻辑
- 参数校验

完成后告诉我改了哪些文件，不要 push，我会自己合并。"
```

### 2.4 Agent 工作期间

- Agent 可以随意修改代码、提交 commit、切换子分支
- 主项目窗口 A 完全不受影响，可以继续正常开发
- 两个窗口可以同时工作，互不干扰

------

## 第三阶段：成果验收与回收（合流）

### 3.1 Agent 工作区提交代码

在窗口 B（Agent 文件夹）执行：

```bash
cd ../GraduationProject_Agent

git add .
git commit -m "feat: agent-generated [功能描述]"
```



### 3.2 预览改动（安全检查）

切换到窗口 A（主项目文件夹）：

```bash
cd ../GraduationProject

# 拉取新分支信息
git fetch

# 查看改动概览（改了哪些文件，多少行）
git diff main..agent-vibe-branch --stat

# 查看详细改动内容
git diff main..agent-vibe-branch
```



### 3.3 合并代码

确认改动无误后：

```bash
# 确保在主分支（根据实际情况选择 main/master/dev）
git checkout main

# 合并 Agent 分支
git merge agent-vibe-branch
```

### 3.4 处理合并冲突（如有）

如果出现冲突：

```bash
# 合并时提示冲突
# CONFLICT (content): Merge conflict in xxx

# 1. 打开冲突文件，搜索 <<<<<<< HEAD
# 2. 手动保留需要的代码
# 3. 保存后执行：

git add .
git commit -m "merge: 合并 agent 分支，手动解决冲突"
```

### 3.5 只合并部分改动（可选）

如果只需要合并部分提交：

```bash
# 查看 Agent 分支的所有提交
git log agent-vibe-branch --oneline

# 挑出需要的单个提交
git cherry-pick <commit-hash>

# 或挑出一段连续的提交
git cherry-pick <start-hash>^..<end-hash>
```



### 3.6 推送远程

```bash
git push origin main
```



------

## 第四阶段：过河拆桥（清理）

任务完成后，清理临时工作区和分支：

```bash
cd ../GraduationProject

# 1. 删除 worktree（自动删除文件夹）
git worktree remove ../GraduationProject_Agent

# 2. 删除本地分支
git branch -d agent-vibe-branch

# 3. 如果推送过远程，删除远程分支（可选）
git push origin --delete agent-vibe-branch
```



------

## 常用命令速查表

| 命令                                | 说明               |
| :---------------------------------- | :----------------- |
| `git worktree add ../xxx -b branch` | 创建新的 worktree  |
| `git worktree list`                 | 查看所有 worktree  |
| `git worktree remove ../xxx`        | 删除 worktree      |
| `git worktree prune`                | 清理 worktree 引用 |
| `git diff main..branch --stat`      | 预览两个分支的差异 |
| `git merge branch`                  | 合并分支           |
| `git cherry-pick <hash>`            | 挑选单个提交合并   |
| `git branch -d branch`              | 删除本地分支       |
| `git push origin --delete branch`   | 删除远程分支       |

------

## 注意事项

### ✅ 推荐做法

1. **提交信息加上 scope 标签**，便于区分修改了哪个模块

   bash

   ```
   git commit -m "feat(backend): 添加登录接口"
   git commit -m "fix(mp): 修复页面样式"
   ```

   

2. **合并前预览改动**，确认无误再合并

3. **Agent 工作区定期提交**，避免一次性改动过大难以 Review

### ❌ 避免操作

1. **不要在两个 worktree 中同时操作同一个分支**
2. **不要直接删除 worktree 文件夹**，用 `git worktree remove` 命令
3. **不要在生产环境主分支上让 Agent 直接操作**

------

## 适用场景

| 场景                      | 是否适用   |
| :------------------------ | :--------- |
| Monorepo 多项目开发       | ✅ 非常适合 |
| 需要同时开发多个功能      | ✅ 适合     |
| 修紧急 Bug 同时开发新功能 | ✅ 适合     |
| 个人项目 Vibe Coding      | ✅ 适合     |
| 简单单分支开发            | ❌ 过度设计 |

------

## 版本记录

| 版本 | 日期       | 说明     |
| :--- | :--------- | :------- |
| v1.0 | 2026-04-28 | 初始版本 |