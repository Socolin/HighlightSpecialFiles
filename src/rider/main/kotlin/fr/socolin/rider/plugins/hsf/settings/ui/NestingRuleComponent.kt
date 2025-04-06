package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import icons.RiderIcons

class NestingRuleComponent(
    ruleConfiguration: HsfNestingRuleConfiguration,
    lifetime: Lifetime,
) : RuleComponentBase<HsfNestingRuleConfiguration>(ruleConfiguration) {
    private val panel: DialogPanel
    override val ruleModel = NestingRuleModel(ruleConfiguration)
    private lateinit var patternTextField: Cell<JBTextField>

    init {
        val deleteAction = object : DumbAwareAction("Delete Rule", "Delete this rule", AllIcons.General.Delete) {
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
                        checkBox("Disable")
                            .bindSelected(ruleModel::isDisabled)
                    }
                }
                group("Match") {
                    row("Pattern") {
                        @Suppress("UnstableApiUsage")
                        patternTextField = textField()
                            .columns(COLUMNS_LARGE)
                            .bindText(ruleModel::pattern)
                            .whenTextChangedFromUi { pattern ->
                                updateLabelWithPattern(patternTextField.component, pattern)
                            }
                            .comment("A regex used to configure the nesting rule, it need to have 2 capture: groupBy and childPart")
                    }
                    row("Example") {
                        panel {
                            row {
                                text("For example to nest the files App.config App.Release.config and App.Debug.config like " +
                                        "<br>App.config<br>├─ App.Debug.config<br>└─ App.Release.config")
                            }
                            row {
                                text("You can use the following regex <code>^(?<groupBy>[^.]+)(?<childPart>\\..+)?\\.cs\$</code>")
                                link("Use this regex") { _ ->
                                    patternTextField.component.text = "^(?<groupBy>[^.]+)(?<childPart>\\..+)?\\.cs\$"
                                }
                            }
                        }
                    }
                }
            }
        }
        // "^(?<groupBy>[^.]+)(?<childPart>\\..+)?\\.cs\$"

        add(panel)
    }

    override fun getRule(): HsfNestingRuleConfiguration {
        panel.apply()
        return HsfNestingRuleConfiguration(
            ruleId,
            ruleModel.pattern,
            ruleModel.order,
            ruleModel.isShared,
            ruleModel.isDisabled
        )
    }

    override fun setRule(updatedRule: HsfNestingRuleConfiguration) {
        super.setRule(updatedRule)
        panel.reset()
        updateLabelWithPattern(patternTextField.component, ruleModel.pattern)
    }
}

class NestingRuleModel(ruleConfiguration: HsfNestingRuleConfiguration) : RuleModelBase<HsfNestingRuleConfiguration> {
    lateinit var pattern: String
    var isShared: Boolean = false
    var isDisabled: Boolean = false
    override var order: Int = 0

    init {
        updateModel(ruleConfiguration)
    }

    override fun updateModel(ruleConfiguration: HsfNestingRuleConfiguration) {
        pattern = ruleConfiguration.pattern
        isShared = ruleConfiguration.isShared
        isDisabled = ruleConfiguration.isDisabled
        order = ruleConfiguration.order
    }
}