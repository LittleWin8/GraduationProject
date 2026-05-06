# Day 17 提示词：审核自动发送系统通知

```
在 AdminNoteServiceImpl.auditNote 方法中，审核笔记后自动向笔记作者发送系统通知。

⚠️ 设计决策：
- 上架（status=1）→ 发送 type=3（审核通过）通知
- 下架（status=3）→ 发送 type=4（审核不通过）通知
- 通知 sender_id = 当前管理员 userId
- 通知关联 noteId，用户点击可跳转笔记详情
- 参考 AdminNotificationServiceImpl 中 MessageService.sendMessage 的调用方式

### 修改文件：AdminNoteServiceImpl.java

(1) 新增注入：
private final MessageService messageService;
private final NoteMapper noteMapper;  // 已有，用于查询笔记作者

(2) 修改 auditNote 方法，在 noteMapper.auditNote 之后添加通知逻辑：

@Override
public void auditNote(Long noteId, Integer status) {
    if (status == null || (status != 1 && status != 3)) {
        throw new ServiceException("状态只能是 1（上架）或 3（下架）");
    }

    // 查询笔记信息（获取作者 ID）
    Note note = noteMapper.selectById(noteId);
    if (note == null || note.getDelFlag() == 1) {
        throw new ServiceException("笔记不存在或已删除");
    }

    // 执行审核
    int rows = noteMapper.auditNote(noteId, status);
    if (rows == 0) {
        throw new ServiceException("笔记不存在或已删除");
    }

    // 自动发送系统通知给笔记作者
    Long adminUserId = SecurityUtils.getLoginUser().getUserId();
    if (status == 1) {
        // 上架 → 审核通过
        messageService.sendMessage(
                note.getUserId(), adminUserId, noteId, null,
                3, "审核通过", "你的笔记「" + note.getTitle() + "」已通过审核，已上架展示"
        );
    } else {
        // 下架 → 审核不通过
        messageService.sendMessage(
                note.getUserId(), adminUserId, noteId, null,
                4, "审核不通过", "你的笔记「" + note.getTitle() + "」未通过审核，已被下架"
        );
    }
}

(3) 需要新增 import：
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.service.MessageService;

⚠️ 注意：
- sendMessage 参数：receiverId=笔记作者, senderId=管理员, noteId, commentId=null, type, title, content
- type=3 审核通过，type=4 审核不通过（与 user_message.type 定义一致）
- title 字段用于小程序系统通知列表展示（getNoticeTitle 优先用 item.title）
- 不给自己发通知（管理员自己发布的笔记审核时不发，但当前逻辑不做此判断，因为管理员一般不发布笔记）

验证：
1. 管理端上架一篇笔记 → 笔记作者小程序收到"审核通过"系统通知
2. 管理端下架一篇笔记 → 笔记作者小程序收到"审核不通过"系统通知
3. 通知内容包含笔记标题
4. 点击通知跳转笔记详情（noteId 关联）
5. user_message 表 type=3/4 记录正确
```
