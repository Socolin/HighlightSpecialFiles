package fr.socolin.rider.plugins.hsf.virtual_folder.node

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import com.jetbrains.rider.projectView.views.*
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.ProjectModelEntityReference
import com.jetbrains.rider.projectView.workspace.getVirtualFileAsContentRoot
import com.jetbrains.rider.projectView.workspace.impl.WorkspaceEntityErrorsSupport
import com.jetbrains.rider.projectView.workspace.toReference
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule
import java.util.*

class VirtualFolderNode(
    project: Project,
    private val nodesToGroup: List<SolutionExplorerModelNode>,
    val settings: SolutionExplorerViewSettings,
    private val parentEntity: VirtualFolderProjectModelEntity,
    val rule: HsfHighlightingRule,
    val id: String,
) : SolutionViewNode<String>(project, id), ClickableNode, SolutionViewEntityOwner, SolutionViewCustomEntityContainer {

    override val entity: ProjectModelEntity
        get() {
            return parentEntity
        }

    override val entityReference: ProjectModelEntityReference
        get() {
            return parentEntity.toReference()
        }

    override fun calculateChildren(): MutableList<AbstractTreeNode<*>> {
        return ArrayList(nodesToGroup)
    }

    override fun update(presentation: PresentationData) {
        presentation.setIcon(rule.virtualFolderIcon.icon ?: AllIcons.Nodes.Folder)
        presentation.addText(rule.virtualFolderName ?: "<No title>", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    override fun hasProblemFileBeneath(): Boolean {
        if (!Registry.`is`("projectView.showHierarchyErrors"))
            return false

        for (abstractTreeNode in nodesToGroup) {
            val entity = abstractTreeNode.entityReference.getEntity(project)
            if (entity != null)
                if (WorkspaceEntityErrorsSupport.getInstance(myProject).hasErrors(entity))
                    return true
        }
        return false
    }

    override fun navigate(requestFocus: Boolean) {
    }

    override fun canNavigateToSource(): Boolean {
        return false
    }

    override fun expandOnDoubleClick(): Boolean {
        return true
    }

    override fun showInplaceComments(): Boolean {
        return true
    }

    override fun getVirtualFile() = null

    override fun contains(file: VirtualFile): Boolean {
        for (node in this.nodesToGroup) {
            val nodeVirtualFile = node.entity?.getVirtualFileAsContentRoot()
            if (nodeVirtualFile != null) {
                if (file.path === nodeVirtualFile.path)
                    return true
                if (VfsUtil.isAncestor(nodeVirtualFile, file, false))
                    return true
            }
        }
        return false
    }

    override fun contains(entity: ProjectModelEntityReference): Boolean {
        val entityVirtualUrl = entity.getEntity(project)?.url ?: return false;
        for (node in this.nodesToGroup) {
            if (node.entity?.url == entityVirtualUrl)
                return true;
        }
        return false;
    }

    override fun getBackgroundColor() = null

    override fun getName(): String = rule.virtualFolderName ?: "INVALID"

    override fun getLinkData(): ClickableNodeLink? {
        return null
    }

    override fun processLinkClick() {
    }

    override fun toString() = name
}