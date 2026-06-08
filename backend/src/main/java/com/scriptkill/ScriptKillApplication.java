package com.scriptkill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.scriptkill.mapper")
public class ScriptKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScriptKillApplication.class, args);
        System.out.println("========================================");
        System.out.println("  剧本杀平台后端服务启动成功！");
        System.out.println("  访问地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
