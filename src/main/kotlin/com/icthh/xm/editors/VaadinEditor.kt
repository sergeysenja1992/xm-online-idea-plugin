package com.icthh.xm.editors

import com.icthh.xm.ViewRegistry
import com.icthh.xm.ViewServer
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.vaadin.navigator.View
import com.vaadin.ui.Component
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import java.awt.Color
import java.awt.Dimension
import java.beans.PropertyChangeListener
import javax.swing.JComponent

abstract class VaadinEditor(val project: Project,
                   val viewName: String,
                   val title: String,
                   val dimension: Dimension = Dimension(500, 500)): FileEditor {

    init {
        ViewServer.startServer()
    }

    override fun getComponent(): JComponent {
        ViewRegistry.registry(viewName, object: View {
            override fun getViewComponent(): Component {
                return viewComponent()
            }
        })

        val fxPanel = JFXPanel()
        //fxPanel.preferredSize = dimension
        Platform.setImplicitExit(false)
        Platform.runLater {
            val root = StackPane()
            val scene = Scene(root)
            val webView = WebView()
            webView.engine.load("http://localhost:${ViewServer.serverPort}/#!$viewName")
            //root.setMinSize(dimension.getWidth(), dimension.getHeight())
            root.getChildren().add(webView)
            fxPanel.scene = scene
        }
        return fxPanel
    }

    abstract fun viewComponent() : Component

    override fun dispose() {
        ViewRegistry.unregistry(viewName)
    }

    override fun isModified(): Boolean {
        return false;
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun getName(): String {
        return title
    }

    override fun setState(state: FileEditorState) {

    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
    }

    override fun <T : Any?> getUserData(key: Key<T>): T? {
        return null
    }

    override fun selectNotify() {

    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {

    }

    override fun getCurrentLocation(): FileEditorLocation? {
        return null
    }

    override fun deselectNotify() {

    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {

    }

}
