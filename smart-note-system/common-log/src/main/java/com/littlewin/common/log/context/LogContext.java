package com.littlewin.common.log.context;

public class LogContext {
    private static final ThreadLocal<String> DESC_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Long> ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME_HOLDER = new ThreadLocal<>();

    public static void setDesc(String desc) { DESC_HOLDER.set(desc); }
    public static String getDesc() { return DESC_HOLDER.get(); }

    public static void setBusinessId(Long id) { ID_HOLDER.set(id); }
    public static Long getBusinessId() { return ID_HOLDER.get(); }

    public static void setUsername(String username) { USER_NAME_HOLDER.set(username); }
    public static String getUsername() { return USER_NAME_HOLDER.get(); }

    public static void clear() {
        DESC_HOLDER.remove();
        ID_HOLDER.remove();
        USER_NAME_HOLDER.remove();
    }
}