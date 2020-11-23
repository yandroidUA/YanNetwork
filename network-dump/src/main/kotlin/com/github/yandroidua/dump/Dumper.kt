package com.github.yandroidua.dump

import com.github.yandroidua.dump.models.*
import kotlinx.serialization.json.Json
import java.io.File

interface Dumper {

    val path: String

    fun addToDump(communicationNodeDump: CommunicationNodeDump): Dumper

    fun addToDump(lineDump: LineDump): Dumper

    fun addToDump(workstationDump: WorkstationDump): Dumper

    fun dump(): Dumper

    fun read(): ConfigDump?

    class EasyDumper : Dumper {

        private val json: Json by lazy {
            Json {  }
        }

        private val configDump: ConfigDump = ConfigDump()
        private val elementsDump: ElementsDump by lazy { configDump.elements }

        override var path: String = ""
            private set

        override fun addToDump(communicationNodeDump: CommunicationNodeDump): Dumper {
            elementsDump.communicationNodes.add(communicationNodeDump)
            return this
        }

        override fun addToDump(lineDump: LineDump): Dumper {
            elementsDump.lines.add(lineDump)
            return this
        }

        override fun addToDump(workstationDump: WorkstationDump): Dumper {
            elementsDump.workstations.add(workstationDump)
            return this
        }

        override fun dump(): Dumper {
            val writer = File(path).printWriter(charset("UTF8"))
            writer.println(json.encodeToJsonElement(ConfigDump.serializer(), configDump))
            writer.close()
            return this
        }

        fun path(path: String): Dumper {
            this.path = path
            return this
        }

        override fun read(): ConfigDump? {
            return try {
                val txt = File(path).readText()
                json.decodeFromString(ConfigDump.serializer(), txt)
            } catch (e : Exception) {
                e.printStackTrace()
                null
            }
        }

    }

}