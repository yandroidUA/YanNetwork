package com.github.yandroidua.ui.screens.calculation

import androidx.compose.runtime.MutableState

data class CalculationFromErrorContext(
    val workstationFromErrorState: MutableState<Boolean>,
    val messageSizeErrorState: MutableState<String?>,
    val informationPackageSizeErrorState: MutableState<String?>,
    val systemPackageSizeErrorState: MutableState<String?>,
    val frameSysSizeState: MutableState<String?>
)