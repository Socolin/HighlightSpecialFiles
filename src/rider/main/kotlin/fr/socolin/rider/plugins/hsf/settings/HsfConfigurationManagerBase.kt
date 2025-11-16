package fr.socolin.rider.plugins.hsf.settings

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.containers.SortedList
import com.jetbrains.rd.util.putUnique
import com.jetbrains.rd.util.reactive.Signal
import fr.socolin.rider.plugins.hsf.settings.models.IHsfRuleConfiguration
import java.util.*

abstract class HsfConfigurationManagerBase<TRuleConfiguration : IHsfRuleConfiguration<TRuleConfiguration>> {
    private val logger: Logger = Logger.getInstance(HsfConfigurationManagerBase::class.java)

    fun getOrderedRules(): Collection<TRuleConfiguration> = rulesConfigurationsOrdered
    private val rulesConfigurationsOrdered = SortedList<TRuleConfiguration> { a, b -> a.order - b.order }
    private val rulesByIds: HashMap<UUID, TRuleConfiguration> = HashMap()

    val ruleChanged: Signal<Pair<TRuleConfiguration, TRuleConfiguration>> = Signal()
    val ruleDeleted: Signal<TRuleConfiguration> = Signal()
    val ruleAdded: Signal<TRuleConfiguration> = Signal()

    fun init(rules: List<TRuleConfiguration>) {
        try {
            rulesConfigurationsOrdered.addAll(rules)
            rulesConfigurationsOrdered.forEach { r -> rulesByIds.putUnique(r.id, r) }
        } catch (e: Exception) {
            logger.error(
                "An error occurred while loading rules. " +
                        "Please fix problem and restart the IDE, the rules can be found in .idea/fr.socolin.hsf.project.xml" +
                        " and fr.socolin.hsf.user.xml", e
            )
        }
    }

    fun updateRules(updatedRules: List<TRuleConfiguration>) {
        val diffResult = HsfRuleConfigurationHelper.computeDiffBetweenRules(rulesConfigurationsOrdered, updatedRules)
        for (rule in diffResult.removedRules) {
            removeRule(rule)
        }
        for (rule in diffResult.updatedRules) {
            updateRule(rule)
        }
        for (rule in diffResult.addedRules) {
            addRule(rule)
        }
    }


    fun isDifferentFrom(rules: List<TRuleConfiguration>): Boolean {
        if (rules.count() != this.rulesConfigurationsOrdered.size)
            return true
        for (rule in rules) {
            val existingRule = rulesByIds.getOrDefault(rule.id, null) ?: return true
            if (existingRule.isDifferentFrom(rule))
                return true
        }

        return false
    }

    private fun updateRule(rule: TRuleConfiguration) {
        val currenRule = rulesByIds.getOrDefault(rule.id, null) ?: return
        if (currenRule.isDifferentFrom(rule)) {
            rulesConfigurationsOrdered.removeIf { r -> r.id == rule.id }
            rulesConfigurationsOrdered.add(rule)
            rulesByIds.replace(rule.id, rule)
            ruleChanged.fire(Pair(currenRule, rule))
            removeRuleFromConfigurationStorage(currenRule)
            addRuleToConfigurationStorage(rule)
        }
    }

    private fun addRule(rule: TRuleConfiguration) {
        rulesByIds.putUnique(rule.id, rule)
        rulesConfigurationsOrdered.add(rule)
        ruleAdded.fire(rule)
        addRuleToConfigurationStorage(rule)
    }

    private fun removeRule(rule: TRuleConfiguration) {
        rulesConfigurationsOrdered.removeIf { r -> r.id == rule.id }
        rulesByIds.remove(rule.id)
        ruleDeleted.fire(rule)
        removeRuleFromConfigurationStorage(rule)
    }

    protected abstract fun removeRuleFromConfigurationStorage(rule: TRuleConfiguration)
    protected abstract fun addRuleToConfigurationStorage(rule: TRuleConfiguration)
}