/**
 * Copyright (c) 2025. Shanghai HEADING information Engineering Co., Ltd. All rights reserved.
 */
package pers.tia.alibaba.mcpserver;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author ZhengYu
 * @since 1.0
 */
@SpringBootApplication
public class TiaAlibabaMcpServerApplication {
  public static void main(String[] args) {
    SpringApplication.run(TiaAlibabaMcpServerApplication.class, args);
  }

  @Bean
  public ToolCallbackProvider timeTools(TimeService timeService) {
    return MethodToolCallbackProvider.builder().toolObjects(timeService).build();
  }
}