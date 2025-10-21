/**
 * Copyright (c) 2025. Shanghai HEADING information Engineering Co., Ltd. All rights reserved.
 */
package pers.tia.alibaba.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 最简单的对话实现
 *
 * @author ZhengYu
 * @since 1.0
 */
@RestController
@RequestMapping("/chat")
public class SimpleChatController {

  private final ChatClient chatClient;

  public SimpleChatController(ChatClient.Builder builder) {
    this.chatClient = builder.build();
  }

  @GetMapping("call")
  public String call(@RequestParam(value = "query",
      defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {
    return chatClient.prompt(query).call().content();
  }

  @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> stream(@RequestParam(value = "query",
      defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {
    return chatClient.prompt(query).stream().content();
  }
}