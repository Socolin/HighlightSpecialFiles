package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.lifetime.Lifetime
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import java.util.*

class NestingRulesComponent(
    project: Project,
    lifetime: Lifetime,
) : RulesComponentBase<HsfNestingRuleConfiguration>(project, lifetime) {
    override val title: String
        get() = "Nesting Rules"

    override fun createNewRuleConfiguration(): HsfNestingRuleConfiguration {
        return HsfNestingRuleConfiguration(UUID.randomUUID(), "^$", 0)
    }

    override fun createRuleComponent(rule: HsfNestingRuleConfiguration): RuleComponentBase<HsfNestingRuleConfiguration> {
        return NestingRuleComponent(rule, lifetime)
    }

    override fun createRuleConfigurationFrom(duplicatedRule: HsfNestingRuleConfiguration): HsfNestingRuleConfiguration {
        return HsfNestingRuleConfiguration.createFrom(duplicatedRule)
    }
}