package com.ag.service;

import com.ag.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

import java.util.List;


@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeService {

    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String msg);

    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatReport(String msg);

    // 总结信息,利用record结构化输出的形式
    record Report(String name, List<String> SummaryList){
    }

    //返回封装后的结果
    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(String msg);

    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId,@UserMessage String msg);
}