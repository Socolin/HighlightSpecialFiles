package fr.socolin.rider.plugins.hsf.settings

import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.models.RulesDiffResult

class HsfRuleConfigurationHelper {
    companion object {
        fun computeDiffBetweenRules(
            currentRules: Collection<HsfRuleConfiguration>,
            newRules: Collection<HsfRuleConfiguration>,
        ): RulesDiffResult {
            val removedRules = ArrayList<HsfRuleConfiguration>()
            val addedRules = ArrayList<HsfRuleConfiguration>()
            val updatedRules = ArrayList<HsfRuleConfiguration>()

            for (rule in currentRules) {
                if (newRules.find { r -> r.id == rule.id } == null)
                    removedRules.add(rule)
            }

            for (rule in newRules) {
                val currentRule = currentRules.find { r -> r.id == rule.id }
                if (currentRule != null) {
                    if (currentRule.isDifferentFrom(rule))
                        updatedRules.add(rule)
                } else
                    addedRules.add(rule)
            }

            return RulesDiffResult(addedRules, updatedRules, removedRules)
        }
    }
}