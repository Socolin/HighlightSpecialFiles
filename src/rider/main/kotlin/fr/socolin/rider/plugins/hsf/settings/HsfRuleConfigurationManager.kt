package fr.socolin.rider.plugins.hsf.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.storage.HsfProjectSettingsStorageService
import fr.socolin.rider.plugins.hsf.settings.storage.HsfUserSettingsStorageService

@Service(Service.Level.PROJECT)
class HsfRuleConfigurationManager(project: Project) : HsfConfigurationManagerBase<HsfRuleConfiguration>() {
    private val hsfUserSettingsStorageService = HsfUserSettingsStorageService.getInstance(project);
    private val hsfProjectSettingsStorageService = HsfProjectSettingsStorageService.getInstance(project);

    init {
        val allRules = ArrayList<HsfRuleConfiguration>();
        allRules.addAll(hsfProjectSettingsStorageService.getRulesConfigurations())
        allRules.addAll(hsfUserSettingsStorageService.getRulesConfigurations())
        init(allRules)
    }

    override fun removeRuleFromConfigurationStorage(rule: HsfRuleConfiguration) {
        if (rule.isShared)
            hsfProjectSettingsStorageService.removeRule(rule);
        else
            hsfUserSettingsStorageService.removeRule(rule);
    }

    override fun addRuleToConfigurationStorage(rule: HsfRuleConfiguration) {
        if (rule.isShared)
            hsfProjectSettingsStorageService.addRule(rule);
        else
            hsfUserSettingsStorageService.addRule(rule);
    }

    companion object {
        fun getInstance(project: Project): HsfRuleConfigurationManager {
            return project.getService(HsfRuleConfigurationManager::class.java)
        }
    }
}