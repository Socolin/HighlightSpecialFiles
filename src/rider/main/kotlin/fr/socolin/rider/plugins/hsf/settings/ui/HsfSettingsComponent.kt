package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import javax.swing.JPanel

class HsfSettingsComponent(
    project: Project,
    lifetime: Lifetime
) {
    private val rulesComponent: RulesComponent

    init {
        rulesComponent = RulesComponent(project, lifetime)
    }

    fun getPanel(): JPanel {
        return panel {
            row {
                cell(rulesComponent.panel).align(AlignX.FILL)
            }
        }
    }

    fun setRules(rules: Collection<HsfRuleConfiguration>) {
        rulesComponent.setRules(rules)
    }

    fun getRules(): List<HsfRuleConfiguration> {
        return rulesComponent.getRules()
    }
}