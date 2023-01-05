package fr.socolin.rider.plugins.hsf.settings

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.containers.SortedList
import com.jetbrains.rd.util.putUnique
import com.jetbrains.rd.util.reactive.Signal
import fr.socolin.rider.plugins.hsf.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.storage.HsfProjectSettingsStorageService
import fr.socolin.rider.plugins.hsf.settings.storage.HsfUserSettingsStorageService
import java.util.*

class HsfConfigurationManager(project: Project) {
    private val logger: Logger = Logger.getInstance(HsfConfigurationManager::class.java)
    private val hsfUserSettingsStorageService = HsfUserSettingsStorageService.getInstance(project);
    private val hsfProjectSettingsStorageService = HsfProjectSettingsStorageService.getInstance(project);

    fun getOrderedRules(): Collection<HsfRuleConfiguration> = rulesConfigurationsOrdered;
    private val rulesConfigurationsOrdered = SortedList<HsfRuleConfiguration> { a, b -> a.order - b.order }
    private val rulesByIds: HashMap<UUID, HsfRuleConfiguration> = HashMap()

    val ruleChanged: Signal<Pair<HsfRuleConfiguration, HsfRuleConfiguration>> = Signal()
    val ruleDeleted: Signal<HsfRuleConfiguration> = Signal()
    val ruleAdded: Signal<HsfRuleConfiguration> = Signal()


    init {
        try {
            rulesConfigurationsOrdered.addAll(hsfUserSettingsStorageService.getRulesConfigurations())
            rulesConfigurationsOrdered.addAll(hsfProjectSettingsStorageService.getRulesConfigurations())
            rulesConfigurationsOrdered.forEach { r -> rulesByIds.putUnique(r.id, r) }
        }
        catch (e: Exception) {
            logger.error("An error occurred while loading rules. " +
                    "Please fix problem and restart the IDE, the rules can be found in .idea/fr.socolin.hsf.project.xml" +
                    " and fr.socolin.hsf.user.xml", e)
        }
    }

    fun updateRules(updatedRules: List<HsfRuleConfiguration>) {
        val ruleToRemove = ArrayList<HsfRuleConfiguration>()
        for (rule in rulesConfigurationsOrdered) {
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

    fun isDifferentFrom(rules: List<HsfRuleConfiguration>): Boolean {
        if (rules.count() != this.rulesConfigurationsOrdered.size)
            return true
        for (rule in rules) {
            val existingRule = rulesByIds.getOrDefault(rule.id, null) ?: return true
            if (existingRule.isDifferentFrom(rule))
                return true
        }

        return false
    }

    private fun updateRule(rule: HsfRuleConfiguration) {
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

    private fun addRule(rule: HsfRuleConfiguration) {
        rulesByIds.putUnique(rule.id, rule)
        rulesConfigurationsOrdered.add(rule)
        ruleAdded.fire(rule)
        addRuleToConfigurationStorage(rule)
    }

    private fun removeRule(rule: HsfRuleConfiguration) {
        rulesConfigurationsOrdered.removeIf { r -> r.id == rule.id }
        rulesByIds.remove(rule.id)
        ruleDeleted.fire(rule)
        removeRuleFromConfigurationStorage(rule)
    }

    private fun removeRuleFromConfigurationStorage(rule: HsfRuleConfiguration) {
        if (rule.isShared)
            hsfProjectSettingsStorageService.removeRule(rule);
        else
            hsfUserSettingsStorageService.removeRule(rule);
    }

    private fun addRuleToConfigurationStorage(rule: HsfRuleConfiguration) {
        if (rule.isShared)
            hsfProjectSettingsStorageService.addRule(rule);
        else
            hsfUserSettingsStorageService.addRule(rule);
    }

    companion object {
        fun getInstance(project: Project): HsfConfigurationManager {
            return project.getService(HsfConfigurationManager::class.java)
        }
    }
}