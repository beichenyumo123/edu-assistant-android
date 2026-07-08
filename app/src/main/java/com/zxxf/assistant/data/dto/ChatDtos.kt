package com.zxxf.assistant.data.dto

import com.google.gson.annotations.SerializedName

// ── Request ──

data class ChatRequest(
    @SerializedName("conversation_id") val conversationId: Long?,
    val message: String,
    @SerializedName("agent_type") val agentType: String = "edu",
    @SerializedName("selected_document_ids") val selectedDocumentIds: List<Long>? = null
)

// ── HTTP Response ──

data class ChatResponse(
    @SerializedName("conversation_id") val conversationId: Long,
    val message: MessageDto,
    @SerializedName("agent_steps") val agentSteps: List<AgentStepDto>? = null,
    val evaluation: EvaluationDto? = null
)

data class MessageDto(
    val role: String,
    val content: String,
    val sources: List<SourceDto>? = null,
    @SerializedName("agent_steps") val agentSteps: List<AgentStepDto>? = null,
    val evaluation: EvaluationDto? = null
)

// ── Sources ──

data class SourceDto(
    @SerializedName("source_no") val sourceNo: Int,
    @SerializedName("document_id") val documentId: String,
    @SerializedName("document_name") val documentName: String? = null,
    @SerializedName("chunk_index") val chunkIndex: Int,
    @SerializedName("evidence_id") val evidenceId: String? = null,
    @SerializedName("source_type") val sourceType: String? = null,
    @SerializedName("trust_level") val trustLevel: String? = null,
    @SerializedName("retrieval_rank") val retrievalRank: Int? = null,
    @SerializedName("retrieval_score") val retrievalScore: Double? = null,
    val text: String? = null
)

// ── Agent Steps ──

data class AgentStepDto(
    val text: String? = null,
    val step: String? = null,
    @SerializedName("tool_name") val toolName: String? = null,
    @SerializedName("elapsed_ms") val elapsedMs: Long? = null
)

// ── Evaluation ──

data class EvaluationDto(
    val mode: String? = null,
    @SerializedName("overall_score") val overallScore: Double? = null,
    @SerializedName("risk_level") val riskLevel: String? = null,
    @SerializedName("retrieval_quality") val retrievalQuality: Double? = null,
    val groundedness: Double? = null,
    @SerializedName("citation_coverage") val citationCoverage: Double? = null,
    @SerializedName("citation_validity") val citationValidity: Double? = null,
    @SerializedName("hallucination_risk") val hallucinationRisk: Double? = null,
    val retrieval: RetrievalDetailDto? = null,
    val generation: GenerationDetailDto? = null,
    val notes: List<String>? = null
)

data class RetrievalDetailDto(
    @SerializedName("top_k") val topK: Int? = null,
    @SerializedName("selected_document_count") val selectedDocumentCount: Int? = null,
    @SerializedName("retrieved_chunks") val retrievedChunks: Int? = null,
    @SerializedName("retrieval_hit") val retrievalHit: Boolean? = null,
    @SerializedName("best_relevance") val bestRelevance: Double? = null,
    @SerializedName("mean_relevance") val meanRelevance: Double? = null,
    @SerializedName("query_coverage") val queryCoverage: Double? = null,
    @SerializedName("document_diversity") val documentDiversity: Double? = null,
    @SerializedName("source_trust") val sourceTrust: Double? = null
)

data class GenerationDetailDto(
    @SerializedName("claim_count") val claimCount: Int? = null,
    @SerializedName("supported_claim_count") val supportedClaimCount: Int? = null,
    @SerializedName("unsupported_claim_count") val unsupportedClaimCount: Int? = null,
    @SerializedName("valid_citation_count") val validCitationCount: Int? = null,
    @SerializedName("invalid_citation_count") val invalidCitationCount: Int? = null,
    @SerializedName("source_utilization") val sourceUtilization: Double? = null,
    @SerializedName("context_overlap") val contextOverlap: Double? = null,
    @SerializedName("abstention_detected") val abstentionDetected: Boolean? = null
)
