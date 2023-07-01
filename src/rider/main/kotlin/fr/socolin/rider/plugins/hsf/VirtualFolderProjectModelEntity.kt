package fr.socolin.rider.plugins.hsf

import com.intellij.platform.workspace.storage.EntityReference
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.jetbrains.rider.model.RdProjectModelItemDescriptor
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule

class VirtualFolderProjectModelEntity(
    val sourceModelEntity: ProjectModelEntity,
    val rule: HsfHighlightingRule
) : ProjectModelEntity {
    override val alternativeUrls: List<VirtualFileUrl>
        get() = sourceModelEntity.alternativeUrls
    override val childrenEntities: List<ProjectModelEntity>
        get() = sourceModelEntity.childrenEntities
    override val descriptor: RdProjectModelItemDescriptor
        get() = VirtualFolderItemDescriptor(rule.virtualFolderName ?: "", sourceModelEntity.descriptor.location)
    override val entitySource: EntitySource
        get() = sourceModelEntity.entitySource
    override val parentEntity: ProjectModelEntity?
        get() = sourceModelEntity.parentEntity
    override val url: VirtualFileUrl?
        get() = sourceModelEntity.url

    override fun <E : WorkspaceEntity> createReference(): EntityReference<E> {
        return sourceModelEntity.createReference()
    }

    override fun getEntityInterface(): Class<out WorkspaceEntity> {
        return sourceModelEntity.getEntityInterface()
    }

}