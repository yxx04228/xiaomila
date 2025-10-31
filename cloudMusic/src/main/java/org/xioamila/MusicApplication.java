package org.xioamila;

import com.greatmap.modules.core.util.Func;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

@EnableAsync
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients({"org.xioamila"})
@SpringBootApplication
@MapperScan(value={"org.xioamila.**.mapper*"})
@Slf4j
public class MusicApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(MusicApplication.class, args);
        Environment env = application.getEnvironment();
        //程序启动时间(秒)
        Long time = (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000;
        log.info("\n---------------------------------------------------------------------------------\n\t" +
                        "应用 '{}' 运行成功! 耗时 {} 秒 访问链接:\n\t" +
                        "Swagger文档: http://{}:{}{}/doc.html\n" +
                        "---------------------------------------------------------------------------------",
                env.getProperty("spring.application.name"), time,
                InetAddress.getLocalHost().getHostAddress(), env.getProperty("server.port"), Func.toStr(env.getProperty("server.servlet.context-path"), ""));
    }

}
