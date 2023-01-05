package fr.socolin.rider.plugins.hsf.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.ConfigurableName
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import fr.socolin.rider.plugins.hsf.settings.ui.HsfSettingsComponent
import javax.swing.JComponent

class HsfSettingsConfigurable(private var project: Project) : Configurable {
    private var settingsComponent: HsfSettingsComponent? = null
    private val lifetime: LifetimeDefinition = LifetimeDefinition()
    private val configurationManager = HsfConfigurationManager.getInstance(project)

    override fun getDisplayName(): @ConfigurableName String {
        return "Highlights Special Files"
    }

    override fun createComponent(): JComponent {
        val settingsComponent = HsfSettingsComponent(project, lifetime)
        this.settingsComponent = settingsComponent
        return settingsComponent.getPanel()
    }

    override fun isModified(): Boolean {
        val settingsComponent = settingsComponent ?: return false

        var modified = false
        modified = modified or configurationManager.isDifferentFrom(settingsComponent.getRules())

        return modified
    }

    override fun reset() {
        val settingsComponent = settingsComponent ?: return
        settingsComponent.setRules(configurationManager.getOrderedRules())
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settingsComponent = settingsComponent ?: return
        configurationManager.updateRules(settingsComponent.getRules())
    }

    override fun disposeUIResources() {
        lifetime.terminate()
    }
}