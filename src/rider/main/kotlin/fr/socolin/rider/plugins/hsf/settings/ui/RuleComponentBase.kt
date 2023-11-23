package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.jetbrains.rd.util.reactive.Signal
import fr.socolin.rider.plugins.hsf.settings.models.IHsfRuleConfiguration
import java.awt.Color
import java.awt.Container
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel


abstract class RuleComponentBase<TRuleConfiguration : IHsfRuleConfiguration<TRuleConfiguration>>(
    ruleConfiguration: TRuleConfiguration,
) : JPanel(GridLayout()) {
    val onDelete = Signal<TRuleConfiguration>()
    val onDuplicate = Signal<TRuleConfiguration>()
    val ruleId = ruleConfiguration.id
    protected abstract val ruleModel: RuleModelBase<TRuleConfiguration>

    val order: Int
        get() = ruleModel.order

    abstract fun getRule(): TRuleConfiguration;

    open fun setRule(updatedRule: TRuleConfiguration) {
        ruleModel.updateModel(updatedRule)
    }

    companion object {
        internal fun convertColorToHex(c: Color?): String? {
            if (c == null)
                return null
            return "#${c.red.toString(16)}${c.green.toString(16)}${c.blue.toString(16)}"
        }

        internal fun updateLabelWithPattern(component: JComponent, pattern: String) {
            val label = findRuleLabelInParentOf(component)
            if (label != null)
                label.text = "Rule: $pattern"
        }

        internal fun findRuleLabelInParentOf(container: Container?): JBLabel? {
            if (container == null)
                return null

            if (container is DialogPanel) {
                for (component in container.components) {
                    if (component is TitledSeparator) {
                        for (child in component.components) {
                            if (child is JBLabel && child.text.startsWith("Rule: "))
                                return child
                        }
                    }
                }

            }

            return findRuleLabelInParentOf(container.parent)
        }
    }
}