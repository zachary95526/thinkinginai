/**
 * Copyright (c) 2025. Shanghai HEADING information Engineering Co., Ltd. All rights reserved.
 */
package pers.tia.alibaba.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * RAG
 *
 * @author ZhengYu
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("rag")
public class RagController {

  private final SimpleVectorStore vectorStore;
  private final ChatClient.Builder builder;
  private final ChatClient chatClient;

  public RagController(EmbeddingModel embeddingModel, ChatClient.Builder builder) {
    this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    this.builder = builder;
    this.chatClient = builder.build();
  }

  private volatile boolean initRagDataFlag = false;

  @GetMapping("chat-normal")
  public String normalChat() {
    String query = "你知道小狗“舌头”的故事吗？";
    return chatClient.prompt(query).call().content();
  }

  @GetMapping("chat-simple")
  public String chatSimple() {
    initRagData();

    RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()//
        .documentRetriever(VectorStoreDocumentRetriever.builder()//
            .vectorStore(vectorStore)//
            .build()//
        ).build();
    String query = "你知道小狗“舌头”的故事吗？";
    return chatClient.prompt(query).advisors(advisor).call().content();
  }

  @GetMapping("chat")
  public String chatModule() {
    initRagData();

    // 1. Pre-Retrieval
    TranslationQueryTransformer translationQueryTransformer = TranslationQueryTransformer.builder()//
        .chatClientBuilder(builder)//
        .targetLanguage("English")//
        .build();
    MultiQueryExpander multiQueryExpander = MultiQueryExpander.builder()//
        .chatClientBuilder(builder)//
        .build();
    // 2. Retrieval
    VectorStoreDocumentRetriever vectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()//
        .vectorStore(vectorStore)//
        .build();
    ConcatenationDocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
    // 3. Post-Retrieval
    DocumentPostProcessor documentPostProcessor = (query, documents) -> documents;
    // 4. Generation
    ContextualQueryAugmenter contextualQueryAugmenter = ContextualQueryAugmenter.builder()//
        .allowEmptyContext(true)//
        .build();

    RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()//
        .queryTransformers(translationQueryTransformer)//
        .queryExpander(multiQueryExpander)//
        .documentRetriever(vectorStoreDocumentRetriever)//
        .documentJoiner(documentJoiner)//
        .documentPostProcessors(documentPostProcessor)//
        .queryAugmenter(contextualQueryAugmenter)//
        .build();
    String query = "你知道小狗“舌头”的故事吗？";
    return chatClient.prompt(query).advisors(advisor).call().content();
  }

  private synchronized void initRagData() {
    if (initRagDataFlag) {
      return;
    }

    List<String> original = Arrays.asList(//
        "我家有只黄白相间的小土狗，名叫 “舌头”。这名字可不是随便取的 —— 它总爱把粉嫩嫩的舌头吐在外面，跑起来舌头一甩一甩，像挂着片小旗子，连睡觉都要把舌尖露在嘴边，模样憨极了。",
        "每天放学，“舌头” 准会蹲在小区门口等我。一看见我的身影，它就摇着尾巴冲过来，用湿漉漉的鼻子蹭我的手，舌头还会不小心舔到我的裤腿，留下一串湿乎乎的印子。有次我带它去公园，它追着蝴蝶跑了一圈又一圈，舌头伸得老长，呼哧呼哧地喘气，却还是不肯停下。",
        "最有意思的是，它特别喜欢叼拖鞋。每次我换鞋，它就叼着我的拖鞋满屋跑，等我去追，它又会把拖鞋轻轻放在我脚边，然后吐着舌头歪头看我，好像在说：“快夸夸我呀！”",
        "有 “舌头” 陪着我，每一天都充满了欢乐。");
    List<Document> documents = new ArrayList<>();
    for (int i = 0; i < original.size(); i++) {
      Document document = new Document(Integer.toString(i), original.get(i),
          Map.of("lucyNum", new Random().nextInt(original.size())));
      documents.add(document);
    }
    vectorStore.add(documents);
    initRagDataFlag = true;
  }
}