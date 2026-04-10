package com.yuexiang.common.pojo;

import com.yuexiang.common.enums.ResultCodeEnum;

import java.io.Serializable;

public class CommonResult<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public CommonResult() {
    }

    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = ResultCodeEnum.SUCCESS.getCode();
        result.msg = ResultCodeEnum.SUCCESS.getMsg();
        result.data = data;
        return result;
    }

    public static CommonResult<Void> success() {
        return success(null);
    }

    public static <T> CommonResult<T> error(ResultCodeEnum errorCode) {
        CommonResult<T> result = new CommonResult<>();
        result.code = errorCode.getCode();
        result.msg = errorCode.getMsg();
        return result;
    }

    public static <T> CommonResult<T> error(int code, String msg) {
        CommonResult<T> result = new CommonResult<>();
        result.code = code;
        result.msg = msg;
        return result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
