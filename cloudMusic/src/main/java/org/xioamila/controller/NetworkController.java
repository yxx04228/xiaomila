package org.xioamila.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xioamila.vo.Result;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/newWork")
public class NetworkController {

    @GetMapping("/health")
    public Result healthCheck() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("timestamp", System.currentTimeMillis());
        data.put("service", "music-service");
        data.put("version", "1.0.0");

        return Result.success("服务正常运行", data);
    }
}