<!-- eslint-disable -->
<template>
  <el-drawer v-model="drawerVisible" :destroy-on-close="true" size="650px" title="笔记详情">
    <div v-if="drawerProps.row" class="note-detail">
      <h2 class="note-title">{{ drawerProps.row.title }}</h2>
      <div class="note-meta">
        <span
          ><el-icon><User /></el-icon>{{ drawerProps.row.author }}</span
        >
        <span
          ><el-icon><Clock /></el-icon>{{ drawerProps.row.updateTime }}</span
        >
        <el-tag v-if="drawerProps.row.isPublic === 1" type="success" size="small">公开</el-tag>
        <el-tag v-else type="info" size="small">私密</el-tag>
      </div>
      <el-divider />
      <div class="markdown-body" v-html="renderedContent"></div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts" name="NoteDrawer">
import { ref, computed } from "vue";
import { User, Clock } from "@element-plus/icons-vue";
import MarkdownIt from "markdown-it";
import hljs from "highlight.js";

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight(str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`;
      } catch (_) {}
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`;
  }
});

interface DrawerProps {
  row: {
    title: string;
    content: string;
    author: string;
    updateTime: string;
    isPublic: number;
  } | null;
}

const drawerVisible = ref(false);
const drawerProps = ref<DrawerProps>({ row: null });

const renderedContent = computed(() => {
  if (!drawerProps.value.row?.content) return "";
  return md.render(drawerProps.value.row.content);
});

const acceptParams = (params: DrawerProps) => {
  drawerProps.value = params;
  drawerVisible.value = true;
};

defineExpose({ acceptParams });
</script>

<style scoped lang="scss">
.note-detail {
  padding: 0 4px;
  .note-title {
    margin: 0 0 16px;
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    line-height: 1.4;
  }
  .note-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 13px;
    color: #909399;
    .el-icon {
      margin-right: 4px;
      vertical-align: -1px;
    }
  }
}

.markdown-body {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  word-break: break-word;

  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4),
  :deep(h5),
  :deep(h6) {
    margin: 1.2em 0 0.6em;
    font-weight: 600;
    color: #1d2129;
  }
  :deep(h1) {
    font-size: 1.6em;
  }
  :deep(h2) {
    font-size: 1.4em;
    border-bottom: 1px solid #e5e6eb;
    padding-bottom: 6px;
  }
  :deep(h3) {
    font-size: 1.2em;
  }

  :deep(p) {
    margin: 0.8em 0;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 2em;
    margin: 0.6em 0;
  }
  :deep(li) {
    margin: 4px 0;
  }

  :deep(blockquote) {
    margin: 1em 0;
    padding: 8px 16px;
    border-left: 4px solid #409eff;
    background: #f4f5f7;
    color: #606266;
    border-radius: 0 4px 4px 0;
  }

  :deep(code) {
    padding: 2px 6px;
    font-size: 0.9em;
    background: #f2f3f5;
    border-radius: 3px;
    color: #c7254e;
  }

  :deep(pre) {
    margin: 1em 0;
    border-radius: 6px;
    overflow-x: auto;
    &.hljs {
      padding: 16px;
    }
    code {
      display: block;
      padding: 0;
      background: transparent;
      color: inherit;
      font-size: 13px;
      line-height: 1.6;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 1em 0;
    th,
    td {
      border: 1px solid #e5e6eb;
      padding: 8px 12px;
      text-align: left;
    }
    th {
      background: #f2f3f5;
      font-weight: 600;
    }
    tr:nth-child(even) td {
      background: #fafafa;
    }
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 4px;
    margin: 8px 0;
  }

  :deep(hr) {
    border: none;
    border-top: 1px solid #e5e6eb;
    margin: 1.5em 0;
  }

  :deep(a) {
    color: #409eff;
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
