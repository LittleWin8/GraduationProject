package com.littlewin.common.core;

import java.util.HashMap;

public class AjaxResult extends HashMap<String, Object> {

    public static AjaxResult success() {
        AjaxResult r = new AjaxResult();
        r.put("code", 200);
        r.put("msg", "success");
        return r;
    }

    public static AjaxResult success(Object data) {
        AjaxResult r = success();
        r.put("data", data);
        return r;
    }

    public static AjaxResult error(String msg) {
        AjaxResult r = new AjaxResult();
        r.put("code", 500);
        r.put("msg", msg);
        return r;
    }
}
