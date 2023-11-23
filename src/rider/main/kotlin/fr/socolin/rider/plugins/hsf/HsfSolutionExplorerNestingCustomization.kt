package fr.socolin.rider.plugins.hsf

import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.workspace.storage.EntityReference
import com.jetbrains.rider.model.RdProjectModelItemDescriptor
import com.jetbrains.rider.projectView.views.NestingNode
import com.jetbrains.rider.projectView.views.SolutionViewNode
import com.jetbrains.rider.projectView.views.getVirtualFile
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerFileNode
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.ProjectModelEntityReference
import fr.socolin.rider.plugins.hsf.models.HsfNestingRule
import fr.socolin.rider.plugins.hsf.virtual_folder.node.SolutionExplorerModelNodeWithNesting

class HsfSolutionExplorerNestingCustomization(project: Project) : SolutionExplorerCustomization(project) {
    private val hsfActiveNestingRuleManager: HsfActiveNestingRuleManager =
        HsfActiveNestingRuleManager.getInstance(project)

    override fun modifyChildren(
        entity: ProjectModelEntity,
        settings: SolutionExplorerViewSettings,
        children: MutableList<AbstractTreeNode<*>>
    ) {
        var virtualNodes: ArrayList<AbstractTreeNode<*>>? = null;
        for (rule in hsfActiveNestingRuleManager.rules) {
            val filesToGroup = getMatchingNodes<AbstractTreeNode<*>>(rule, children);

            for ((key, files) in filesToGroup) {
                val parentMatchingNodes = ArrayList<MatchingNodes<AbstractTreeNode<*>>>()
                val childrenMatchingNodes = ArrayList<MatchingNodes<AbstractTreeNode<*>>>()
                for (file in files) {
                    if (file.childPart == null) {
                        parentMatchingNodes.add(file)
                    } else {
                        childrenMatchingNodes.add(file)
                    }
                }
                if (parentMatchingNodes.count() == 1 && childrenMatchingNodes.isNotEmpty()) {
                    val parentNode = parentMatchingNodes[0].node;
                    val childrenNodes = childrenMatchingNodes.map { i -> i.node }
                    val parentVirtualFile = parentNode.getVirtualFile();
                    if (parentNode is SolutionExplorerModelNode && parentVirtualFile != null) {
                        val nestedNodes = ArrayList<AbstractTreeNode<*>>(childrenNodes.size);
                        children.remove(parentNode)
                        for (childrenNode in childrenNodes) {
                            children.remove(childrenNode)
                            nestedNodes.add(childrenNode as AbstractTreeNode<*>)
                        }
                        if (virtualNodes == null)
                            virtualNodes = ArrayList();
                        val nameAmbiguity = false; // FIXME: No way to get this from the parentNode
                        virtualNodes.add(
                            SolutionExplorerModelNodeWithNesting(project, parentNode.entityReference, parentNode.settings, nameAmbiguity, nestedNodes)
                        )
                    }
                }
            }
        }

        if (virtualNodes != null)
            children.addAll(virtualNodes)
        super.modifyChildren(entity, settings, children)
    }
    override fun modifyChildren(
        virtualFile: VirtualFile,
        settings: SolutionExplorerViewSettings,
        children: MutableList<AbstractTreeNode<*>>
    ) {

        var virtualNodes: ArrayList<AbstractTreeNode<*>>? = null;
        for (rule in hsfActiveNestingRuleManager.rules) {
            val filesToGroup = getMatchingNodes<AbstractTreeNode<*>>(rule, children);

            for ((key, files) in filesToGroup) {
                val parentMatchingNodes = ArrayList<MatchingNodes<AbstractTreeNode<*>>>()
                val childrenMatchingNodes = ArrayList<MatchingNodes<AbstractTreeNode<*>>>()
                for (file in files) {
                    if (file.childPart == null) {
                        parentMatchingNodes.add(file)
                    } else {
                        childrenMatchingNodes.add(file)
                    }
                }
                if (parentMatchingNodes.count() == 1 && childrenMatchingNodes.isNotEmpty()) {
                    val parentNode = parentMatchingNodes[0].node;
                    val childrenNodes = childrenMatchingNodes.map { i -> i.node }
                    if (parentNode is SolutionExplorerFileNode) {
                        val nestedNodes = ArrayList<NestingNode<VirtualFile>>(childrenNodes.size);
                        nestedNodes.addAll(parentNode.nestedNodes)
                        for (childrenNode in childrenNodes) {
                            children.remove(childrenNode)
                            val virtualFile = childrenNode.getVirtualFile();
                            if (virtualFile != null) {
                                nestedNodes.add(NestingNode(childrenNode.name ?: virtualFile.name, virtualFile))
                            }
                        }
                        children.remove(parentNode)
                        if (virtualNodes == null)
                            virtualNodes = ArrayList();
                        virtualNodes.add(
                            SolutionExplorerFileNode(project, parentNode.virtualFile, nestedNodes, parentNode.settings, parentNode.isRoot, parentNode.isAttachedFolder)
                        )
                    }
                }
            }
        }

        if (virtualNodes != null)
            children.addAll(virtualNodes)
        super.modifyChildren(virtualFile, settings, children)
    }

    private inline fun <reified T : AbstractTreeNode<*>> getMatchingNodes(
        rule: HsfNestingRule,
        nodes: MutableList<AbstractTreeNode<*>>
    ): HashMap<String, ArrayList<MatchingNodes<T>>> {
        val filesToGroup = HashMap<String, ArrayList<MatchingNodes<T>>>();
        for (node in nodes) {
            if (node !is T) continue;
            val name = node.name ?: continue;
            val match = rule.pattern.matcher(name)
            if (match.matches()) {
                val groupBy = match.group("groupBy") ?: continue;
                val childPart = match.group("childPart")

                val matchedNodes = filesToGroup.getOrPut(groupBy) { ArrayList() }
                matchedNodes.add(MatchingNodes(groupBy, childPart, node))
            }
        }

        return filesToGroup;
    }
}

class MatchingNodes<T>(val groupBy: String, val childPart: String?, val node: T) {

}