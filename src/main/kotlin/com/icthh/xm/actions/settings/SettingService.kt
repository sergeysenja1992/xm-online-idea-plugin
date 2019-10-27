package com.icthh.xm.actions.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.util.xmlb.XmlSerializerUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@State(name = "xm^Online.Settings")
class SettingService: PersistentStateComponent<SettingService> {

    var envs: MutableList<EnvironmentSettings> = ArrayList()
    var selectedEnv: String? = null
    var trackChanges: Boolean = false
    var editedFiles: MutableMap<String, String> = HashMap()

    override fun getState() = this

    override fun loadState(state: SettingService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun selected() = envs.find { it.id == selectedEnv }

    fun select(settings: EnvironmentSettings?) {
        selectedEnv = settings?.id
    }
}

class EnvironmentSettings {

    var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var xmUrl: String = ""
    var xmSuperAdminLogin: String = ""
    var xmSuperAdminPassword: String = ""

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EnvironmentSettings

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}


