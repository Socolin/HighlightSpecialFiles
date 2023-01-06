package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.containers.SortedList
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.HsfRuleConfigurationHelper
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import java.util.*
import javax.swing.JPanel
import kotlin.collections.HashSet

class RulesComponent(
    private val project: Project,
    private val lifetime: Lifetime,
) {
    val panel: DialogPanel
    private var rulesPanel: JPanel = JPanel(VerticalFlowLayout())
    private val ruleComponents: SortedList<RuleComponent> = SortedList { a, b -> a.order - b.order }

    init {
        panel = panel {
            group("Rules") {
                row {
                    button("Add Rule") { addNewRule() }
                }
                row {
                    cell(rulesPanel).align(AlignX.FILL)
                }
            }
        }
    }

    private fun addNewRule() {
        addRule(HsfRuleConfiguration(UUID.randomUUID(), "^$", 0))
    }

    fun setRules(rules: Collection<HsfRuleConfiguration>) {
        val diffResult = HsfRuleConfigurationHelper.computeDiffBetweenRules(getRules(), rules)

        val removedRuleIds = HashSet(diffResult.removedRules.map { r -> r.id })
        this.ruleComponents.removeIf { r -> removedRuleIds.contains(r.ruleId) }

        for (addedRule in diffResult.addedRules) {
            addRule(addedRule)
        }

        for (updatedRule in diffResult.updatedRules) {
            val rc = this.ruleComponents.find { r -> r.ruleId == updatedRule.id }
            rc?.setRule(updatedRule)
        }

        this.rulesPanel.removeAll()
        for (ruleComponent in this.ruleComponents) {
            this.rulesPanel.add(ruleComponent)
        }
    }

    fun getRules(): List<HsfRuleConfiguration> {
        return ruleComponents.map { c -> c.getRule() }
    }

    private fun addRule(rule: HsfRuleConfiguration) {
        val ruleComponent = RuleComponent(rule, HsfIconManager.getInstance(project))
        this.rulesPanel.add(ruleComponent)
        ruleComponents.add(ruleComponent)

        ruleComponent.onDelete.advise(lifetime) { deletedRule ->
            run {
                val rc = this.rulesPanel.components.find { c -> (c as RuleComponent).ruleId == deletedRule.id }
                this.rulesPanel.remove(rc)
                ruleComponents.remove(rc)
                rulesPanel.revalidate()
            }
        }

        rulesPanel.revalidate()
    }

}