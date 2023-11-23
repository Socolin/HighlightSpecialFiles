package fr.socolin.rider.plugins.hsf.settings.ui

import fr.socolin.rider.plugins.hsf.settings.models.IHsfRuleConfiguration

interface RuleModelBase<TRuleConfiguration : IHsfRuleConfiguration<TRuleConfiguration>> {
    val order: Int

    fun updateModel(ruleConfiguration: TRuleConfiguration)

}