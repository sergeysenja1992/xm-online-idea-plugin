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

    override fun getState() = this

    override fun loadState(state: SettingService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun selected() = envs.find { it.id == selectedEnv }
}

class EnvironmentSettings {

    val id: String = UUID.randomUUID().toString()
    var name: String = ""
    var xmUrl: String = ""
    var xmSuperAdminLogin: String = ""
    var xmSuperAdminPassword: String = ""
    var editedFiels: Map<String, String> = HashMap()

    override fun toString() = name
}


