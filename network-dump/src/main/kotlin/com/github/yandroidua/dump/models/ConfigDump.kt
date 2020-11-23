package com.github.yandroidua.dump.models

import kotlinx.serialization.Serializable

@Serializable
data class ConfigDump(
        val elements: ElementsDump = ElementsDump()
)