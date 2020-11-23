package com.github.yandroidua.algorithm

class BellmanFordAlgorithm(
        private val workstations: List<Workstation>,
        private val lines: List<Line>
) {

    fun calculate(from: Workstation, to: Workstation): List<PathResult> {
        val k = workstations.size
        val result = mutableListOf<PathResult>()
        repeat(times = k - 1) { count ->
            val info = getConnectionOr(from, to, count + 1, first = true)
            result.add(PathResult(
                    from = from.number,
                    to = to.number,
                    summary = info.first,
                    path = info.second
            ))
        }
        return result
    }

    fun calculate(from: Workstation): List<PathResult> {
        // count of stations
        val k = workstations.size
        // allocation [k][k-1] matrix, where k - for stations, and k-1 for path result from
        val result = mutableListOf<PathResult>()
        repeat(times = k) { index ->
            repeat(times = k - 1) { count ->
                val info = getConnectionOr(from = from, workstations[index], count + 1, first = true)
                result.add(PathResult(
                        from = from.number,
                        to = workstations[index].number,
                        summary = info.first,
                        path = info.second
                ))
            }
        }
        return result
    }

    private fun getConnectionOr(
            from: Workstation,
            to: Workstation,
            count: Int,
            or: Int = Int.MAX_VALUE,
            first: Boolean = false,
    ): Pair<Int, List<Pair<Int, Int>>> {
        if (from.number == to.number) return 0 to listOf()
        if (count <= 0) return or to listOf()
        return from.linesId
                .asSequence()
                .flatMap { lineId ->
                    val line = lines.find { it.id == lineId } ?: return@flatMap listOf(null)
                    listOf(
                            workstations.find { it.number == line.station2Number }?.let { it to line },
                            workstations.find { it.number == line.station1Number }?.let { it to line }
                    )
                }
                .filterNotNull() // remove all failure search
                .map {
                    val conn = getConnectionOr(from = it.first, to = to, count = count - 1, or = or)
                    if (conn.first == or)
                        conn
                    else
                        (conn.first + it.second.weight) to conn.second.toMutableList().apply {
                            add(it.second.id to if (first) to.number else from.number)
                        }
                }
                .minByOrNull { it.first } // get min weight from all lines
                ?: or to listOf() // if no way from to to default value will be returned
    }

}