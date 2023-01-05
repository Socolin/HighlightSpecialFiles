package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.jetbrains.rd.util.reactive.Signal
import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.ui.renderers.ComboCellStyleRender
import fr.socolin.rider.plugins.hsf.settings.ui.renderers.ComboCellWithIconRender
import icons.CollaborationToolsIcons
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel

class RuleComponent(
    ruleConfiguration: HsfRuleConfiguration,
    hsfIconManager: HsfIconManager
) : JPanel(GridLayout()) {
    val onDelete = Signal<HsfRuleConfiguration>()
    private val panel: DialogPanel

    internal val ruleModel = RuleModel(ruleConfiguration)

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
                        textField()
                            .bindText(ruleModel::pattern)
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
            ruleModel.id,
            ruleModel.pattern,
            ruleModel.order,
            ruleModel.iconId,
            if (ruleModel.usePriority) ruleModel.priority else null,
            if (ruleModel.useAnnotation) ruleModel.annotationText else null,
            ruleModel.annotationStyle,
            if (ruleModel.useForegroundColor) ruleModel.foregroundColorHex else null,
            ruleModel.isShared
        )
    }

    companion object {
        private fun convertColorToHex(c: Color?): String? {
            if (c == null)
                return null
            return "#${c.red.toString(16)}${c.green.toString(16)}${c.blue.toString(16)}"
        }
    }
}

internal class RuleModel(ruleConfiguration: HsfRuleConfiguration) {
    var id = ruleConfiguration.id
    var pattern = ruleConfiguration.pattern
    var isShared = ruleConfiguration.isShared
    var order = ruleConfiguration.order
    var iconId = ruleConfiguration.iconId

    var usePriority: Boolean = ruleConfiguration.priority != null
    var priority = ruleConfiguration.priority ?: 0

    var useAnnotation: Boolean = ruleConfiguration.annotationText != null
    var annotationText = ruleConfiguration.annotationText ?: ""
    var annotationStyle: String? = ruleConfiguration.annotationStyle ?: HsfAnnotationTextStyles.defaultId

    var useForegroundColor = ruleConfiguration.foregroundColorHex != null
    var foregroundColorHex = ruleConfiguration.foregroundColorHex
}