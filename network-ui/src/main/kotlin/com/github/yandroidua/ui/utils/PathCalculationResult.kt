package com.github.yandroidua.ui.utils

import com.github.yandroidua.algorithm.Workstation

sealed class PathCalculationResult {

    data class SingleResult(
            val from: Workstation,
            val to: Workstation,
            val path: List<Pair<Int, List<Int>>>
    ) : PathCalculationResult()

    data class MultiResult(
            val from: Workstation,
            val paths: Array<Array<Pair<Int, List<Int>>>>
    ) : PathCalculationResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MultiResult

            if (from != other.from) return false
            if (!paths.contentDeepEquals(other.paths)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = from.hashCode()
            result = 31 * result + paths.contentDeepHashCode()
            return result
        }

    }

}