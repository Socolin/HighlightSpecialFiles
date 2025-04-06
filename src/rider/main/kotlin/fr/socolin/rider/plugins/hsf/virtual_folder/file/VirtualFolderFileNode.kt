package fr.socolin.rider.plugins.hsf.virtual_folder.file

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import com.jetbrains.rider.projectView.views.SolutionViewCustomEntityContainer
import com.jetbrains.rider.projectView.views.SolutionViewNode
import com.jetbrains.rider.projectView.views.getVirtualFile
import com.jetbrains.rider.projectView.workspace.ProjectModelEntityReference
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule

class VirtualFolderFileNode(
    project: Project,
    private val value: String,
    private val rule: HsfHighlightingRule,
    private val filesToGroup: List<AbstractTreeNode<*>>,
    private val virtualFile: VirtualFile,
) : SolutionViewNode<String>(project, value), SolutionViewCustomEntityContainer {
    override fun update(presentation: PresentationData) {
        presentation.setIcon(rule.virtualFolderIcon.icon ?: AllIcons.Nodes.Folder)
        presentation.addText(rule.virtualFolderName ?: "<No title>", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    override fun calculateChildren(): MutableList<AbstractTreeNode<*>> {
        return ArrayList(filesToGroup)
    }

    override fun contains(file: VirtualFile): Boolean {
        return filesToGroup.any { f -> f.getVirtualFile() == file };
    }

    override fun contains(entity: ProjectModelEntityReference): Boolean {
        val entityVirtualUrl = entity.getEntity(project)?.url ?: return false;
        return filesToGroup.any { f -> f.getVirtualFile() == entityVirtualUrl };
    }

    override fun getVirtualFile(): VirtualFile {
        return this.virtualFile;
    }

    override fun getName(): String {
        return rule.virtualFolderName ?: "<No Virtual Folder Name>";
    }

    override fun toString(): String {
        return rule.virtualFolderName ?: "<No Virtual Folder Name>";
    }
}
