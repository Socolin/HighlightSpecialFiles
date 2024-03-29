package fr.socolin.rider.plugins.hsf.virtual_folder.node

import com.intellij.platform.workspace.storage.EntityPointer
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.jetbrains.rider.model.RdProjectModelItemDescriptor
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule

class VirtualFolderProjectModelEntity(
    private val parentModelEntity: ProjectModelEntity,
    val rule: HsfHighlightingRule,
) : ProjectModelEntity {
    override val alternativeUrls: List<VirtualFileUrl>
        get() = parentModelEntity.alternativeUrls
    override val childrenEntities: List<ProjectModelEntity>
        get() = parentModelEntity.childrenEntities
    override val descriptor: RdProjectModelItemDescriptor
        get() = VirtualFolderItemDescriptor(rule.virtualFolderName ?: "", parentModelEntity.descriptor.location)
    override val entitySource: EntitySource
        get() = VirtualEntitySource()
    override val parentEntity: ProjectModelEntity?
        get() = parentModelEntity
    override val url: VirtualFileUrl?
        get() = null

    override fun <E : WorkspaceEntity> createPointer(): EntityPointer<E> {
        return parentModelEntity.createPointer();
    }

    override fun getEntityInterface(): Class<out WorkspaceEntity> {
        return parentModelEntity.getEntityInterface()
    }

}

class VirtualEntitySource : EntitySource {

}