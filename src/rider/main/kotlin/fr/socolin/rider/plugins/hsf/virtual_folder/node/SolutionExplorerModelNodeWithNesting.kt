package fr.socolin.rider.plugins.hsf.virtual_folder.node

import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.views.SolutionViewCustomEntityContainer
import com.jetbrains.rider.projectView.views.getVirtualFile
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode
import com.jetbrains.rider.projectView.workspace.ProjectModelEntityReference

class SolutionExplorerModelNodeWithNesting(
    project: Project,
    entityReference: ProjectModelEntityReference,
    settings: SolutionExplorerViewSettings,
    nameAmbiguity: Boolean,
    private val nestingNodes: List<AbstractTreeNode<*>>
) : SolutionExplorerModelNode(project, entityReference, settings, nameAmbiguity), SolutionViewCustomEntityContainer {
    override fun calculateChildren(): MutableList<AbstractTreeNode<*>> {
        val children = super.calculateChildren()
        children.addAll(nestingNodes)
        return children;
    }

    override fun contains(entity: ProjectModelEntityReference): Boolean {
        val entityVirtualUrl = entity.getEntity(project)?.url ?: return false;
        return nestingNodes.any { n -> n.getVirtualFile()?.url == entityVirtualUrl.url }
    }
}