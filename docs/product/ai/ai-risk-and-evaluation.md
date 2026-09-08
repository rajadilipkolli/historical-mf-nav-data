# AI Risk and Evaluation Strategy

## Risk Analysis and Mitigations

Integrating non-deterministic LLMs into a deterministic financial data library introduces specific risks. Our strategy frames every mitigation as an enhancement to existing code entities.

| Risk Category              | Description                                            | Existing Code Mitigation                                                                                                                                                                                                                                                                                                |
|:---------------------------|:-------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Hallucination (RAG)**    | Fabricating answers from unrelated documents.          | **Thresholding**: Update `KnowledgeSearchService`, which currently queries `SimpleVectorStore` using `topK`, to enforce a strict similarity threshold, rejecting low-confidence matches.                                                                                                                                |
| **Hallucination (SQL)**    | LLMs passing fabricated dates/ISINs to tools.          | **Validation**: Implement explicit input validation within the six `@Tool` methods in `MutualFundTools` to reject invalid ISIN formats before querying repositories. Address unhandled LLM exceptions in `NaturalLanguageSearchService` by failing gracefully.                                                          |
| **Latency**                | Local Ollama inference adds multi-second overhead.     | **Caching**: Reuse the existing Caffeine `latestNav` cache configured in `MutualFundService` to bypass the LLM for repeated identical queries.                                                                                                                                                                          |
| **Cost & Maintainability** | Operational burden of scaling hardware and data churn. | **Opt-in Dormancy & Scoping**: Maintain the `daily-nav.ai.enabled` toggle. Ensure AI dependencies remain `provided`/`optional` in `pom.xml`. To mitigate ingestion costs, address the behavior in `SchemeDocumentIngestionService` where it currently recomputes and re-ingests documents on every application restart. |

## Financial-Correctness Priority

**Absolute Directive**: Numeric answers, historical returns, and NAV values **must** come directly from deterministic SQL execution or existing parameterized repository results (`MutualFundService` + JDBC repositories). The LLM is strictly prohibited from generating free-text numerical predictions. This is fundamentally consistent with how our existing `TrendAnomalyService` computes metrics statistically before synthesizing any narrative.

## Evaluation Strategy

To ensure confidence, we propose an automated evaluation strategy integrated into the existing CI pipeline.

### RAG Path Metrics
For `KnowledgeSearchService`:
*   **Faithfulness**: Does the answer strictly align with the retrieved vector chunks?
*   **Answer Relevance**: Does it answer the user's prompt?
*   **Context Precision / Recall**: Did `SimpleVectorStore` retrieve the correct chunks?

### Text-to-SQL Path Metrics
For structured-data queries:
*   **Execution Accuracy**: Does the SQL execute without syntax errors against `dailyNavJdbcTemplate`?
*   **Result-Match**: Does the AI's result perfectly match a deterministic, hardcoded SQL query?

### CI Integration (Testcontainers)
Currently, our primary evaluation gate is the `AiSearchControllerIT`, which only asserts that the HTTP response is non-blank. 
To implement stronger gates, we will enhance the existing `TestcontainersConfiguration` (which already spins up Ollama) to run a suite of "golden prompts". Assertions will calculate RAGAS metrics and SQL accuracy, failing the build if thresholds are not met.

---

## Phased Rollout Plan

To safely introduce these capabilities without disrupting deterministic workflows, we propose a rollout sequenced by risk and value:

### Phase 1: Harden Existing Paths
*   **Action**: Add robust exception handling and input validation around `NaturalLanguageSearchService` and `MutualFundTools`. Add a strict similarity distance threshold to `KnowledgeSearchService`.

### Phase 2: Introduce Guarded Text-to-SQL
*   **Action**: Deploy the self-correcting, read-only text-to-SQL path to run alongside the existing `MutualFundTools` for handling open-ended analytics.

### Phase 3: Add Evaluation Gates
*   **Action**: Expand `AiSearchControllerIT` and `TestcontainersConfiguration` to enforce RAGAS and SQL accuracy metrics during the Maven `verify` phase.

### Phase 4: Enable Pluggable External Vector Store (Optional)
*   **Action**: Expose auto-configuration hooks allowing consumers to swap the default `SimpleVectorStore` bean for an external provider (like pgvector) to support massive-scale corpora.
