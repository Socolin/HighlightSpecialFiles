package fr.socolin.rider.plugins.hsf

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.containers.SortedList
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rider.projectView.ProjectModelViewUpdater
import fr.socolin.rider.plugins.hsf.helpers.HsfColorHelper
import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.HsfRuleConfigurationManager
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import java.util.regex.Pattern

@Service(Service.Level.PROJECT)
class HsfActiveRuleManager(project: Project) {

    private val hsfIconManager: HsfIconManager = HsfIconManager.getInstance(project)
    private val rulesConfigurationManager: HsfRuleConfigurationManager = HsfRuleConfigurationManager.getInstance(project)

    val rules : List<HsfHighlightingRule> get() {
        return activeRules;
    }
    val rulesWithPriority : List<HsfHighlightingRule> get() {
        return activeRules;
    }
    private val activeRules: SortedList<HsfHighlightingRule> = SortedList { a, b -> a.order - b.order }
    private val activeRulesWithPriority: SortedList<HsfHighlightingRule> = SortedList { a, b -> a.order - b.order }

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

    private fun removeRule(deletedRule: HsfRuleConfiguration) {
        for ((index, rule) in activeRules.withIndex()) {
            if (rule.id == deletedRule.id) {
                activeRules.removeAt(index)
                break
            }
        }

        if (deletedRule.priority != null) {
            for ((index, rule) in activeRulesWithPriority.withIndex()) {
                if (rule.id == deletedRule.id) {
                    activeRulesWithPriority.removeAt(index)
                    break
                }
            }
        }
    }

    private fun addRule(ruleConfig: HsfRuleConfiguration) {
        if (ruleConfig.isDisabled)
            return;
        val activeRule = createRuleFromConfig(ruleConfig)
        activeRules.add(activeRule)
        if (activeRule.priority != null)
            activeRulesWithPriority.add(activeRule)
    }

    private fun updateRule(previousRule: HsfRuleConfiguration, newRule: HsfRuleConfiguration) {
        if (previousRule.isDisabled && newRule.isDisabled)
            return;
        if (previousRule.isDisabled) {
            addRule(newRule);
            return;
        }
        if (newRule.isDisabled) {
            removeRule(previousRule);
            return;
        }
        val activeRule = createRuleFromConfig(newRule)
        for ((index, rule) in activeRules.withIndex()) {
            if (rule.id == previousRule.id) {
                activeRules.removeAt(index)
                activeRules.add(activeRule);
                break
            }
        }
        if (isRuleModifyingFilePriority(previousRule)) {
            for ((index, rule) in activeRulesWithPriority.withIndex()) {
                if (rule.id == previousRule.id) {
                    if (isRuleModifyingFilePriority(activeRule)) {
                        activeRulesWithPriority.removeAt(index)
                        activeRulesWithPriority.add(activeRule)
                    } else
                        activeRulesWithPriority.removeAt(index)
                    break
                }
            }
        } else if (isRuleModifyingFilePriority(newRule)) {
            activeRulesWithPriority.add(activeRule)
        }
    }

    private fun isRuleModifyingFilePriority(rule: HsfRuleConfiguration): Boolean {
        return rule.priority != null && !rule.groupInVirtualFolder;
    }

    private fun isRuleModifyingFilePriority(rule: HsfHighlightingRule): Boolean {
        return rule.priority != null && !rule.groupInVirtualFolder;
    }

    private fun createRuleFromConfig(
        ruleConfig: HsfRuleConfiguration,
    ) = HsfHighlightingRule(
        ruleConfig.id,
        Pattern.compile(ruleConfig.pattern),
        ruleConfig.order,
        hsfIconManager.getIcon(ruleConfig.iconId),
        ruleConfig.priority,
        ruleConfig.annotationText,
        HsfAnnotationTextStyles.annotationsStyles.getOrDefault(
            ruleConfig.annotationStyle,
            SimpleTextAttributes.GRAYED_ATTRIBUTES
        ),
        HsfColorHelper.colorFromHex(ruleConfig.foregroundColorHex),
        ruleConfig.groupInVirtualFolder,
        hsfIconManager.getIcon(ruleConfig.folderIconId),
        ruleConfig.folderName,
        ruleConfig.filesCountBeforeCreatingVirtualFolder ?: 1,
    )

    companion object {
        fun getInstance(project: Project): HsfActiveRuleManager {
            return project.getService(HsfActiveRuleManager::class.java)
        }
    }
}