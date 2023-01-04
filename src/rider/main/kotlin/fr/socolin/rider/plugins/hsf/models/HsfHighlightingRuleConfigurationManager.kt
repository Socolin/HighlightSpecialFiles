package fr.socolin.rider.plugins.hsf.models

import com.jetbrains.rd.util.reactive.Signal
import java.util.*

class HsfHighlightingRuleConfigurationManager {
    val rules: ArrayList<HsfHighlightingRuleConfiguration> = ArrayList()
    val ruleChanged: Signal<Pair<HsfHighlightingRuleConfiguration, HsfHighlightingRuleConfiguration>> = Signal()
    val ruleDeleted: Signal<HsfHighlightingRuleConfiguration> = Signal()
    val ruleAdded: Signal<HsfHighlightingRuleConfiguration> = Signal()

    private val rulesByIds: HashMap<UUID, HsfHighlightingRuleConfiguration> = HashMap()

    fun addRule(
        ruleId: UUID,
        scopeId: String,
        icon: String,
        priority: Int?,
        annotationText: String?,
        annotationStyle: String,
        foregroundColorHex: String?
    ) {
        addRule(
            HsfHighlightingRuleConfiguration(
                ruleId,
                scopeId,
                icon,
                priority,
                annotationText,
                annotationStyle,
                foregroundColorHex
            )
        )
    }

    fun updateRules(updatedRules: List<HsfHighlightingRuleConfiguration>) {
        val ruleToRemove = ArrayList<HsfHighlightingRuleConfiguration>()
        for (rule in rules) {
            if (updatedRules.find { r -> r.id == rule.id } == null)
                ruleToRemove.add(rule)
        }
        for (rule in ruleToRemove) {
            removeRule(rule)
        }

        for (rule in updatedRules) {
            if (rulesByIds.containsKey(rule.id))
                updateRule(rule)
            else
                addRule(rule)
        }
    }

    fun isDifferentFrom(rules: List<HsfHighlightingRuleConfiguration>): Boolean {
        if (rules.count() != this.rules.size)
            return true
        for (rule in rules) {
            val existingRule = rulesByIds.getOrDefault(rule.id, null) ?: return true
            if (existingRule.isDifferentFrom(rule))
                return true
        }

        return false
    }

    private fun updateRule(rule: HsfHighlightingRuleConfiguration) {
        val currenRule = rulesByIds.getOrDefault(rule.id, null) ?: return
        if (currenRule.isDifferentFrom(rule)) {
            val currentRuleIndex = rules.indexOf(currenRule)
            if (currentRuleIndex == -1)
                return
            rules[currentRuleIndex] = rule
            rulesByIds[rule.id] = rule
            ruleChanged.fire(Pair(currenRule, rule))
        }
    }

    private fun addRule(rule: HsfHighlightingRuleConfiguration) {
        rules.add(rule)
        rulesByIds[rule.id] = rule
        ruleAdded.fire(rule)
    }

    private fun removeRule(rule: HsfHighlightingRuleConfiguration) {
        val removedRule = rulesByIds.remove(rule.id) ?: return
        rules.remove(removedRule)
        ruleDeleted.fire(removedRule)
    }
}