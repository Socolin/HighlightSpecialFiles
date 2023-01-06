package fr.socolin.rider.plugins.hsf

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.model.RdDependencyFolderDescriptor
import com.jetbrains.rider.model.RdProjectFolderDescriptor
import com.jetbrains.rider.model.RdProjectModelItemDescriptor
import com.jetbrains.rider.model.RdSolutionFolderDescriptor
import com.jetbrains.rider.projectView.ProjectModelViewUpdater
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.getFile
import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.HsfConfigurationManager
import java.awt.Color
import java.util.regex.Pattern

class HsfSolutionExplorerCustomization(project: Project) : SolutionExplorerCustomization(project) {
    private val rulesConfigurationManager: HsfConfigurationManager =
        HsfConfigurationManager.getInstance(project)
    private val hsfIconManager: HsfIconManager = HsfIconManager.getInstance(project)

    private val activeRules: ArrayList<HsfHighlightingRule> = ArrayList()
    private val activeRulesWithPriority: ArrayList<HsfHighlightingRule> = ArrayList()

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

    private fun updateRule(previousRule: HsfRuleConfiguration, newRule: HsfRuleConfiguration) {
        val activeRule = createRuleFromConfig(newRule)
        for ((index, rule) in activeRules.withIndex()) {
            if (rule.id == previousRule.id) {
                activeRules[index] = activeRule
                break
            }
        }
        if (previousRule.priority != null) {
            for ((index, rule) in activeRulesWithPriority.withIndex()) {
                if (rule.id == previousRule.id) {
                    if (activeRule.priority != null)
                        activeRulesWithPriority[index] = activeRule
                    else
                        activeRulesWithPriority.removeAt(index)
                    break
                }
            }
        } else if (newRule.priority != null) {
            // FIXME: Should insert at correct place
            activeRulesWithPriority.add(activeRule)
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
        val activeRule = createRuleFromConfig(ruleConfig)
        activeRules.add(activeRule)
        if (activeRule.priority != null)
            activeRulesWithPriority.add(activeRule)
    }

    private fun createRuleFromConfig(
        ruleConfig: HsfRuleConfiguration,
    ) = HsfHighlightingRule(
        ruleConfig.id,
        Pattern.compile(ruleConfig.pattern),
        hsfIconManager.getIcon(ruleConfig.iconId),
        ruleConfig.priority,
        ruleConfig.annotationText,
        HsfAnnotationTextStyles.annotationsStyles.getOrDefault(
            ruleConfig.annotationStyle,
            SimpleTextAttributes.GRAYED_ATTRIBUTES
        ),
        colorFromHex(ruleConfig.foregroundColorHex)
    )

    override fun updateNode(presentation: PresentationData, entity: ProjectModelEntity) {
        entity.getFile()
        for (rule in activeRules) {
            val match = rule.pattern.matcher(entity.name)
            if (match.matches()) {
                applyRule(rule, presentation)
            }
        }

        super.updateNode(presentation, entity)
    }

    private fun applyRule(rule: HsfHighlightingRule, presentation: PresentationData) {
        val icon = rule.icon.icon
        if (icon != null)
            presentation.setIcon(icon)

        val annotationText = rule.annotationText
        if (annotationText != null)
            presentation.addText(" $annotationText", rule.annotationStyle)
        if (rule.foregroundColor != null)
            presentation.forcedTextForeground = rule.foregroundColor
    }

    override fun compareNodes(x: ProjectModelEntity, y: ProjectModelEntity): Int {
        var xPriority = 0
        var yPriority = 0
        if (isFolder(x.descriptor))
            xPriority = 1000
        if (isFolder(y.descriptor))
            yPriority = 1000
        for (rule in activeRulesWithPriority) {
            val xMatch = rule.pattern.matcher(x.name)
            if (xMatch.matches()) {
                if (rule.priority != null)
                    xPriority = rule.priority
                break
            }
        }
        for (rule in activeRulesWithPriority) {
            val yMatch = rule.pattern.matcher(y.name)
            if (yMatch.matches()) {
                if (rule.priority != null)
                    yPriority = rule.priority
                break
            }
        }
        if (xPriority > yPriority)
            return -1
        if (xPriority < yPriority)
            return 1

        return super.compareNodes(x, y)
    }

    private fun isFolder(descriptor: RdProjectModelItemDescriptor) =
        (descriptor is RdProjectFolderDescriptor
                || descriptor is RdDependencyFolderDescriptor
                || descriptor is RdSolutionFolderDescriptor)

    companion object {
        private fun colorFromHex(hex: String?): Color? {
            if (hex == null)
                return null
            return try {
                Color.decode(hex)
            } catch (e: Exception) {
                null
            }
        }
    }
}