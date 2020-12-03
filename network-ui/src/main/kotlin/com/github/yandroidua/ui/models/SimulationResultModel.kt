package com.github.yandroidua.ui.models

import androidx.compose.ui.geometry.Offset

sealed class SimulationResultModel {

    data class TextSimulationModel(val text: String) : SimulationResultModel()

    data class MessageStartModel(
            val from: Int,
            val to: Int,
            val by: Int,
            val time: Long
    ) : SimulationResultModel()

    data class MessageMoveModel(
            val from: Int,
            val to: Int,
            val by: Int,
            val offset: Offset,
            val time: Long
    ) : SimulationResultModel()

    data class ErrorMessageModel(val reason: String) : SimulationResultModel()

}