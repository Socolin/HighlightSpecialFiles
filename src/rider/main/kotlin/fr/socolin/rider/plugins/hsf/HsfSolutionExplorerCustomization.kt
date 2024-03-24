package fr.socolin.rider.plugins.hsf

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rider.model.RdDependencyFolderDescriptor
import com.jetbrains.rider.model.RdProjectFolderDescriptor
import com.jetbrains.rider.model.RdProjectModelItemDescriptor
import com.jetbrains.rider.model.RdSolutionFolderDescriptor
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerFileNode
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import fr.socolin.rider.plugins.hsf.actions.OpenPluginSettingsAction
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule
import fr.socolin.rider.plugins.hsf.virtual_folder.file.VirtualFolderFileNode
import fr.socolin.rider.plugins.hsf.virtual_folder.file.VirtualFolderVirtualFile
import fr.socolin.rider.plugins.hsf.virtual_folder.node.VirtualFolderItemDescriptor
import fr.socolin.rider.plugins.hsf.virtual_folder.node.VirtualFolderNode
import fr.socolin.rider.plugins.hsf.virtual_folder.node.VirtualFolderProjectModelEntity

class HsfSolutionExplorerCustomization(project: Project) : SolutionExplorerCustomization(project) {
    private val hsfActiveRuleManager: HsfActiveRuleManager = HsfActiveRuleManager.getInstance(project)

    override fun addPrimaryToolbarActions(actionGroup: DefaultActionGroup) {
        actionGroup.add(OpenPluginSettingsAction())
        super.addPrimaryToolbarActions(actionGroup)
    }


    override fun updateNode(presentation: PresentationData, entity: ProjectModelEntity) {
        for (rule in hsfActiveRuleManager.rules) {
            val match = rule.pattern.matcher(entity.name)
            if (match.matches()) {
                applyRule(rule, presentation)
            }
        }

        super.updateNode(presentation, entity)
    }

    override fun updateNode(presentation: PresentationData, virtualFile: VirtualFile) {
        for (rule in hsfActiveRuleManager.rules) {
            val match = rule.pattern.matcher(virtualFile.name)
            if (match.matches()) {
                applyRule(rule, presentation)
            }
        }
        super.updateNode(presentation, virtualFile)
    }

    override fun modifyChildren(
        virtualFile: VirtualFile,
        settings: SolutionExplorerViewSettings,
        children: MutableList<AbstractTreeNode<*>>
    ) {
        super.modifyChildren(virtualFile, settings, children)
        var virtualNodes: ArrayList<AbstractTreeNode<*>>? = null;
        for (rule in hsfActiveRuleManager.rules) {
            if (!rule.groupInVirtualFolder) continue;

            val filesToGroup = getMatchingNodes<AbstractTreeNode<*>>(rule, children);
            if (filesToGroup.isNotEmpty()) {
                if (virtualNodes == null)
                    virtualNodes = ArrayList()
                val firstElement = filesToGroup.first()

                if (firstElement is SolutionExplorerFileNode) {
                    val virtualFolder = VirtualFolderFileNode(
                        firstElement.project,
                        VirtualFolderVirtualFile(virtualFile, rule),
                        rule,
                        filesToGroup
                    )
                    children.removeAll(filesToGroup)
                    virtualNodes.add(virtualFolder)
                }
            }
        }

        if (virtualNodes != null)
            children.addAll(virtualNodes)
        super.modifyChildren(virtualFile, settings, children)
    }

    override fun modifyChildren(
        entity: ProjectModelEntity,
        settings: SolutionExplorerViewSettings,
        children: MutableList<AbstractTreeNode<*>>
    ) {
        var virtualNodes: ArrayList<AbstractTreeNode<*>>? = null;
        for (rule in hsfActiveRuleManager.rules) {
            if (!rule.groupInVirtualFolder) continue;

            val filesToGroup = getMatchingNodes<SolutionExplorerModelNode>(rule, children);
            if (filesToGroup.isNotEmpty()) {
                if (virtualNodes == null)
                    virtualNodes = ArrayList()
                val virtualFolder = VirtualFolderNode(
                    project,
                    filesToGroup,
                    settings,
                    VirtualFolderProjectModelEntity(entity, rule),
                    rule,
                )
                children.removeAll(filesToGroup)
                virtualNodes.add(virtualFolder)
            }
        }

        if (virtualNodes != null)
            children.addAll(virtualNodes)
        super.modifyChildren(entity, settings, children)
    }

    private inline fun <reified T : AbstractTreeNode<*>> getMatchingNodes(rule: HsfHighlightingRule, nodes: MutableList<AbstractTreeNode<*>>): List<T> {
        val filesToGroup = ArrayList<T>();
        for (node in nodes) {
            if (node !is T) continue;
            val name = node.name ?: continue;
            val match = rule.pattern.matcher(name)
            if (match.matches()) {
                filesToGroup.add(node);
            }
        }

        return filesToGroup;
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
        val xPriority = computePriority(x)
        val yPriority = computePriority(y)

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
            for (rule in hsfActiveRuleManager.rulesWithPriority) {
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
}