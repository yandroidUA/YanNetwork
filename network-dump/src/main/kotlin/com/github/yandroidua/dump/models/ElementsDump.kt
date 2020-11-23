package com.github.yandroidua.dump.models

import kotlinx.serialization.Serializable

@Serializable
data class ElementsDump(
        val lines: MutableList<LineDump> = mutableListOf(),
        val workstations: MutableList<WorkstationDump> = mutableListOf(),
        val communicationNodes: MutableList<CommunicationNodeDump> = mutableListOf()
)