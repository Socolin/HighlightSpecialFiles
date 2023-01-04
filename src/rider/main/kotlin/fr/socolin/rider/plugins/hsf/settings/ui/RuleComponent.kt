package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.IntegerField
import com.jetbrains.rd.util.reactive.Signal
import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRuleConfiguration
import fr.socolin.rider.plugins.hsf.models.HsfIcon
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import java.awt.*
import java.util.*
import javax.swing.*

class RuleComponent(
    private val rule: HsfHighlightingRuleConfiguration,
    hsfIconManager: HsfIconManager
) : JPanel(GridBagLayout()) {
    private val iconComboBox = ComboBox<HsfIcon>()
    private val priorityField = IntegerField()

    // FIXME: See IgnoredFilesAndFoldersPanel.PatternEditField to add some validation
    private val patternTextField = JBTextField()
    private val annotationTextField = JBTextField()
    private val annotationStyleComboBox = ComboBox<String>()
    private val useForegroundColor = JBCheckBox()
    private val foregroundColorPanel = ColorPanel()

    val onDelete = Signal<RuleComponent>()

    init {
        addTitle()

        val secondLineConstraint = GridBagConstraints()
        secondLineConstraint.gridx = 0
        secondLineConstraint.gridy = 1
        secondLineConstraint.fill = GridBagConstraints.HORIZONTAL
        secondLineConstraint.gridwidth = 1
        secondLineConstraint.gridheight = 1
        secondLineConstraint.anchor = GridBagConstraints.PAGE_START

        add(
            createLine(createIconSelector(hsfIconManager), createPriorityField(), createAnnotationTextField()),
            secondLineConstraint
        )

        val thirdLineConstraint = GridBagConstraints()
        thirdLineConstraint.gridx = 0
        thirdLineConstraint.gridy = 2
        thirdLineConstraint.fill = GridBagConstraints.HORIZONTAL
        thirdLineConstraint.gridwidth = 1
        thirdLineConstraint.gridheight = 1
        thirdLineConstraint.anchor = GridBagConstraints.PAGE_START

        add(
            createLine(createForegroundField()),
            thirdLineConstraint
        )

        val deleteButtonConstraint = GridBagConstraints()
        deleteButtonConstraint.gridx = 1

        val deleteButton = JButton(AllIcons.General.Remove)
        deleteButton.preferredSize = Dimension(30, 30)
        deleteButton.addActionListener { _ -> onDelete.fire(this) }

        add(deleteButton, deleteButtonConstraint)
    }

    private fun addTitle() {
        val patternConstraint = GridBagConstraints()
        patternConstraint.gridx = 0
        patternConstraint.gridwidth = 1
        patternConstraint.gridheight = 1
        patternConstraint.anchor = GridBagConstraints.FIRST_LINE_START
        patternConstraint.weightx = 1.0
        patternConstraint.fill = GridBagConstraints.HORIZONTAL

        patternTextField.preferredSize = Dimension(300, 30)
        patternTextField.text = rule.pattern
        val patternPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        patternPanel.add(JLabel("Pattern: "))
        patternPanel.add(patternTextField)

        add(patternPanel, patternConstraint)
    }

    private fun createIconSelector(hsfIconManager: HsfIconManager): JComponent {
        val icons = hsfIconManager.getIcons()
        val iconsArray = Array(icons.size) { i -> icons[i] }
        iconComboBox.model = DefaultComboBoxModel(iconsArray)
        iconComboBox.renderer = ComboCellWithIconRender()
        iconComboBox.selectedItem = hsfIconManager.getIcon(rule.iconId)

        val iconPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        iconPanel.add(JLabel("Icon: "))
        iconPanel.add(iconComboBox)

        return iconPanel
    }

    private fun createPriorityField(): JComponent {
        priorityField.preferredSize = Dimension(50, priorityField.preferredSize.height)
        if (rule.priority != null)
            priorityField.text = rule.priority.toString()

        val priorityIcon = JPanel(FlowLayout(FlowLayout.LEFT))
        priorityIcon.add(JLabel("Priority:"))
        priorityIcon.add(priorityField)

        return priorityIcon
    }
    private fun createForegroundField(): JComponent {

        if (rule.foregroundColorHex != null) {
            useForegroundColor.isSelected = true
            foregroundColorPanel.selectedColor = Color.decode(rule.foregroundColorHex)
        }

        val priorityIcon = JPanel(FlowLayout(FlowLayout.LEFT))
        priorityIcon.add(JLabel("Foreground:"))
        priorityIcon.add(useForegroundColor)
        priorityIcon.add(foregroundColorPanel)

        return priorityIcon
    }

    private fun createAnnotationTextField(): JComponent {
        annotationTextField.text = rule.annotationText
        annotationTextField.preferredSize = Dimension(150, priorityField.preferredSize.height)

        val v = Vector(HsfAnnotationTextStyles.annotationsStyles.keys)
        v.sort()
        annotationStyleComboBox.model = DefaultComboBoxModel(v)
        annotationStyleComboBox.renderer = ComboCellStyleRender()
        annotationStyleComboBox.selectedItem = rule.annotationStyle

        val annotationPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        annotationPanel.add(JLabel("Annotation:"))
        annotationPanel.add(annotationTextField)
        annotationPanel.add(annotationStyleComboBox)

        return annotationPanel
    }

    private fun createLine(vararg components: JComponent): JPanel {
        val linePanel = JPanel(FlowLayout(FlowLayout.LEFT))
        for (component in components) {
            linePanel.add(component)
        }
        return linePanel
    }

    fun getRule(): HsfHighlightingRuleConfiguration {
        var priority: Int? = null
        if (priorityField.text != null && priorityField.text != "")
            priority = priorityField.value

        var annotationText = annotationTextField.text
        if (annotationText == "") {
            annotationText = null
        }

        return HsfHighlightingRuleConfiguration(
            rule.id,
            patternTextField.text,
            (iconComboBox.selectedItem as HsfIcon).id,
            priority,
            annotationText,
            annotationStyleComboBox.selectedItem as String,
            getForegroundColor()
        )
    }

    private fun getForegroundColor(): String? {
        if (!useForegroundColor.isSelected)
            return null
        val color = foregroundColorPanel.selectedColor ?: return null
        return "#${color.red.toString(16)}${color.green.toString(16)}${color.blue.toString(16)}"
    }

}
