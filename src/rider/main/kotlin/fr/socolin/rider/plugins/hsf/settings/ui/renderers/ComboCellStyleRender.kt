package fr.socolin.rider.plugins.hsf.settings.ui.renderers

import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class ComboCellStyleRender : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?, value: Any, index: Int,
        isSelected: Boolean, cellHasFocus: Boolean
    ): Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        val name: String = value as String
        this.text = name
        val style = HsfAnnotationTextStyles.getStyle(name)
        foreground = style.fgColor
        background = style.bgColor
        font = font.deriveFont(style.style)
        return this
    }
}