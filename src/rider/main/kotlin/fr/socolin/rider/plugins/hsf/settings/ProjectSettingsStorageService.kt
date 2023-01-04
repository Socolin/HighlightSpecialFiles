package fr.socolin.rider.plugins.hsf.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponentWithModificationTracker
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.util.xmlb.XmlSerializerUtil
import com.jetbrains.rd.platform.util.lifetime
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRuleConfiguration

// https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html#the-appsettingscomponent-class
@State(
    name = "fr.socolin.rider.plugins.hsf.settings.ProjectSettingsState",
    storages = [Storage("fr.socolin.hsf.xml")]
)
class ProjectSettingsStorageService(private val project: Project) :
    PersistentStateComponentWithModificationTracker<HsfProjectSettingsState?>,
    Disposable {

    @Volatile
    private var state = HsfProjectSettingsState()
    private val tracker = SimpleModificationTracker()

    init {
        registerAllPropertyToIncrementTrackerOnChanges(state)
    }

    override fun getState(): HsfProjectSettingsState {
        return state
    }

    override fun loadState(state: HsfProjectSettingsState) {
        XmlSerializerUtil.copyBean(state, this.state)
        registerAllPropertyToIncrementTrackerOnChanges(state)
    }

    private fun registerAllPropertyToIncrementTrackerOnChanges(state: HsfProjectSettingsState) {
        state.rulesManager.ruleChanged.advise(project.lifetime) { _: Pair<HsfHighlightingRuleConfiguration, HsfHighlightingRuleConfiguration> -> tracker.incModificationCount() }
        state.rulesManager.ruleDeleted.advise(project.lifetime) { _: HsfHighlightingRuleConfiguration -> tracker.incModificationCount() }
        state.rulesManager.ruleAdded.advise(project.lifetime) { _: HsfHighlightingRuleConfiguration -> tracker.incModificationCount() }
    }

    override fun getStateModificationCount(): Long {
        return tracker.modificationCount
    }

    companion object {
        fun getInstance(project: Project): ProjectSettingsStorageService {
            return project.getService(ProjectSettingsStorageService::class.java)
        }
    }

    override fun dispose() {}
}