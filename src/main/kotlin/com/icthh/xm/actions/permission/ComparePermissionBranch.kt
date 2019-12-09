package com.icthh.xm.actions.permission

import com.icthh.xm.service.getRepository
import com.icthh.xm.service.getTenantName
import com.icthh.xm.service.isConfigProject
import com.icthh.xm.service.permission.TenantRoleService
import com.icthh.xm.service.updateSupported
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import git4idea.util.GitFileUtils
import java.nio.charset.StandardCharsets.UTF_8


class ComparePermissionBranch() : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        project ?: return
        val file = e.getDataContext().getData(VIRTUAL_FILE) ?: return
        if (!file.path.endsWith("/permissions.yml")) {
            return
        }
        val repository = project.getRepository() ?: return
        val branches = repository.branches.localBranches.map { it.name }
        val baseListPopupStep = object: BaseListPopupStep<String>("Branches", branches) {
            override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                if (selectedValue != null) {
                    onChosen(project, file, repository, selectedValue)
                }
                return PopupStep.FINAL_CHOICE
            }
        }
        val listPopup = JBPopupFactory.getInstance().createListPopup(baseListPopupStep)
        listPopup.showCenteredInCurrentWindow(project)
    }

    private fun onChosen(project: Project, file: VirtualFile, repository: GitRepository, selectedValue: String) {
        val content = getFileContent(project, file, repository, selectedValue)
        val tenantName = file.getTenantName(project)
        //TenantRoleService(tenantName, project)
    }

    private fun getFileContent(
        project: Project,
        file: VirtualFile,
        repository: GitRepository,
        selectedValue: String
    ): String {
        val relativePath = file.path.substringAfter(repository.root.path)
        try {
            val fileContent = GitFileUtils.getFileContent(project, repository.root, selectedValue, "." + relativePath)
            return String(fileContent, UTF_8)
        } catch (e: VcsException) {
            return ""
        }
    }

    override fun update(anActionEvent: AnActionEvent) {
        super.update(anActionEvent)
        anActionEvent.updateSupported()
        val file = anActionEvent.getDataContext().getData(VIRTUAL_FILE) ?: return
        var isVisibale = anActionEvent.presentation.isVisible
        isVisibale = isVisibale && file.path.endsWith("/permissions.yml")
        isVisibale = isVisibale && anActionEvent.project.isConfigProject()
        anActionEvent.presentation.isVisible = isVisibale && anActionEvent.project?.getRepository() != null
    }
}