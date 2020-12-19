package com.icthh.xm.actions.settings

import com.icthh.xm.service.getSettings
import com.icthh.xm.service.isSupportProject
import com.icthh.xm.service.updateSupported
import com.icthh.xm.utils.logger
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class MainSettingAction : AnAction() {

    init {
        logger.info("Init main settings")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dialog = SettingsDialog(project)
        dialog.show()
        if (dialog.isOK) {
            project.getSettings().envs.clear()
            project.getSettings().envs.addAll(dialog.data)
        }
        project.save()
    }

    override fun update(e: AnActionEvent) {
        logger.info("Update main settings")
        e.updateSupported() ?: return
        e.presentation.isEnabled = e.project != null
        logger.info("Main settings is ${e.presentation.isEnabled}")
    }
}
