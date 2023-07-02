package fr.socolin.rider.plugins.hsf.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import fr.socolin.rider.plugins.hsf.HsfIcons
import fr.socolin.rider.plugins.hsf.settings.HsfSettingsConfigurable

class OpenPluginSettingsAction : AnAction(
    "Highlight Special Files Settings",
    "Open settings window to configure the rules of Highlight Special Files",
    HsfIcons.Actions.OpenSettings
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.project
        project?.let {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, HsfSettingsConfigurable::class.java)
        }
    }
}