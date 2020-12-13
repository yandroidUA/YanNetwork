package com.github.yandroidua.algorithm

import com.github.yandroidua.algorithm.models.Line
import com.github.yandroidua.algorithm.models.PathHolder
import com.github.yandroidua.algorithm.models.PathResult
import com.github.yandroidua.algorithm.models.Workstation

class BellmanFordAlgorithm(
        private val workstations: List<Workstation>,
        private val lines: List<Line>
) {

    fun calculate(from: Workstation, to: Workstation, byLength: Boolean): List<PathResult> {
        return calculatePaths(from, byLength, to)
    }

    fun calculate(from: Workstation, byLength: Boolean): List<PathResult> {
        return calculatePaths(from, byLength)
    }

    private fun calculatePaths(
            from: Workstation,
            byLength: Boolean = true,
            wTo: Workstation? = null,
            or: Int = Int.MAX_VALUE
    ): List<PathResult> {
        val distances = Array(workstations.size) {
            PathHolder(workstationId = workstations[it].number, weight = or, path = emptyList())
        }
        distances.find { it.workstationId == from.number }?.weight = 0
        repeat(workstations.size - 1) { repeat(lines.size) { lineIndex ->
            val firstWorkStation = distances.find { it.workstationId == lines[lineIndex].station1Number }
            val secondWorkStation = distances.find { it.workstationId == lines[lineIndex].station2Number }
            val weight = if (byLength) lines[lineIndex].weight else 1

            if (firstWorkStation?.weight != or && (secondWorkStation?.weight ?: -1) > (firstWorkStation?.weight?.plus(weight) ?: -1)) {
                secondWorkStation?.weight = firstWorkStation?.weight?.plus(weight) ?: -1
            } else if (secondWorkStation?.weight != or && (firstWorkStation?.weight ?: -1) > (secondWorkStation?.weight?.plus(weight) ?: -1)) {
                firstWorkStation?.weight = secondWorkStation?.weight?.plus(weight) ?: -1
            }

        } }

        // get distance path
        val analyzingDistances = if (wTo == null) distances else arrayOf(distances.find { it.workstationId == wTo.number } ?: return emptyList())
        for (to in analyzingDistances) {
            calculateMinPath(to, from, or, distances, byLength)
        }

        return analyzingDistances.map {
            PathResult(from = from.number, to = it.workstationId, summary = it.weight, path = it.path)
        }
    }

    private fun calculateMinPath(to: PathHolder, from: Workstation, or: Int, distances: Array<PathHolder>, byLength: Boolean = true) {
        var currentWorkstation = to.workstationId
        val path = mutableListOf<Pair<Int, Int>>()
        var summaryWeight = 0
        var realWeight = 0
        var tries = 0

        while (currentWorkstation != from.number) {
            if (tries == workstations.size) return
            tries++
            for (workstation in workstations) {
                if (currentWorkstation == from.number) {
                    path.add(-1 to currentWorkstation)
                    break
                }
                var connectionWeight = or
                var realConnectionWeight = connectionWeight
                var mLineId = -1
                // finding line that connect some workstation with currentWorkstation
                for (lineId in workstation.linesId) {
                    // finding line that connect to currentStation
                    val line = lines.find { it.id == lineId } ?: continue
                    if (line.station1Number == currentWorkstation || line.station2Number == currentWorkstation) {
                        connectionWeight = if (byLength) line.weight else 1
                        realConnectionWeight = line.weight
                        mLineId = line.id
                        break
                    }
                }
                if (connectionWeight == or) continue
                // get params of workstation that connect currentWorkstation
                val workstationDistance = distances.find { it.workstationId == workstation.number } ?: continue

                if (workstationDistance.weight + connectionWeight + summaryWeight == to.weight) {
                    realWeight += realConnectionWeight
                    path.add(mLineId to currentWorkstation)
                    currentWorkstation = workstation.number
                    summaryWeight += connectionWeight
                    if (currentWorkstation == from.number) {
                        path.add(mLineId to currentWorkstation)
                        break
                    }
                }

            }
        }
        to.weight = realWeight
        to.path = path.asReversed()
    }

}