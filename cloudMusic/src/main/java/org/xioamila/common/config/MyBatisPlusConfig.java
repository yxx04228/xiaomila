package org.xioamila.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 官方配置类：仅注册官方分页插件
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册官方分页插件（核心）
     * 仅添加 PaginationInnerInterceptor，不引入其他插件，确保纯净
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 配置MySQL分页插件（与数据库类型一致）
        PaginationInnerInterceptor paginationPlugin = new PaginationInnerInterceptor(DbType.MYSQL);

        // 可选配置（根据需求调整）
        paginationPlugin.setOverflow(false); // 不允许页数溢出（如总页数10，查第11页返回空）
        paginationPlugin.setMaxLimit(1000L); // 单页最大条数限制（防止恶意查询10万条，默认无限制）
        paginationPlugin.setOptimizeJoin(true); // 优化关联查询的COUNT语句（减少性能消耗）

        interceptor.addInnerInterceptor(paginationPlugin); // 仅添加官方分页插件
        return interceptor;
    }
}