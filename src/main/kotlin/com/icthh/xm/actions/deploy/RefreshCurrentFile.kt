package com.icthh.xm.actions.deploy

import com.icthh.xm.service.getSettings
import com.icthh.xm.service.updateFilesInMemory
import com.icthh.xm.service.updateSupported
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE

class RefreshCurrentFile: AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        project ?: return
        val selected = project.getSettings().selected()
        selected ?: return
        val file = e.getDataContext().getData(VIRTUAL_FILE)
        file ?: return
        project.updateFilesInMemory(listOf(file.path), selected)
    }

    override fun update(anActionEvent: AnActionEvent) {
        anActionEvent.updateSupported() ?: return
        val project = anActionEvent.project
        val settings = project?.getSettings()?.selected()
        anActionEvent.presentation.isEnabled = project != null && settings != null
    }
}
