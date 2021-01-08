package com.icthh.xm.editors.permission

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.mvysny.karibudsl.v8.*
import com.icthh.xm.actions.BrowserCallback
import com.icthh.xm.domain.permission.dto.PermissionMatrixDTO
import com.icthh.xm.domain.permission.dto.RoleMatrixDTO
import com.icthh.xm.editors.VaadinEditor
import com.icthh.xm.editors.WebEditor
import com.icthh.xm.service.getLocalBranches
import com.icthh.xm.service.getRepository
import com.icthh.xm.service.getSettings
import com.icthh.xm.service.getTenantName
import com.icthh.xm.service.permission.TenantRoleService
import com.icthh.xm.utils.logger
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.Alarm
import com.intellij.util.Alarm.ThreadToUse.SWING_THREAD
import com.vaadin.data.provider.DataProvider
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ValueChangeMode.TIMEOUT
import com.vaadin.ui.CheckBox
import com.vaadin.ui.Component
import com.vaadin.ui.Grid
import com.vaadin.ui.Grid.SelectionMode.NONE
import com.vaadin.ui.VerticalLayout


class WebRoleMatrixEditor(val currentProject: Project, val currentFile: VirtualFile): WebEditor(currentProject, "role-matrix", "Role matrix") {

    val mapper = jacksonObjectMapper()

    var documentAlarm = Alarm(SWING_THREAD, this)
    var selectedMs: String? = null
    var permissionToSearch: String? = null

    fun viewComponent(): Component {

        val tenantName = currentFile.getTenantName(project)
        val tenantRoleService = TenantRoleService(tenantName, currentProject) { writeAction(it) }
        val roleMatrix = tenantRoleService.getRoleMatrix()
        val msNames = getPermission(roleMatrix).map { it.msName }.toSet()

        lateinit var grid: Grid<PermissionMatrixDTO>
        val rootLayout = VerticalLayout().apply {
            setSizeFull()

            horizontalLayout {
                comboBox<String> {
                    setItems(msNames)
                    addValueChangeListener {
                        selectedMs = it.value
                        grid.refresh()
                    }
                }
                textField {
                    setWidth(300f, PIXELS)
                    onEnterPressed {
                        permissionToSearch = it.value
                        grid.refresh()
                    }
                    addValueChangeListener {
                        permissionToSearch = it.value
                        grid.refresh()
                    }
                    valueChangeMode = TIMEOUT
                }
            }
            grid = grid {
                expandRatio = 1f;
                setColumnReorderingAllowed(true);
                setSelectionMode(NONE)
                setSizeFull()


                val privilegeKey = addColumn{ it.privilegeKey }
                privilegeKey.setCaption("Privilege key")
                privilegeKey.isSortable = true

                val msName = addColumn{ it.msName }
                msName.setCaption("MS name")
                msName.isHidable = true

                roleMatrix.roles.forEachIndexed { index, role ->
                    val column = addComponentColumn { column ->
                        CheckBox().apply {
                            value = column.roles.contains(role)
                            addValueChangeListener {
                                if (it.value) {
                                    column.roles.add(role)
                                } else {
                                    column.roles.remove(role)
                                }
                                getApplication().executeOnPooledThread {
                                    tenantRoleService.updateRoleMatrix(roleMatrix.copy())
                                }
                            }
                        }
                    }
                    column.isSortable = false
                    column.caption = role
                    column.isHidable = true
                    if (index != 0) {
                        column.isHidden = true
                    }
                }
                setDataProvider( DataProvider.fromCallbacks(
                    { query ->
                        val permission = getPermission(roleMatrix)
                        val start = query.getOffset()
                        val end = Math.min(query.getLimit() + start, permission.size)
                        permission.toList().subList(start, end).stream()
                    },
                    { getPermission(roleMatrix).size }
                ))
            }
        }
        return rootLayout
    }

    private fun getPermission(roleMatrix: RoleMatrixDTO): List<PermissionMatrixDTO> {
        return roleMatrix.permissions
            .filter { if (selectedMs == null) true else it.msName.equals(selectedMs) }
            .filter { it.privilegeKey.contains(permissionToSearch ?: "") }
    }

    fun writeAction(update: () -> Unit) {
        if (documentAlarm.isDisposed) {
            return
        }

        documentAlarm.cancelAllRequests()
        documentAlarm.addRequest({
            getApplication().runWriteAction {
                update.invoke()
            }
        }, 50)
    }

    override fun callbacks(browser: JBCefBrowser): List<BrowserCallback> {
        return listOf(
            BrowserCallback("componentReady") {body, pipe ->


            }
        )
    }

    override fun getFile(): VirtualFile {
        return currentFile
    }

}

