package fr.socolin.rider.plugins.hsf.settings.storage

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponentWithModificationTracker
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.util.xmlb.XmlSerializerUtil
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration

// https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html#the-appsettingscomponent-class
@State(
    name = "fr.socolin.rider.plugins.hsf.settings.storage.UserSettingsStorageService",
    storages = [Storage("fr.socolin.hsf.user.xml")]
)
@Service(Service.Level.PROJECT)
class HsfUserSettingsStorageService(private val project: Project) :
    PersistentStateComponentWithModificationTracker<HsfUserSettingsState?>, Disposable {

    private val state = HsfUserSettingsState()
    private val tracker = SimpleModificationTracker()

    override fun getState(): HsfUserSettingsState {
        return state
    }

    override fun loadState(state: HsfUserSettingsState) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    fun addRule(ruleConfiguration: HsfRuleConfiguration) {
        state.rules.add(ruleConfiguration)
        tracker.incModificationCount()
    }

    fun removeRule(ruleConfiguration: HsfRuleConfiguration) {
        state.rules.removeIf { r -> r.id == ruleConfiguration.id }
        tracker.incModificationCount()
    }

    fun getRulesConfigurations(): List<HsfRuleConfiguration> = state.rules

    fun addNestingRule(ruleConfiguration: HsfNestingRuleConfiguration) {
        state.nestingRules.add(ruleConfiguration)
        tracker.incModificationCount();
    }

    fun removeNestingRule(ruleConfiguration: HsfNestingRuleConfiguration) {
        state.nestingRules.removeIf { r -> r.id == ruleConfiguration.id }
        tracker.incModificationCount();
    }

    fun getNestingRulesConfigurations(): List<HsfNestingRuleConfiguration> = state.nestingRules
    override fun getStateModificationCount(): Long {
        return tracker.modificationCount
    }

    companion object {
        fun getInstance(project: Project): HsfUserSettingsStorageService {
            return project.getService(HsfUserSettingsStorageService::class.java)
        }
    }

    override fun dispose() {}
}