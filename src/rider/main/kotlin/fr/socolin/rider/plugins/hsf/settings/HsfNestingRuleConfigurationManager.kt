package fr.socolin.rider.plugins.hsf.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.storage.HsfProjectSettingsStorageService
import fr.socolin.rider.plugins.hsf.settings.storage.HsfUserSettingsStorageService

@Service(Service.Level.PROJECT)
class HsfNestingRuleConfigurationManager(project: Project) : HsfConfigurationManagerBase<HsfNestingRuleConfiguration>() {
    private val hsfUserSettingsStorageService = HsfUserSettingsStorageService.getInstance(project)
    private val hsfProjectSettingsStorageService = HsfProjectSettingsStorageService.getInstance(project)

    init {
        val allRules = ArrayList<HsfNestingRuleConfiguration>()
        allRules.addAll(hsfUserSettingsStorageService.getNestingRulesConfigurations())
        allRules.addAll(hsfProjectSettingsStorageService.getNestingRulesConfigurations())
        init(allRules)
    }

    override fun removeRuleFromConfigurationStorage(rule: HsfNestingRuleConfiguration) {
        if (rule.isShared)
            hsfProjectSettingsStorageService.removeNestingRule(rule)
        else
            hsfUserSettingsStorageService.removeNestingRule(rule)
    }

    override fun addRuleToConfigurationStorage(rule: HsfNestingRuleConfiguration) {
        if (rule.isShared)
            hsfProjectSettingsStorageService.addNestingRule(rule)
        else
            hsfUserSettingsStorageService.addNestingRule(rule)
    }

    companion object {
        fun getInstance(project: Project): HsfNestingRuleConfigurationManager {
            return project.getService(HsfNestingRuleConfigurationManager::class.java)
        }
    }
}