package com.littlewin.common.log.annotation;

import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    /** 模块名：AUTH, USER, DICT, AI 等 */
    LogModule module();

    /** 强制要求传入操作类型：1登录, 2退出, 3创建... */
    LogAction action();


    /** 静态描述（可选，动态描述优先） */
    String desc() default "";
}
