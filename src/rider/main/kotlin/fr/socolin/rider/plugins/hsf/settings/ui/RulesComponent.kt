package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.containers.SortedList
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.HsfRuleConfigurationHelper
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.models.IHsfRuleConfiguration
import java.util.*
import javax.swing.JPanel
import kotlin.collections.HashSet

class RulesComponent(
    project: Project,
    lifetime: Lifetime,
) : RulesComponentBase<HsfRuleConfiguration>(project, lifetime) {
    override val title: String
        get() = "Rules"

    override fun createNewRuleConfiguration(): HsfRuleConfiguration {
        return HsfRuleConfiguration(UUID.randomUUID(), "^$", 0)
    }

    override fun createRuleComponent(rule: HsfRuleConfiguration): RuleComponentBase<HsfRuleConfiguration> {
        return RuleComponent(rule, HsfIconManager.getInstance(project), lifetime)
    }

    override fun createRuleConfigurationFrom(duplicatedRule: HsfRuleConfiguration): HsfRuleConfiguration {
        return HsfRuleConfiguration.createFrom(duplicatedRule)
    }
}