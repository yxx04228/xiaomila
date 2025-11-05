package org.xioamila.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "通用返回结果")
public class Result<T> {

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "返回消息")
    private String message;

    @Schema(description = "返回数据")
    private T data;

    // 成功状态码常量
    public static final int SUCCESS_CODE = 200;
    // 常见错误状态码
    public static final int ERROR_CODE = 500;
    public static final int BAD_REQUEST_CODE = 400;
    public static final int UNAUTHORIZED_CODE = 401;
    public static final int FORBIDDEN_CODE = 403;
    public static final int NOT_FOUND_CODE = 404;

    // 智能数据包装方法
    public static <T> Result<T> data(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setSuccess(true);

        if (data instanceof Boolean) {
            result.setMessage((Boolean) data ? "操作成功" : "操作失败");
        } else if (data == null) {
            result.setMessage("未找到数据");
        } else {
            result.setMessage("操作成功");
        }

        result.setData(data);
        return result;
    }

    // 成功静态方法 - 带消息
    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    // 成功静态方法 - 带消息和数据
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 失败静态方法 - 默认错误码
    public static <T> Result<T> error(String message) {
        return error(ERROR_CODE, message);
    }

    // 失败静态方法 - 指定错误码
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    // 常用错误类型快捷方法
    public static <T> Result<T> badRequest(String message) {
        return error(BAD_REQUEST_CODE, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return error(UNAUTHORIZED_CODE, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return error(FORBIDDEN_CODE, message);
    }

    public static <T> Result<T> notFound(String message) {
        return error(NOT_FOUND_CODE, message);
    }
}