# Technology Decision Record: AI Models and Vector Store

## Decision 1: Model Selection for Chat and Embeddings

### Status
Accepted

### Context
The Daily NAV library integrates AI capabilities for natural language search, text-to-SQL generation, and qualitative narrative/summary generation. These capabilities require robust Large Language Models (LLMs) and embedding models. A critical constraint of this library is that it is an open-source, embedded, zero-configuration tool; introducing external cloud dependencies (e.g., OpenAI or Anthropic API keys) violates our privacy and operational constraints.

The library currently relies on the Spring AI Ollama starter configured in `pom.xml` and wired via `DailyNavAiAutoConfiguration`.

### Decision
We retain the current implementation: **Ollama** as the local execution engine, leveraging **llama3.2:1b** (or Mistral) for chat and text-to-SQL generation, alongside **mxbai-embed-large** for generating semantic embeddings. 

We will explicitly retain the existing deterministic settings currently defined in `application.properties`:
*   `spring.ai.ollama.chat.options.temperature=0.2`
*   `spring.ai.ollama.chat.options.top-k=2`
*   `spring.ai.ollama.chat.options.top-p=0.2`

### Rationale
*   **Local-Only Constraint**: Ollama runs models locally without cloud keys, adhering to strict financial data privacy requirements.
*   **Determinism & Performance**: Retaining the low temperature and strict top-k/top-p parameters ensures predictable text-to-SQL generation and prevents creative deviation during structured lookups.
*   **GraalVM 25 CI Compatibility**: The existing `spring-ai-starter-model-ollama` integrates seamlessly with Spring Boot Native Image compilation, allowing us to maintain compatibility with our GraalVM 25 CI pipelines without requiring custom reflection hints.
*   **Embeddings Size/Latency**: `mxbai-embed-large` provides excellent semantic search precision while remaining lightweight enough to satisfy latency constraints on standard hardware.

### Trade-offs
*   *Hardware Requirements*: Running local LLMs demands adequate host memory and CPU limits. While `llama3.2:1b` is highly optimized, it still poses a baseline resource floor.
*   *Reasoning Capability*: Smaller local models may struggle with massive zero-shot reasoning tasks. We accept this trade-off because our architecture relies heavily on deterministic tool-calling (via `MutualFundTools`) rather than open-ended LLM reasoning.

---

## Decision 2: Vector Database Infrastructure

### Status
Accepted

### Context
For the qualitative RAG workflow (answering questions about prospectuses), the library requires a mechanism to store and query text chunks. Currently, `DailyNavAiAutoConfiguration` wires a `SimpleVectorStore` bean (`dailyNavVectorStore`). 

This embedded approach has known limitations: it operates in-memory, the `vectorstore.json` is only ever loaded (never written to disk by the app during ingestion), and the underlying AI dependencies are scoped as `provided` / `optional` in `pom.xml`, requiring the host application to actively supply the artifact.

### Decision
We adopt a **Tiered Recommendation Model** that retains the existing baseline while enabling extensibility:
1.  **Default (Retain)**: The `SimpleVectorStore` bean (`dailyNavVectorStore`) remains the default for the embedded/zero-config path.
2.  **Pluggable Upgrade (Extend)**: We explicitly design the auto-configuration to back off (`@ConditionalOnMissingBean(VectorStore.class)`) if the host application provides an external vector DB bean (e.g., pgvector, Chroma) for larger, host-managed corpora.

### Rationale
*   **Zero-Config Promise**: Retaining `SimpleVectorStore` allows the library to run locally without forcing developers to spin up external Docker containers or install specialized databases. It satisfies the core library requirement out of the box.
*   **Optional Scope Alignment**: Because AI dependencies are `optional` in `pom.xml`, developers who do not enable AI (`daily-nav.ai.enabled=false`) pay zero cost. Developers who *do* enable it can easily override the default `SimpleVectorStore` bean with an enterprise implementation if needed.

### Trade-offs
*   *Scalability*: `SimpleVectorStore` loads all embeddings into application RAM. This is perfectly adequate for our current dataset of mutual fund metadata but will hit OOM limits if a host tries to ingest thousands of PDF prospectuses.
*   *Operational Cost*: Upgrading to pgvector or Chroma requires the consumer to provision and maintain external infrastructure, breaking the zero-config simplicity in exchange for massive scalability.
