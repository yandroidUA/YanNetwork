package com.github.yandroidua.algorithm.models

data class PathHolder(
        val workstationId: Int,
        var weight: Int,
        var path: List<Pair<Int, Int>> = emptyList()
)