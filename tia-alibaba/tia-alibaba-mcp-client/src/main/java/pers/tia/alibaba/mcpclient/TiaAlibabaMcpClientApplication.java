/**
 * Copyright (c) 2025. Shanghai HEADING information Engineering Co., Ltd. All rights reserved.
 */
package pers.tia.alibaba.mcpclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * @author ZhengYu
 * @since 1.0
 */
@SpringBootApplication
public class TiaAlibabaMcpClientApplication {
  public static void main(String[] args) {
    SpringApplication.run(TiaAlibabaMcpClientApplication.class, args);
  }

  @Bean
  public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
      ToolCallbackProvider tools, ConfigurableApplicationContext context) {
    return args -> {
      var chatClient = chatClientBuilder.defaultToolCallbacks(tools.getToolCallbacks()).build();

      Scanner scanner = new Scanner(System.in);
      while (true) {
        System.out.print("\n>>> QUESTION: ");
        String userInput = scanner.nextLine();
        if (userInput.equalsIgnoreCase("exit")) {
          break;
        }
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
      }
      scanner.close();
      context.close();
    };
  }
}