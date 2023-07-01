package fr.socolin.rider.plugins.hsf.settings.models

import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import java.util.UUID

class HsfRuleConfiguration(
    val id: UUID,
    val pattern: String,
    val order: Int,
    val iconId: String = HsfIconManager.None.id,
    val priority: Int? = null,
    val annotationText: String? = null,
    val annotationStyle: String? = null,
    val foregroundColorHex: String? = null,
    val groupInVirtualFolder: Boolean = false,
    val folderIconId: String = HsfIconManager.None.id,
    val folderName: String? = null,
    val isShared: Boolean = false,
) {
    fun isDifferentFrom(other: HsfRuleConfiguration): Boolean {
        return pattern != other.pattern
                || iconId != other.iconId
                || order != other.order
                || isShared != other.isShared
                || priority != other.priority
                || annotationText != other.annotationText
                || annotationStyle != other.annotationStyle
                || foregroundColorHex != other.foregroundColorHex
                || groupInVirtualFolder != other.groupInVirtualFolder
                || folderIconId != other.folderIconId
                || folderName != other.folderName
    }

    companion object {
        fun createFrom(source: HsfRuleConfiguration): HsfRuleConfiguration {
            return HsfRuleConfiguration(UUID.randomUUID()
                , source.pattern
                , source.order
                , source.iconId
                , source.priority
                , source.annotationText
                , source.annotationStyle
                , source.foregroundColorHex
                , source.groupInVirtualFolder
                , source.folderIconId
                , source.folderName
                , source.isShared
            )
        }
    }
}