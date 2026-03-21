package com.wheels.app.core.trust.domain.model

enum class TrustScoreNoticeType {
    SUCCESS,
    ERROR
}

data class TrustScoreNotice(
    val title: String,
    val message: String,
    val newScore: Int? = null,
    val deltaPoints: Int? = null,
    val type: TrustScoreNoticeType = TrustScoreNoticeType.SUCCESS
)
