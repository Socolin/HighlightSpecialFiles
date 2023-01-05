package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.models.HsfRuleConfiguration
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
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
        val panel = JPanel(GridBagLayout())
        val rulesConstraint = GridBagConstraints()
        rulesConstraint.gridwidth = 1
        rulesConstraint.gridheight = 1
        rulesConstraint.weightx = 1.0
        rulesConstraint.weighty = 1.0
        rulesConstraint.anchor = GridBagConstraints.FIRST_LINE_START
        rulesConstraint.fill = GridBagConstraints.HORIZONTAL
        panel.add(rulesComponent, rulesConstraint)
        return panel
    }

    fun setRules(rules: Collection<HsfRuleConfiguration>) {
        rulesComponent.setRules(rules)
    }

    fun getRules(): List<HsfRuleConfiguration> {
        return rulesComponent.getRules()
    }
}