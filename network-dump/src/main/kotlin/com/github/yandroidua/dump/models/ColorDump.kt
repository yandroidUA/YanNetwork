package com.github.yandroidua.dump.models

import kotlinx.serialization.Serializable

@Serializable
data class ColorDump(
        val red: Int,
        val green: Int,
        val blue: Int,
        val alpha: Int
)