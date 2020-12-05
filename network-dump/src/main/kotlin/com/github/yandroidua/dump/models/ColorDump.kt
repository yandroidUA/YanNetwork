package com.github.yandroidua.dump.models

import kotlinx.serialization.Serializable

@Serializable
data class ColorDump(
        val red: Float,
        val green: Float,
        val blue: Float,
        val alpha: Float
)