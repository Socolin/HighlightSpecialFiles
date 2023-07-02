package fr.socolin.rider.plugins.hsf

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.containers.SortedList
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.model.RdDependencyFolderDescriptor
import com.jetbrains.rider.model.RdProjectFolderDescriptor
import com.jetbrains.rider.model.RdProjectModelItemDescriptor
import com.jetbrains.rider.model.RdSolutionFolderDescriptor
import com.jetbrains.rider.projectView.ProjectModelViewUpdater
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.toReference
import fr.socolin.rider.plugins.hsf.actions.OpenPluginSettingsAction
import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.HsfConfigurationManager
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import java.awt.Color
import java.util.regex.Pattern

class HsfSolutionExplorerCustomization(project: Project) : SolutionExplorerCustomization(project) {
    private val rulesConfigurationManager: HsfConfigurationManager =
        HsfConfigurationManager.getInstance(project)
    private val hsfIconManager: HsfIconManager = HsfIconManager.getInstance(project)

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

    override fun addPrimaryToolbarActions(actionGroup: DefaultActionGroup) {
        actionGroup.add(OpenPluginSettingsAction())
        super.addPrimaryToolbarActions(actionGroup)
    }

    private fun updateRule(previousRule: HsfRuleConfiguration, newRule: HsfRuleConfiguration) {
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
                    }
                    else
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
        ruleConfig.order,
        hsfIconManager.getIcon(ruleConfig.iconId),
        ruleConfig.priority,
        ruleConfig.annotationText,
        HsfAnnotationTextStyles.annotationsStyles.getOrDefault(
            ruleConfig.annotationStyle,
            SimpleTextAttributes.GRAYED_ATTRIBUTES
        ),
        colorFromHex(ruleConfig.foregroundColorHex),
        ruleConfig.groupInVirtualFolder,
        hsfIconManager.getIcon(ruleConfig.folderIconId),
        ruleConfig.folderName
    )

    override fun updateNode(presentation: PresentationData, entity: ProjectModelEntity) {
        for (rule in activeRules) {
            val match = rule.pattern.matcher(entity.name)
            if (match.matches()) {
                applyRule(rule, presentation)
            }
        }

        super.updateNode(presentation, entity)
    }

    override fun modifyChildren(
        entity: ProjectModelEntity,
        settings: SolutionExplorerViewSettings,
        children: MutableList<AbstractTreeNode<*>>
    ) {
        var virtualNodes: ArrayList<AbstractTreeNode<*>>? = null;
        for (rule in activeRules) {
            if (!rule.groupInVirtualFolder) continue;

            val filesToGroup = ArrayList<AbstractTreeNode<*>>();
            for (child in children) {
                val name = child.name ?: continue;
                val match = rule.pattern.matcher(name)
                if (match.matches()) {
                    filesToGroup.add(child);
                }
            }

            if (filesToGroup.isNotEmpty()) {
                if (virtualNodes == null)
                    virtualNodes = ArrayList()
                val firstElement = filesToGroup.first()

                if (firstElement is SolutionExplorerModelNode) {
                    val firstNodeEntity = firstElement.entity;
                    if (firstNodeEntity != null) {
                        val virtualFolder = VirtualFolderNode(
                            firstElement.project,
                            firstNodeEntity.toReference(),
                            settings,
                            rule,
                            filesToGroup
                        )
                        children.removeAll(filesToGroup)
                        virtualNodes.add(virtualFolder)
                    }
                }
            }
        }

        if (virtualNodes != null)
            children.addAll(virtualNodes)
        super.modifyChildren(entity, settings, children)
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
        var xPriority = computePriority(x)
        var yPriority = computePriority(y)

        if (xPriority > yPriority)
            return -1
        if (xPriority < yPriority)
            return 1

        return super.compareNodes(x, y)
    }

    private fun computePriority(entity: ProjectModelEntity): Int {
        var priority = 0
        if (isFolder(entity.descriptor))
            priority = 1000
        if (entity is VirtualFolderProjectModelEntity)
            priority = entity.rule.priority ?: priority
        else
            for (rule in activeRulesWithPriority) {
                val xMatch = rule.pattern.matcher(entity.name)
                if (xMatch.matches()) {
                    if (rule.priority != null)
                        priority = rule.priority
                    break
                }
            }
        return priority;
    }

    private fun isFolder(descriptor: RdProjectModelItemDescriptor) =
        (descriptor is RdProjectFolderDescriptor
                || descriptor is VirtualFolderItemDescriptor
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