package fr.socolin.rider.plugins.hsf.models

import com.intellij.ui.SimpleTextAttributes
import java.awt.Color
import java.util.*
import java.util.regex.Pattern

class HsfHighlightingRule(
    val id: UUID,
    val pattern: Pattern,
    val order: Int,
    val icon: HsfIcon,
    val priority: Int?,
    val annotationText: String?,
    val annotationStyle: SimpleTextAttributes?,
    val foregroundColor: Color?,
    val groupInVirtualFolder: Boolean,
    val virtualFolderIcon: HsfIcon,
    val virtualFolderName: String?,
    val filesCountBeforeCreatingVirtualFolder: Int,
) {
}