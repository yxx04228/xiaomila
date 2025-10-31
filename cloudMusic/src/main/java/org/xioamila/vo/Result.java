package org.xioamila.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "通用返回结果")
public class Result<T> {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "返回消息")
    private String message;

    @Schema(description = "返回数据")
    private T data;

    // 智能数据包装方法
    public static <T> Result<T> data(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);

        if (data instanceof Boolean) {
            // 如果是布尔值，根据true/false设置不同消息
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
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    // 成功静态方法 - 带消息和数据
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 失败静态方法
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}