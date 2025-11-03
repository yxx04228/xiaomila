package org.xioamila.common.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;
import org.xioamila.common.utils.SecurityUtil;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * 审计字段自动填充拦截器
 * 自动维护 create_time, create_by, update_time, update_by 字段
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class AuditInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];
        System.out.println("parameter: " + parameter);
        // 获取当前用户信息
        String currentUserId = SecurityUtil.getUserId();
        Date now = new Date();

        // 处理插入操作
        if (sqlCommandType == SqlCommandType.INSERT) {
            handleInsert(parameter, currentUserId, now);
        }

        // 处理更新操作
        if (sqlCommandType == SqlCommandType.UPDATE) {
            handleUpdate(parameter, currentUserId, now);
        }

        return invocation.proceed();
    }

    /**
     * 处理插入操作的字段填充
     */
    private void handleInsert(Object parameter, String userId, Date now) {
        if (parameter == null) return;

        // 直接处理所有实体对象
        processEntityFields(parameter, true, userId, now);
    }

    /**
     * 处理更新操作的字段填充
     */
    private void handleUpdate(Object parameter, String userId, Date now) {
        if (parameter == null) return;

        // 直接处理所有实体对象
        processEntityFields(parameter, false, userId, now);
    }

    /**
     * 处理实体字段的通用方法
     * @param parameter 参数对象
     * @param isInsert 是否为插入操作
     * @param userId 用户ID
     * @param now 当前时间
     */
    private void processEntityFields(Object parameter, boolean isInsert, String userId, Date now) {
        try {
            // 1. 如果是Map类型，遍历所有值
            if (parameter instanceof Map) {
                Map<?, ?> paramMap = (Map<?, ?>) parameter;
                for (Object value : paramMap.values()) {
                    if (value != null && isEntityObject(value)) {
                        setAuditFields(value, isInsert, userId, now);
                    }
                }
                return;
            }

            // 2. 直接处理实体对象
            if (isEntityObject(parameter)) {
                setAuditFields(parameter, isInsert, userId, now);
            }

        } catch (Exception e) {
            System.out.println("处理审计字段时发生异常: " + e.getMessage());
        }
    }

    /**
     * 判断是否为实体对象（有我们需要的审计字段）
     */
    private boolean isEntityObject(Object obj) {
        if (obj == null) return false;

        Class<?> clazz = obj.getClass();
        try {
            // 检查是否包含审计字段
            return hasField(clazz, "createTime") &&
                    hasField(clazz, "createBy") &&
                    hasField(clazz, "updateTime") &&
                    hasField(clazz, "updateBy");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置审计字段
     */
    private void setAuditFields(Object entity, boolean isInsert, String userId, Date now) {
        try {
            Class<?> clazz = entity.getClass();

            if (isInsert) {
                // 插入操作：设置创建和更新字段
                setFieldValue(entity, clazz, "createTime", now);
                setFieldValue(entity, clazz, "createBy", userId);
                setFieldValue(entity, clazz, "updateTime", now);
                setFieldValue(entity, clazz, "updateBy", userId);
            } else {
                // 更新操作：只设置更新字段
                setFieldValue(entity, clazz, "updateTime", now);
                setFieldValue(entity, clazz, "updateBy", userId);
            }

        } catch (Exception e) {
            System.out.println("设置审计字段失败: " + e.getMessage());
        }
    }

    /**
     * 检查类是否有指定字段
     */
    private boolean hasField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field != null;
        } catch (NoSuchFieldException e) {
            // 检查父类
            if (clazz.getSuperclass() != null) {
                return hasField(clazz.getSuperclass(), fieldName);
            }
            return false;
        }
    }

    /**
     * 设置字段值
     */
    private void setFieldValue(Object obj, Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = getDeclaredField(clazz, fieldName);
            if (field != null) {
                field.setAccessible(true);
                Object currentValue = field.get(obj);

                field.set(obj, value);
            }
        } catch (Exception e) {
            // 忽略字段设置异常
        }
    }

    /**
     * 递归获取字段（包括父类）
     */
    private Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return getDeclaredField(clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以在这里配置属性
    }
}