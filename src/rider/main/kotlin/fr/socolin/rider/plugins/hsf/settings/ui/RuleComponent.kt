package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import fr.socolin.rider.plugins.hsf.models.HsfIcon
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.ui.renderers.ComboCellStyleRender
import fr.socolin.rider.plugins.hsf.settings.ui.renderers.ComboCellWithIconRender
import icons.CollaborationToolsIcons
import icons.RiderIcons
import java.awt.Color
import java.util.*
import javax.swing.DefaultComboBoxModel

class RuleComponent(
    ruleConfiguration: HsfRuleConfiguration,
    hsfIconManager: HsfIconManager,
    lifetime: Lifetime,
) : RuleComponentBase<HsfRuleConfiguration>(ruleConfiguration) {
    private val panel: DialogPanel
    override val ruleModel = RuleModel(ruleConfiguration)
    private lateinit var patternTextField: Cell<JBTextField>

    init {
        val deleteAction = object : DumbAwareAction("Delete Rule", "Delete this rule", CollaborationToolsIcons.Delete) {
            override fun actionPerformed(e: AnActionEvent) {
                onDelete.fire(ruleConfiguration)
            }
        }
        val duplicateAction =
            object : DumbAwareAction("Duplicate Rule", "Duplicate this rule", RiderIcons.Toolbar.Duplicate) {
                override fun actionPerformed(e: AnActionEvent) {
                    onDuplicate.fire(ruleConfiguration)
                }
            }
        panel = panel {
            collapsibleGroup("Rule: " + ruleModel.pattern) {
                group("Metadata") {
                    row {
                        label("")
                            .resizableColumn()
                        actionButton(duplicateAction)
                        actionButton(deleteAction)
                    }
                    row {
                        intTextField()
                            .label("Order")
                            .bindIntText(ruleModel::order);
                        icon(AllIcons.General.ShowInfos).applyToComponent {
                            toolTipText = "When multiple rules match a same file, the one with the larger value is applied the latest"
                        }
                        checkBox("Shared")
                            .bindSelected(ruleModel::isShared)
                        icon(AllIcons.General.ShowInfos).applyToComponent {
                            toolTipText = "Define where this rule is saved. When shared a rule is saved in .project.xml otherwise it's in .user.xml"
                        }
                        label("")
                            .resizableColumn()
                        checkBox("Disable")
                            .bindSelected(ruleModel::isDisabled)
                    }
                }
                group("Match") {
                    row("Pattern") {
                        @Suppress("UnstableApiUsage")
                        patternTextField = textField()
                            .bindText(ruleModel::pattern)
                            .whenTextChangedFromUi { pattern ->
                                updateLabelWithPattern(patternTextField.component, pattern)
                            }
                        icon(AllIcons.General.ShowInfos).applyToComponent {
                            toolTipText = "A regex used to match on the filename (does not include the path)"
                        }
                    }
                }
                group("Effects") {
                    row("Icon") {
                        val iconComboBox: Cell<ComboBox<HsfIcon>> =
                            comboBox(hsfIconManager.icons, ComboCellWithIconRender())
                                .bindItem(
                                    { hsfIconManager.getIcon(ruleModel.iconId) },
                                    { v -> ruleModel.iconId = v?.id ?: HsfIconManager.None.id })
                        hsfIconManager.onReload.advise(lifetime) { icons ->
                            val selectedIcon = hsfIconManager.getIcon(ruleModel.iconId);
                            iconComboBox.component.model = DefaultComboBoxModel(Vector(icons))
                            iconComboBox.component.selectedItem = selectedIcon;
                        }
                    }
                    lateinit var usePriorityCheckbox: Cell<JBCheckBox>
                    row("Priority") {
                        usePriorityCheckbox = checkBox("Change order")
                            .bindSelected(ruleModel::usePriority)
                        intTextField()
                            .bindIntText(ruleModel::priority)
                            .visibleIf(usePriorityCheckbox.selected)
                        icon(AllIcons.General.ShowInfos).applyToComponent {
                            toolTipText = "A file with highest value will be sorted first.<br>File default: 0, Folder default: 1000"
                        }.visibleIf(usePriorityCheckbox.selected)
                    }
                    lateinit var useAnnotationCheckbox: Cell<JBCheckBox>
                    row("Annotation") {
                        useAnnotationCheckbox = checkBox("Add annotation")
                            .bindSelected(ruleModel::useAnnotation)
                        textField()
                            .bindText(ruleModel::annotationText)
                            .visibleIf(useAnnotationCheckbox.selected)
                        comboBox(HsfAnnotationTextStyles.sortedStyles, ComboCellStyleRender())
                            .bindItem(ruleModel::annotationStyle)
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
                    lateinit var groupInVirtualFolderCheckbox: Cell<JBCheckBox>
                    row("Virtual Folder") {
                        groupInVirtualFolderCheckbox = checkBox("Group in virtual folder")
                            .bindSelected(ruleModel::groupInVirtualFolder)
                    }
                    indent {
                        row {
                            label("Folder icon")
                            val iconComboBox: Cell<ComboBox<HsfIcon>> =
                                comboBox(hsfIconManager.icons, ComboCellWithIconRender())
                                    .bindItem(
                                        { hsfIconManager.getIcon(ruleModel.folderIconId) },
                                        { v -> ruleModel.folderIconId = v?.id ?: HsfIconManager.None.id })

                            hsfIconManager.onReload.advise(lifetime) { icons ->
                                val selectedIcon = hsfIconManager.getIcon(ruleModel.iconId);
                                iconComboBox.component.model = DefaultComboBoxModel(Vector(icons))
                                iconComboBox.component.selectedItem = selectedIcon;
                            }
                        }

                        row {
                            label("Folder name")
                            textField()
                                .bindText(ruleModel::folderName)
                        }

                        row {
                            label("Number of files before creating folder")
                            textField()
                                .bindIntText(ruleModel::filesCountBeforeCreatingVirtualFolder)
                        }
                    }.visibleIf(groupInVirtualFolderCheckbox.selected)
                }
            }
        }

        add(panel)
    }

    override fun getRule(): HsfRuleConfiguration {
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
            ruleModel.groupInVirtualFolder,
            ruleModel.folderIconId,
            if (ruleModel.groupInVirtualFolder) ruleModel.folderName else null,
            ruleModel.filesCountBeforeCreatingVirtualFolder,
            ruleModel.isShared,
            ruleModel.isDisabled
        )
    }

    override fun setRule(updatedRule: HsfRuleConfiguration) {
        super.setRule(updatedRule)
        panel.reset()
        updateLabelWithPattern(patternTextField.component, ruleModel.pattern)
    }
}

class RuleModel(ruleConfiguration: HsfRuleConfiguration) : RuleModelBase<HsfRuleConfiguration> {
    lateinit var pattern: String
    var isShared: Boolean = false
    var isDisabled: Boolean = false
    lateinit var iconId: String
    override var order: Int = 0

    var usePriority: Boolean = false
    var priority: Int = 0

    var useAnnotation: Boolean = false
    lateinit var annotationText: String
    var annotationStyle: String? = null

    var useForegroundColor: Boolean = false
    var foregroundColorHex: String? = null

    var groupInVirtualFolder: Boolean = false
    lateinit var folderIconId: String
    lateinit var folderName: String
    var filesCountBeforeCreatingVirtualFolder: Int = 0

    init {
        updateModel(ruleConfiguration)
    }

    override fun updateModel(ruleConfiguration: HsfRuleConfiguration) {
        pattern = ruleConfiguration.pattern
        isShared = ruleConfiguration.isShared
        isDisabled = ruleConfiguration.isDisabled
        order = ruleConfiguration.order
        iconId = ruleConfiguration.iconId

        usePriority = ruleConfiguration.priority != null
        priority = ruleConfiguration.priority ?: 0

        useAnnotation = ruleConfiguration.annotationText != null
        annotationText = ruleConfiguration.annotationText ?: ""
        annotationStyle = ruleConfiguration.annotationStyle ?: HsfAnnotationTextStyles.defaultId

        useForegroundColor = ruleConfiguration.foregroundColorHex != null
        foregroundColorHex = ruleConfiguration.foregroundColorHex

        groupInVirtualFolder = ruleConfiguration.groupInVirtualFolder
        folderIconId = if (ruleConfiguration.groupInVirtualFolder) ruleConfiguration.folderIconId else HsfIconManager.None.id
        folderName = if (ruleConfiguration.groupInVirtualFolder) ruleConfiguration.folderName ?: "" else ""
        filesCountBeforeCreatingVirtualFolder = ruleConfiguration.filesCountBeforeCreatingVirtualFolder ?: 1
    }

}