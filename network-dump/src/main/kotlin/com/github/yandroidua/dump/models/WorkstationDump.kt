package com.github.yandroidua.dump.models

import kotlinx.serialization.Serializable

@Serializable
data class WorkstationDump(
        val id: Int,
        val offset: OffsetDump,
        val lineIds: List<Int>
)