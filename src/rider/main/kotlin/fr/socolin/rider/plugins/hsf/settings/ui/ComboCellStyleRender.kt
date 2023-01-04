package fr.socolin.rider.plugins.hsf.settings.ui

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
        return this
    }
}