package com.github.rajadilipkolli.dailynav;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.FileSystemResource;

public class SchemeDocumentIngestionService implements ApplicationRunner {

  private static final Logger logger =
      LoggerFactory.getLogger(SchemeDocumentIngestionService.class);

  private final VectorStore vectorStore;
  private final DailyNavAiProperties properties;

  public SchemeDocumentIngestionService(VectorStore vectorStore, DailyNavAiProperties properties) {
    this.vectorStore = vectorStore;
    this.properties = properties;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    String path = properties.getRagDocumentPath();
    if (path == null || path.trim().isEmpty()) {
      logger.info("No RAG document path configured. Skipping ingestion.");
      return;
    }

    File dir = new File(path);
    if (!dir.exists() || !dir.isDirectory()) {
      logger.warn("RAG document path does not exist or is not a directory: {}", path);
      return;
    }

    File[] files = dir.listFiles((d, name) -> name.endsWith(".txt") || name.endsWith(".md"));
    if (files == null || files.length == 0) {
      logger.info("No text or markdown files found in RAG document path: {}", path);
      return;
    }

    if (new File(dir, "vectorstore.json").exists()) {
      logger.info("Vector store already restored from disk. Skipping ingestion.");
      return;
    }

    TokenTextSplitter textSplitter = new TokenTextSplitter();

    for (File file : files) {
      logger.info("Ingesting document: {}", file.getName());
      TextReader textReader = new TextReader(new FileSystemResource(file));
      textReader.getCustomMetadata().put("filename", file.getName());
      vectorStore.accept(textSplitter.apply(textReader.get()));
    }
    logger.info("Ingestion complete.");
  }
}
