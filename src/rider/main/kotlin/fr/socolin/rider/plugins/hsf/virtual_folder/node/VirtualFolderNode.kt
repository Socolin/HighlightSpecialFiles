package fr.socolin.rider.plugins.hsf.virtual_folder.node

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import com.jetbrains.rider.projectView.nodes.getScopeColor
import com.jetbrains.rider.projectView.views.*
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.jetbrains.rider.projectView.withProjectModelId
import com.jetbrains.rider.projectView.workspace.*
import com.jetbrains.rider.projectView.workspace.impl.WorkspaceEntityErrorsSupport
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule

class VirtualFolderNode(
    project: Project,
    entityReference: ProjectModelEntityReference,
    val settings: SolutionExplorerViewSettings,
    private val rule: HsfHighlightingRule,
    private val filesToGroup: List<AbstractTreeNode<*>>
) :
    SolutionViewNode<ProjectModelEntityReference>(project, entityReference), ClickableNode, SolutionViewEntityOwner {

    override val entity: ProjectModelEntity?
        get() {
            val parentEntity = value.getEntity(myProject)?.parentEntity ?: return null;
            return VirtualFolderProjectModelEntity(parentEntity, rule)
        }

    override val entityReference: ProjectModelEntityReference
        get() = value

    override fun calculateChildren(): MutableList<AbstractTreeNode<*>> {
        return ArrayList(filesToGroup)
    }

    override fun update(presentation: PresentationData) {
        val entity = entity
        if (entity == null) {
            // That means this node is already detached from the model and should be reloaded soon
            presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            presentation.setIcon(AllIcons.Actions.Refresh)
            return
        }

        presentation.setIcon(rule.virtualFolderIcon.icon ?: AllIcons.Nodes.Folder)
        presentation.addText(rule.virtualFolderName ?: "<No title>", SimpleTextAttributes.REGULAR_ATTRIBUTES)

        if (entity.isProjectFile() || entity.isProjectFolder()) {
            virtualFile?.let {
                presentation.addNonIndexedMark(myProject, it)
            }
        }
    }

    override fun hasProblemFileBeneath(): Boolean {
        val entity = entity ?: return false
        return Registry.`is`("projectView.showHierarchyErrors")
                && WorkspaceEntityErrorsSupport.getInstance(myProject).hasErrors(entity)
    }

    override fun navigate(requestFocus: Boolean) {
        val entityId = entity?.getId(myProject)
        if (entityId != null) {
            withProjectModelId(entityId) {
                super.navigate(requestFocus)
            }
        } else {
            super.navigate(requestFocus)
        }
    }

    override fun canNavigateToSource(): Boolean {
        return false;
    }

    override fun expandOnDoubleClick(): Boolean {
        val entity = entity ?: return false
        return !entity.isProjectFile()
    }

    override fun showInplaceComments(): Boolean {
        return entity?.isProjectFile() == true
    }

    override fun getVirtualFile() = null

    override fun contains(file: VirtualFile): Boolean {
        val entity = entity ?: return false
        if (entity.isSolution()) {
            return true
        }

        // Check file system
        val settings = settings
        if (settings.isShowAllFiles()) {
            val currentFile = entity.getVirtualFileAsContentRoot() ?: return false
            return VfsUtil.isAncestor(currentFile, file, false)
        }

        return false
    }

    override fun getBackgroundColor() =
        virtualFile?.getScopeColor(project!!, isInProjectModel = true, highlightNonProjectItems = true)

    override fun getName(): String = entity?.name ?: "INVALID"
    override fun getLinkData(): ClickableNodeLink? {
        return null
    }

    override fun processLinkClick() {
    }

    override fun toString() = name
}