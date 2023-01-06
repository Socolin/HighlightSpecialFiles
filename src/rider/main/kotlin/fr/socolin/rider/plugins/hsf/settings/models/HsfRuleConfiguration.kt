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
    val isShared: Boolean = false
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
    }
}