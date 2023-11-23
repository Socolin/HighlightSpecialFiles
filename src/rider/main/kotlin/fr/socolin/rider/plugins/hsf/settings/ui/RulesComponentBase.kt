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
import fr.socolin.rider.plugins.hsf.settings.models.IHsfRuleConfiguration
import java.util.*
import javax.swing.JPanel

abstract class RulesComponentBase<TRuleConfiguration : IHsfRuleConfiguration<TRuleConfiguration>>(
    protected val project: Project,
    protected val lifetime: Lifetime,
) {
    abstract val title: String
    val panel: DialogPanel
    private var rulesPanel: JPanel = JPanel(VerticalFlowLayout())
    private val ruleComponents: SortedList<RuleComponentBase<TRuleConfiguration>> =
        SortedList { a, b -> a.order - b.order }

    init {
        panel = panel {
            group(title) {
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
        addRule(createNewRuleConfiguration())
    }

    protected abstract fun createNewRuleConfiguration(): TRuleConfiguration;

    fun setRules(rules: Collection<TRuleConfiguration>) {
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

    fun getRules(): List<TRuleConfiguration> {
        return ruleComponents.map { c -> c.getRule() }
    }

    private fun addRule(rule: TRuleConfiguration) {
        val ruleComponent = createRuleComponent(rule)
        this.rulesPanel.add(ruleComponent)
        ruleComponents.add(ruleComponent)

        ruleComponent.onDelete.advise(lifetime) { deletedRule ->
            run {
                val rc = this.rulesPanel.components.find { c -> (c as RuleComponentBase<*>).ruleId == deletedRule.id }
                this.rulesPanel.remove(rc)
                ruleComponents.remove(rc)
                rulesPanel.revalidate()
            }
        }

        ruleComponent.onDuplicate.advise(lifetime) { duplicatedRule ->
            run {
                val newRuleConfiguration = createRuleConfigurationFrom(duplicatedRule)
                val newRuleComponent = createRuleComponent(newRuleConfiguration)
                val index =
                    this.rulesPanel.components.indexOfFirst { c -> (c as RuleComponentBase<*>).ruleId == duplicatedRule.id }
                if (index != -1)
                    this.rulesPanel.add(newRuleComponent, index + 1)
                else
                    this.rulesPanel.add(newRuleComponent)
                ruleComponents.add(newRuleComponent)
                rulesPanel.revalidate()
            }
        }

        rulesPanel.revalidate()
    }

    abstract fun createRuleComponent(rule: TRuleConfiguration): RuleComponentBase<TRuleConfiguration>

    abstract fun createRuleConfigurationFrom(duplicatedRule: TRuleConfiguration): TRuleConfiguration
}