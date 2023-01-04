package fr.socolin.rider.plugins.hsf.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.ConfigurableName
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import fr.socolin.rider.plugins.hsf.settings.ui.ProjectSettingsComponent
import javax.swing.JComponent

class ProjectSettingsConfigurable(private var project: Project) : Configurable {
    private var mySettingsComponent: ProjectSettingsComponent? = null
    private val lifetime: LifetimeDefinition = LifetimeDefinition()

    override fun getDisplayName(): @ConfigurableName String {
        return "Highlight Special Files"
    }

    override fun createComponent(): JComponent {
        val settingsComponent = ProjectSettingsComponent(project, lifetime)
        mySettingsComponent = settingsComponent
        return settingsComponent.getPanel()
    }

    override fun isModified(): Boolean {
        val settingsComponent = mySettingsComponent ?: return false
        val settings: ProjectSettingsStorageService = ProjectSettingsStorageService.getInstance(project)
        var modified = false
        modified = modified or settings.state.rulesManager.isDifferentFrom(settingsComponent.getRules())
        return modified
    }

    override fun reset() {
        val settings: ProjectSettingsStorageService = ProjectSettingsStorageService.getInstance(project)
        mySettingsComponent!!.setRules(settings.state.rulesManager.rules)
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings: ProjectSettingsStorageService = ProjectSettingsStorageService.getInstance(project)
        settings.state.rulesManager.updateRules(mySettingsComponent!!.getRules())
    }

    override fun disposeUIResources() {
        lifetime.terminate()
    }
}