package com.littlewin.note.task;

import com.littlewin.common.constants.RedisKeyConstants;
import com.littlewin.note.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteViewSyncTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final NoteMapper noteMapper;

    private static final String LUA_GETSET =
            "local val = redis.call('GETSET', KEYS[1], '0') " +
            "return val";

    @Scheduled(fixedRate = 300000)
    public void syncViewCountToDb() {
        try {
            Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                Set<String> result = new HashSet<>();
                ScanOptions options = ScanOptions.scanOptions()
                        .match(RedisKeyConstants.NOTE_VIEWS + "*")
                        .count(100)
                        .build();
                Cursor<byte[]> cursor = connection.scan(options);
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
                return result;
            });

            if (keys == null || keys.isEmpty()) return;

        DefaultRedisScript<String> script = new DefaultRedisScript<>(LUA_GETSET, String.class);
        Map<Long, Long> viewCountMap = new HashMap<>();

        for (String key : keys) {
            String noteIdStr = key.substring(RedisKeyConstants.NOTE_VIEWS.length());
            try {
                String val = stringRedisTemplate.execute(script, List.of(key));
                if (val != null) {
                    long count = Long.parseLong(val);
                    if (count > 0) {
                        viewCountMap.put(Long.parseLong(noteIdStr), count);
                    }
                }
            } catch (Exception e) {
                log.warn("同步浏览量失败: key={}, error={}", key, e.getMessage());
            }
        }

        for (Map.Entry<Long, Long> entry : viewCountMap.entrySet()) {
            try {
                noteMapper.addViewCount(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.warn("写入 DB 浏览量失败: noteId={}, count={}, error={}",
                        entry.getKey(), entry.getValue(), e.getMessage());
            }
        }

        if (!viewCountMap.isEmpty()) {
            log.info("浏览量同步完成，共 {} 条笔记", viewCountMap.size());
        }
        } catch (Exception e) {
            log.warn("浏览量同步任务执行失败（Redis 可能未连接）: {}", e.getMessage());
        }
    }
}
