package com.github.yandroidua.algorithm

class BellmanFordAlgorithm(
        private val workstations: List<Workstation>,
        private val lines: List<Line>
) {

    fun calculate(from: Workstation, to: Workstation): List<Pair<Int, List<Int>>> {
        val k = workstations.size
        val result = mutableListOf<Pair<Int, List<Int>>>()
        repeat(times = k - 1) { count ->
            result.add(getConnectionOr(from, to, count + 1))
        }
        return result
    }

    fun calculate(from: Workstation): Array<Array<Pair<Int, List<Int>>>> {
        // count of stations
        val k = workstations.size
        // allocation [k][k-1] matrix, where k - for stations, and k-1 for path result from
        val result = Array(size = k) { Array(size = k - 1) { Pair(Int.MAX_VALUE, emptyList<Int>()) } }
        repeat(times = k) { index ->
            repeat(times = k - 1) { count ->
                result[index][count] = getConnectionOr(from = from, workstations[index], count + 1)
            }
        }
        return result
    }

    private fun getConnectionOr(
            from: Workstation,
            to: Workstation,
            count: Int,
            or: Int = Int.MAX_VALUE
    ): Pair<Int, List<Int>> {
        if (from.number == to.number) return 0 to listOf()
        if (count <= 0) return or to listOf()
        return from.linesId
                .asSequence()
                .flatMap { lineId ->
                    val line = lines.find { it.id == lineId } ?: return@flatMap listOf(null)
                    listOf(
                            workstations[line.station2Number] to line,
                            workstations[line.station1Number] to line
                    )
                }
                .filterNotNull() // remove all failure search
                .map {
                    val conn = getConnectionOr(from = it.first, to = to, count = count - 1, or = or)
                    if (conn.first == or)
                        conn
                    else
                        (conn.first + it.second.weight) to conn.second.toMutableList().apply { add(it.second.id) }
                }
                .minByOrNull { it.first } // get min weight from all lines
                ?: or to listOf() // if no way from to to default value will be returned
    }

}