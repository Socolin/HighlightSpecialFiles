package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRuleConfiguration
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel

class ProjectSettingsComponent(
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

    fun setRules(rules: List<HsfHighlightingRuleConfiguration>) {
        rulesComponent.setRules(rules)
    }

    fun getRules(): List<HsfHighlightingRuleConfiguration> {
        return rulesComponent.getRules()
    }
}