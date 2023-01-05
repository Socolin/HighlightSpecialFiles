package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.ui.bedsl.button
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import java.awt.*
import java.util.*
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

class RulesComponent(
    private val project: Project,
    private val lifetime: Lifetime,
) : JPanel(GridBagLayout()) {
    private var rulesPanel: JPanel = JPanel(GridBagLayout())
    private val ruleComponents: ArrayList<RuleComponent> = ArrayList()

    init {
        val actionConstraint = GridBagConstraints()
        actionConstraint.fill = GridBagConstraints.HORIZONTAL
        actionConstraint.anchor = GridBagConstraints.PAGE_START
        actionConstraint.gridwidth = 1
        actionConstraint.weightx = 1.0
        actionConstraint.gridy = 0
        add(createActionPanel(), actionConstraint)
        val rulesConstraint = GridBagConstraints()
        rulesConstraint.gridy = 1
        actionConstraint.gridwidth = 1
        rulesConstraint.fill = GridBagConstraints.HORIZONTAL
        actionConstraint.anchor = GridBagConstraints.PAGE_START
        rulesConstraint.weightx = 1.0
        add(rulesPanel, rulesConstraint)
    }

    private fun createActionPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))

        val addButton = JButton(AllIcons.General.Add)
        addButton.preferredSize = Dimension(30, 30)
        addButton.addActionListener { _ -> addNewRule() }
        panel.add(addButton)
        return panel
    }

    private fun addNewRule() {
        addRule(HsfRuleConfiguration(UUID.randomUUID(), "^$", 0))
    }

    fun setRules(rules: Collection<HsfRuleConfiguration>) {
        this.rulesPanel.removeAll()
        this.ruleComponents.clear()
        for (rule in rules) {
            addRule(rule)
        }
    }

    fun getRules(): List<HsfRuleConfiguration> {
        val actualRules = ArrayList<HsfRuleConfiguration>()
        for (ruleComponent in ruleComponents) {
            actualRules.add(ruleComponent.getRule())
        }
        return actualRules
    }

    private fun addRule(rule: HsfRuleConfiguration) {
        val ruleConstraint = GridBagConstraints()
        ruleConstraint.gridy = this.ruleComponents.size
        ruleConstraint.anchor = GridBagConstraints.FIRST_LINE_START
        ruleConstraint.weightx = 1.0
        ruleConstraint.fill = GridBagConstraints.HORIZONTAL

        val ruleComponent = RuleComponent(rule, HsfIconManager.getInstance(project))
        this.rulesPanel.add(ruleComponent, ruleConstraint)
        ruleComponents.add(ruleComponent)

        ruleComponent.onDelete.advise(lifetime) { deletedRule ->
            run {
                val rc = this.rulesPanel.components.find { c -> (c as RuleComponent).ruleModel.id == deletedRule.id }
                this.rulesPanel.remove(rc)
                ruleComponents.remove(rc)
                revalidate()
            }
        }

        revalidate()
    }
}