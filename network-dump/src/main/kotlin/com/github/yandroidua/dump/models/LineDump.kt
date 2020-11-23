package com.github.yandroidua.dump.models

import kotlinx.serialization.Serializable

@Serializable
data class LineDump(
        val id: Int,
        val startOffset: OffsetDump,
        val endOffset: OffsetDump,
        val firstStationId: Int,
        val secondStationId: Int,
        val color: ColorDump,
        val state: String,
        val weight: Int,
        val isInMovement: Boolean
)