package fr.socolin.rider.plugins.hsf

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.containers.SortedList
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rider.projectView.ProjectModelViewUpdater
import fr.socolin.rider.plugins.hsf.models.HsfNestingRule
import fr.socolin.rider.plugins.hsf.settings.HsfNestingRuleConfigurationManager
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import java.util.regex.Pattern

@Service(Service.Level.PROJECT)
class HsfActiveNestingRuleManager(project: Project) {
    private val rulesConfigurationManager: HsfNestingRuleConfigurationManager =
        HsfNestingRuleConfigurationManager.getInstance(project)

    val rules: List<HsfNestingRule>
        get() {
            return activeRules
        }

    private val activeRules: SortedList<HsfNestingRule> = SortedList { a, b -> a.order - b.order }

    init {
        for (rule in rulesConfigurationManager.getOrderedRules()) {
            addRule(rule)
        }

        rulesConfigurationManager.ruleAdded.advise(project.lifetime) { r ->
            run {
                addRule(r)
                ProjectModelViewUpdater.fireUpdate(project) { u -> u.updateAll() }
            }
        }
        rulesConfigurationManager.ruleChanged.advise(project.lifetime) { r ->
            run {
                updateRule(r.first, r.second)
                ProjectModelViewUpdater.fireUpdate(project) { u -> u.updateAll() }
            }
        }
        rulesConfigurationManager.ruleDeleted.advise(project.lifetime) { r ->
            run {
                removeRule(r)
                ProjectModelViewUpdater.fireUpdate(project) { u -> u.updateAll() }
            }
        }
    }

    private fun removeRule(deletedRule: HsfNestingRuleConfiguration) {
        for ((index, rule) in activeRules.withIndex()) {
            if (rule.id == deletedRule.id) {
                activeRules.removeAt(index)
                break
            }
        }
    }

    private fun addRule(ruleConfig: HsfNestingRuleConfiguration) {
        if (ruleConfig.isDisabled)
            return
        val activeRule = createRuleFromConfig(ruleConfig)
        activeRules.add(activeRule)
    }

    private fun updateRule(previousRule: HsfNestingRuleConfiguration, newRule: HsfNestingRuleConfiguration) {
        if (previousRule.isDisabled && newRule.isDisabled)
            return
        if (previousRule.isDisabled) {
            addRule(newRule)
            return
        }
        if (newRule.isDisabled) {
            removeRule(previousRule)
            return
        }
        val activeRule = createRuleFromConfig(newRule)
        for ((index, rule) in activeRules.withIndex()) {
            if (rule.id == previousRule.id) {
                activeRules.removeAt(index)
                activeRules.add(activeRule)
                break
            }
        }
    }

    private fun createRuleFromConfig(
        ruleConfig: HsfNestingRuleConfiguration,
    ) = HsfNestingRule(
        ruleConfig.id,
        Pattern.compile(ruleConfig.pattern),
        ruleConfig.order
    )

    companion object {
        fun getInstance(project: Project): HsfActiveNestingRuleManager {
            return project.getService(HsfActiveNestingRuleManager::class.java)
        }
    }
}