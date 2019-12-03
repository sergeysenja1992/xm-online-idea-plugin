package com.icthh.xm.actions.deploy

import com.github.mvysny.karibudsl.v8.*
import com.icthh.xm.actions.VaadinDialog
import com.icthh.xm.service.*
import com.icthh.xm.utils.showDiffDialog
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.vaadin.icons.VaadinIcons
import com.vaadin.icons.VaadinIcons.*
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.*
import org.apache.commons.codec.digest.DigestUtils.sha256Hex
import java.awt.Dimension
import java.io.File

class FileListDialog(project: Project, val changes: ChangesFiles): VaadinDialog(
    project = project, viewName = "file-list", dialogTitle = "File to update",
    dimension = Dimension(1024, 300)
) {
    override fun component(): Component {
        val ui = UI.getCurrent()
        val panel = Panel().apply {
            setSizeFull()

            addChild(VerticalLayout().apply {
                setSizeUndefined()

                changes.changesFiles.ifEmpty {
                    label {
                        caption = "Already up to update"
                    }
                }
                changes.changesFiles.forEach { fileName ->
                    val warning = Label("${WARNING.html} File changed", HTML)
                    warning.isVisible = false

                    val configPath = fileName
                    val line = horizontalLayout {
                        link { caption = configPath }
                    }
                    line.addComponent(warning)

                    val path = project.configPathToRealPath(fileName)
                    val virtualFile = VfsUtil.findFile(File(path).toPath(), false)
                    if (virtualFile != null) {
                        markFileAsChanged(warning, line, configPath, virtualFile, ui)
                    }
                    line.apply {
                        if (changes.toDelete.contains(fileName)) {
                            label{
                                description = "It's file was deleted"
                                html(TRASH.html)
                            }
                        }
                        if (changes.editedInThisIteration.contains(fileName)) {
                            label{
                                description = "It's file edited in you version after last update"
                                html(EDIT.html)
                                id = "EDIT"
                            }
                        }
                        onLayoutClick {
                            if (it.clickedComponent == null || !(it.clickedComponent is Link)) {
                                return@onLayoutClick
                            }
                            getApplication().invokeLater ({
                                val psiFile = virtualFile?.toPsiFile(project)
                                psiFile?.navigate(true)
                            }, ModalityState.stateForComponent(rootPane))
                        }
                    }

                }
            })
        }
        return panel
    }

    private fun markFileAsChanged(
        warning: Label,
        line: HorizontalLayout,
        configPath: String,
        virtualFile: VirtualFile,
        ui: UI
    ) {
        val fileName = virtualFile.path
        getApplication().executeOnPooledThread {
            val configService = project.getExternalConfigService()
            val settings = project.getSettings()?.selected() ?: return@executeOnPooledThread
            val config = configService.getConfigFileIfExists(project, settings, virtualFile.getConfigRelatedPath(project), null)

            if (config == null) {
                ui.access {
                    removeEditIcon(line)
                    line.apply {
                        label{
                            description = "It's new file"
                            html(FILE_ADD.html)
                        }
                    }
                }
                return@executeOnPooledThread
            }

            val sha256Hex = settings.editedFiles.get(virtualFile.path)?.sha256
            if (!sha256Hex(config).equals(sha256Hex)) {
                ui.access {
                    removeEditIcon(line)
                    warning.isVisible = true

                    line.apply {
                        onLayoutClick {
                            if (it.clickedComponent == null || !(it.clickedComponent is Link)) {
                                return@onLayoutClick
                            }

                            getApplication().invokeLater({
                                showDiffDialog("File difference", config, configPath, fileName, project, virtualFile)
                            }, ModalityState.stateForComponent(rootPane))
                        }
                    }

                }
            }

        }
    }

    private fun removeEditIcon(line: HorizontalLayout) {
        line.filter { it.id == "EDIT" }.forEach { line.removeComponent(it) }
    }

}
