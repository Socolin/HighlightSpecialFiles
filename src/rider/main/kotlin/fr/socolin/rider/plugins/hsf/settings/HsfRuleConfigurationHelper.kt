package fr.socolin.rider.plugins.hsf.settings

import fr.socolin.rider.plugins.hsf.settings.models.IHsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.models.RulesDiffResult

class HsfRuleConfigurationHelper {
    companion object {
        fun <T: IHsfRuleConfiguration<T>> computeDiffBetweenRules(
            currentRules: Collection<T>,
            newRules: Collection<T>,
        ): RulesDiffResult<T> {
            val removedRules = ArrayList<T>()
            val addedRules = ArrayList<T>()
            val updatedRules = ArrayList<T>()

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