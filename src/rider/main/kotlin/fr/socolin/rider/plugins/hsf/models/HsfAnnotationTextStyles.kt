package fr.socolin.rider.plugins.hsf.models

import com.intellij.ui.SimpleTextAttributes

class HsfAnnotationTextStyles {
    companion object {
        const val defaultId: String = "gray"
        val annotationsStyles: HashMap<String, SimpleTextAttributes> = hashMapOf(
            ("gray" to SimpleTextAttributes.GRAYED_ATTRIBUTES),
            ("gray_bold" to SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES),
            ("gray_italic" to SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES),
            ("error" to SimpleTextAttributes.ERROR_ATTRIBUTES),
            ("regular" to SimpleTextAttributes.REGULAR_ATTRIBUTES),
            ("regular_bold" to SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES),
            ("regular_italic" to SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES),
            ("dark" to SimpleTextAttributes.DARK_TEXT),
            ("shortcut" to SimpleTextAttributes.SHORTCUT_ATTRIBUTES),
            ("synthetic" to SimpleTextAttributes.SYNTHETIC_ATTRIBUTES),
        )
    }
}