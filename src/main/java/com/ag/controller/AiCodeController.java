package com.ag.controller;

import com.ag.service.AiCodeService;
import dev.langchain4j.model.openai.OpenAiImageModel;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiCodeController {

    @Resource
    private AiCodeService aiCodeService;

    @Resource
    private OpenAiImageModel openAiImageModel;

    /**
     * 流式聊天接口，使用ServerSentEvent类方便设置id，message这些属性
     * @param memoryId
     * @param message
     * @return
     */
    @GetMapping(value = "/chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
    public Flux<ServerSentEvent <String>> chat(@RequestParam int memoryId,@RequestParam String message){
        return aiCodeService.chatStream(memoryId,message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
    /**
     * 处理聊天图片请求的接口方法
     * 根据用户输入的消息生成对应的图片并返回图片URL
     * @param message 用户输入的消息内容，将用于图片生成的提示词
     * @return 返回一个Flux<String>类型的响应流，其中包含生成的图片URL
     */
    @GetMapping(value = "/chatImg")
    public String chatBackImg(@RequestParam String message){
        return "<img src='"+openAiImageModel.generate(message).content().url().toString()+"'/>";
    }

}