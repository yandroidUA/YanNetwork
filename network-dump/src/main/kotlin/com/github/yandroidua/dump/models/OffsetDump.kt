package com.github.yandroidua.dump.models

import kotlinx.serialization.Serializable

@Serializable
data class OffsetDump(
        val x: Float,
        val y: Float
)