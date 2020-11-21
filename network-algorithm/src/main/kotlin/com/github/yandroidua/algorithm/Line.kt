package com.github.yandroidua.algorithm

data class Line(
        val id: Int,
        val station1Number: Int,
        val station2Number: Int,
        val weight: Int,
        val type: LineType,
        val errorChance: Float
)