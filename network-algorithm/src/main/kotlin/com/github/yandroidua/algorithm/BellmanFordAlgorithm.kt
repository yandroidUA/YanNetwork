package com.github.yandroidua.algorithm

class BellmanFordAlgorithm(
        private val workstations: List<Workstation>
) {

    fun calculate(from: Workstation): List<List<Int>> {
        // count of stations
        val k = workstations.size
        // allocation [k][k-1] matrix, where k - for stations, and k-1 for path result from
        val result = Array(size = k) { Array(size = k - 1) { Int.MAX_VALUE } }
        repeat(times = k) { index ->
            repeat(times = k - 1) { count ->
                result[index][count] = getConnectionOr(from = from, workstations[index], count + 1)
            }
        }
        println(result)
        return emptyList()
    }

    private fun getConnectionOr(from: Workstation, to: Workstation, count: Int, or: Int = Int.MAX_VALUE): Int {
        if (from.number == to.number) return 0
        if (count <= 0) return or
        return from.lines
                .asSequence()
                .flatMap { line ->
                    listOf(
                            workstations.find { it.number == line.station2Number }?.to(line),
                            workstations.find { it.number == line.station1Number }?.to(line),
                    )
                }
                .filterNotNull() // remove all failure search
                .map {
                    val conn = getConnectionOr(from = it.first, to = to, count = count - 1, or = or)
                    if (conn == or) or else conn + it.second.weight
                } // start searching new way to get to the station and add weight of current line
                .minOrNull() // get min weight from all lines
                ?: or // if no way from to to default value will be returned
    }

}