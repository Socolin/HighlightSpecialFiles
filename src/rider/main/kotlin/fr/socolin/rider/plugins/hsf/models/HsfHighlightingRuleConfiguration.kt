package fr.socolin.rider.plugins.hsf.models

import java.util.UUID

class HsfHighlightingRuleConfiguration(
    val id: UUID,
    val pattern: String,
    val iconId: String = HsfIconManager.None.id,
    val priority: Int? = null,
    val annotationText: String? = null,
    val annotationStyle: String = HsfAnnotationTextStyles.defaultId,
    val foregroundColorHex: String? = null,
) {
    fun isDifferentFrom(other: HsfHighlightingRuleConfiguration): Boolean {
        return pattern != other.pattern
                || iconId != other.iconId
                || priority != other.priority
                || annotationText != other.annotationText
                || annotationStyle != other.annotationStyle
                || foregroundColorHex != other.foregroundColorHex
    }
}