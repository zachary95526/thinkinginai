/**
 * Copyright (c) 2025. Shanghai HEADING information Engineering Co., Ltd. All rights reserved.
 */
package pers.tia.alibaba.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 向量化
 *
 * @author ZhengYu
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/vector")
public class EmbeddingController {
  private final VectorStore vectorStore;

  public EmbeddingController(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  @GetMapping("init")
  public void init() {
    List<String> original = Arrays.asList(//
        "崔颢《黄鹤楼》",//
        "昔人已乘黄鹤去，此地空余黄鹤楼。",//
        "黄鹤一去不复返，白云千载空悠悠。",//
        "晴川历历汉阳树，芳草萋萋鹦鹉洲。",//
        "日暮乡关何处是？烟波江上使人愁。" //
    );
    List<Document> documents = new ArrayList<>();
    for (int i = 0; i < original.size(); i++) {
      Document document = new Document(Integer.toString(i), original.get(i),
          Map.of("lucyNum", new Random().nextInt(original.size())));
      documents.add(document);
    }
    vectorStore.add(documents);
  }

  @GetMapping("search")
  public List<Document> search(@RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "lucyNum", required = false) Integer lucyNum) {
    SearchRequest.Builder srb = SearchRequest.builder().topK(3);
    if (query != null) {
      srb.query(query);
    }
    if (lucyNum != null) {
      srb.filterExpression(
          new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("lucyNum"),
              new Filter.Value(lucyNum)));
    }
    return vectorStore.similaritySearch(srb.build());
  }
}