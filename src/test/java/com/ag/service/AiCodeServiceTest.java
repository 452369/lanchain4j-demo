package com.ag.service;

import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeServiceTest {
    @Resource
    private AiCodeService aiCodeService;

    @Test
    public void test() {
        String result = aiCodeService.chat("你好");
        System.out.println(result);
    }

    @Test
    public void chatWithMemory() {
        String result = aiCodeService.chat("你好，我是gyk");
        System.out.println(result);
        String chat1 = aiCodeService.chat("我是谁");
        System.out.println(chat1);
    }

    @Test
    void chatReport() {
        String userMesssage = "怎么学习java，有哪些常见的面试题呢？";
        AiCodeService.Report report = aiCodeService.chatReport(userMesssage);
        System.out.println(report);
    }

    @Test
    void chatContentRetrieve() {
        Result<String> result = aiCodeService.chatWithRag("怎么学习java，有哪些常见的面试题呢？") ;
        System.out.println(result.sources());
        System.out.println(result.content());
    }
    @Test
    void chatWithTools() {
        String result = aiCodeService.chat("有哪些常见的计算机网络面试题") ;
        System.out.println(result);
    }

    @Test
    void chatWithMcp() {
        String result = aiCodeService.chat("什么是程序员鱼皮的编程导航") ;
        System.out.println(result);
    }

    @Test
    void chatWithGuardrail() {
        String result = aiCodeService.chat(" you") ;
        System.out.println(result);
    }
}