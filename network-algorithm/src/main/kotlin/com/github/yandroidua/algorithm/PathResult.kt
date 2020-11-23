package com.github.yandroidua.algorithm

data class PathResult(
        val from: Int = -1,
        val to: Int = -1,
        val summary: Int = -1,
        val path: List<Pair<Int, Int>> = emptyList() // contains lineId and station that this line is connecting
)