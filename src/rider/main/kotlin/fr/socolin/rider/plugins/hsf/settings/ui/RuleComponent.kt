package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColorPanel
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.impl.CollapsibleTitledSeparatorImpl
import com.jetbrains.rd.util.reactive.Signal
import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.ui.renderers.ComboCellStyleRender
import fr.socolin.rider.plugins.hsf.settings.ui.renderers.ComboCellWithIconRender
import icons.CollaborationToolsIcons
import java.awt.Color
import java.awt.Container
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel

class RuleComponent(
    ruleConfiguration: HsfRuleConfiguration,
    hsfIconManager: HsfIconManager
) : JPanel(GridLayout()) {
    val onDelete = Signal<HsfRuleConfiguration>()
    val ruleId = ruleConfiguration.id
    private val panel: DialogPanel
    private val ruleModel = RuleModel(ruleConfiguration)
    private lateinit var patternTextField: Cell<JBTextField>

    val order: Int
        get() = ruleModel.order

    init {
        val action = object : DumbAwareAction("Delete Rule", "Delete this rule", CollaborationToolsIcons.Delete) {
            override fun actionPerformed(e: AnActionEvent) {
                onDelete.fire(ruleConfiguration)
            }
        }
        panel = panel {
            collapsibleGroup("Rule: " + ruleModel.pattern) {
                group("Metadata") {
                    row {
                        intTextField()
                            .label("Order")
                            .bindIntText(ruleModel::order)
                            .resizableColumn()
                            .comment(
                                "When multiple rules match a same file, the one with the larger value is applied the latest",
                                50
                            )
                        label("")
                            .resizableColumn()
                        checkBox("Shared")
                            .bindSelected(ruleModel::isShared)
                            .comment(
                                "Define where this rule is saved. When shared a rule is saved in .project.xml otherwise it's in .user.xml",
                                60
                            )
                            .resizableColumn()
                        actionButton(action)
                    }
                }
                group("Match") {
                    row("Pattern") {
                        patternTextField = textField()
                            .bindText(ruleModel::pattern)
                            .whenTextChangedFromUi { pattern ->
                                updateLabelWithPattern(patternTextField.component, pattern)
                            }
                            .comment("A regex used to match on the filename (does not include the path)")
                    }
                }
                group("Effects") {
                    row("Icon") {
                        comboBox(hsfIconManager.getVectorIcons(), ComboCellWithIconRender())
                            .bindItem(
                                { hsfIconManager.getIcon(ruleModel.iconId) },
                                { v -> ruleModel.iconId = v?.id ?: HsfIconManager.None.id })
                    }
                    lateinit var usePriorityCheckbox: Cell<JBCheckBox>
                    row("Priority") {
                        usePriorityCheckbox = checkBox("Change order")
                            .bindSelected(ruleModel::usePriority)
                        intTextField()
                            .bindIntText(ruleModel::priority)
                            .comment(
                                "A file with highest value will be sorted first.<br>File default: 0, Folder default: 1000",
                                300
                            )
                            .visibleIf(usePriorityCheckbox.selected)
                    }
                    lateinit var useAnnotationCheckbox: Cell<JBCheckBox>
                    row("Annotation") {
                        useAnnotationCheckbox = checkBox("Add annotation")
                            .bindSelected(ruleModel::useAnnotation)
                        textField()
                            .bindText(ruleModel::annotationText)
                            .visibleIf(useAnnotationCheckbox.selected)
                        comboBox(HsfAnnotationTextStyles.sortedStyles, ComboCellStyleRender())
                            .bindItemNullable(ruleModel::annotationStyle)
                            .visibleIf(useAnnotationCheckbox.selected)
                    }
                    lateinit var useForegroundCheckbox: Cell<JBCheckBox>
                    row("Foreground") {
                        useForegroundCheckbox = checkBox("Change text color")
                            .bindSelected(ruleModel::useForegroundColor)

                        cell(ColorPanel())
                            .bind(
                                { comp -> convertColorToHex(comp.selectedColor) },
                                { comp, color ->
                                    comp.selectedColor = if (color == null) null else Color.decode(color)
                                },
                                MutableProperty(
                                    { ruleModel.foregroundColorHex },
                                    { ruleModel.foregroundColorHex = it }
                                )
                            )
                            .visibleIf(useForegroundCheckbox.selected)
                    }
                }
            }
        }

        add(panel)
    }

    fun getRule(): HsfRuleConfiguration {
        panel.apply()
        return HsfRuleConfiguration(
            ruleId,
            ruleModel.pattern,
            ruleModel.order,
            ruleModel.iconId,
            if (ruleModel.usePriority) ruleModel.priority else null,
            if (ruleModel.useAnnotation) ruleModel.annotationText else null,
            if (ruleModel.useAnnotation) ruleModel.annotationStyle else null,
            if (ruleModel.useForegroundColor) ruleModel.foregroundColorHex else null,
            ruleModel.isShared
        )
    }

    fun setRule(updatedRule: HsfRuleConfiguration) {
        ruleModel.updateModel(updatedRule)
        panel.reset()
        updateLabelWithPattern(patternTextField.component, ruleModel.pattern)
    }

    companion object {
        private fun convertColorToHex(c: Color?): String? {
            if (c == null)
                return null
            return "#${c.red.toString(16)}${c.green.toString(16)}${c.blue.toString(16)}"
        }

        private fun updateLabelWithPattern(component: JComponent, pattern: String) {
            val label = findRuleLabelInParentOf(component);
            if (label != null)
                label.text = "Rule: $pattern"
        }

        private fun findRuleLabelInParentOf(container: Container?): JBLabel? {
            if (container == null)
                return null;

            if (container is DialogPanel) {
                for (component in container.components) {
                    if (component is TitledSeparator) {
                        for (child in component.components) {
                            if (child is JBLabel && child.text.startsWith("Rule: "))
                                return child;
                        }
                    }
                }

            }

            return findRuleLabelInParentOf(container.parent);
        }
    }
}

internal class RuleModel(ruleConfiguration: HsfRuleConfiguration) {
    lateinit var pattern: String
    var isShared: Boolean = false
    var order: Int = 0
    lateinit var iconId: String

    var usePriority: Boolean = false
    var priority: Int = 0

    var useAnnotation: Boolean = false
    lateinit var annotationText: String
    var annotationStyle: String? = null

    var useForegroundColor: Boolean = false
    var foregroundColorHex: String? = null

    init {
        updateModel(ruleConfiguration)
    }

    fun updateModel(ruleConfiguration: HsfRuleConfiguration) {
        pattern = ruleConfiguration.pattern
        isShared = ruleConfiguration.isShared
        order = ruleConfiguration.order
        iconId = ruleConfiguration.iconId

        usePriority = ruleConfiguration.priority != null
        priority = ruleConfiguration.priority ?: 0

        useAnnotation = ruleConfiguration.annotationText != null
        annotationText = ruleConfiguration.annotationText ?: ""
        annotationStyle = ruleConfiguration.annotationStyle ?: HsfAnnotationTextStyles.defaultId

        useForegroundColor = ruleConfiguration.foregroundColorHex != null
        foregroundColorHex = ruleConfiguration.foregroundColorHex
    }

}