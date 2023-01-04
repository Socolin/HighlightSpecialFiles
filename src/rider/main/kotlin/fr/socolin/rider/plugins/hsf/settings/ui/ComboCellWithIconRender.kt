package fr.socolin.rider.plugins.hsf.settings.ui

import fr.socolin.rider.plugins.hsf.models.HsfIcon
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class ComboCellWithIconRender : DefaultListCellRenderer() {

    override fun getListCellRendererComponent(
        list: JList<*>?, value: Any, index: Int,
        isSelected: Boolean, cellHasFocus: Boolean
    ): Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        val hsfIcon: HsfIcon = value as HsfIcon
        text = hsfIcon.name
        icon = hsfIcon.icon
        return this
    }
}