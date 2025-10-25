/**
 * Copyright (c) 2025. Shanghai HEADING information Engineering Co., Ltd. All rights reserved.
 */
package pers.tia.alibaba.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.transformer.ContentFormatTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档的提取（Extract）、转换（Transform）和加载（Load）
 *
 * @author ZhengYu
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("etl")
public class DocumentETLController {

  private final ChatModel chatModel;
  private final SimpleVectorStore vectorStore;

  private final List<DocumentTransformer> transformers = new ArrayList<>();

  public DocumentETLController(ChatModel chatModel, EmbeddingModel embeddingModel) {
    this.chatModel = chatModel;
    this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();

    transformers.add(TokenTextSplitter.builder().build());
    transformers.add(new ContentFormatTransformer(DefaultContentFormatter.defaultConfig()));
    transformers.add(new KeywordMetadataEnricher(this.chatModel, 3));
    List<SummaryMetadataEnricher.SummaryType> summaryTypes = List.of(
        SummaryMetadataEnricher.SummaryType.NEXT,//
        SummaryMetadataEnricher.SummaryType.CURRENT,//
        SummaryMetadataEnricher.SummaryType.PREVIOUS);
    transformers.add(new SummaryMetadataEnricher(chatModel, summaryTypes));
  }

  @GetMapping
  public void etl() throws IOException {
    MarkdownDocumentReader reader = new MarkdownDocumentReader(
        "classpath:asserts/黄金：稳健投资的压舱石.md");
    List<Document> documents = reader.get();
    log.info("extra documents: {}", toJson(documents));

    for (DocumentTransformer transformer : transformers) {
      documents = transformer.transform(documents);
      log.info("after {} transform documents: {}", transformer.getClass().getSimpleName(),
          toJson(documents));
    }

    vectorStore.add(documents);

    File file = new File("/Users/zhengyu/Downloads/temp/tmp_vector.json");
    if (!file.exists()) {
      log.info("file created: {}", file.createNewFile());
    }
    vectorStore.save(file);
  }

  private String toJson(Object object) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}