package fr.socolin.rider.plugins.hsf.settings.storage

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponentWithModificationTracker
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.util.xmlb.XmlSerializerUtil
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration

// https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html#the-appsettingscomponent-class
@State(
    name = "fr.socolin.rider.plugins.hsf.settings.storage.ProjectSettingsStorageService",
    storages = [Storage("fr.socolin.hsf.project.xml")]
)
class HsfProjectSettingsStorageService(private val project: Project) :
    PersistentStateComponentWithModificationTracker<HsfProjectSettingsState?>,
    Disposable {

    private val state = HsfProjectSettingsState()
    private val tracker = SimpleModificationTracker()

    override fun getState(): HsfProjectSettingsState {
        return state
    }

    override fun loadState(state: HsfProjectSettingsState) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    override fun getStateModificationCount(): Long {
        return tracker.modificationCount
    }

    override fun dispose() {}

    fun addRule(ruleConfiguration: HsfRuleConfiguration) {
        state.rules.add(ruleConfiguration)
        tracker.incModificationCount();
    }

    fun removeRule(ruleConfiguration: HsfRuleConfiguration) {
        state.rules.removeIf { r -> r.id == ruleConfiguration.id }
        tracker.incModificationCount();
    }

    fun getRulesConfigurations(): List<HsfRuleConfiguration> = state.rules

    companion object {
        fun getInstance(project: Project): HsfProjectSettingsStorageService {
            return project.getService(HsfProjectSettingsStorageService::class.java)
        }
    }

}