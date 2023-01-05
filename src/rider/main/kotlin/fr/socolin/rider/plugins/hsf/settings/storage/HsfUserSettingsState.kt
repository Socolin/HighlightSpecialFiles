package fr.socolin.rider.plugins.hsf.settings.storage

import com.intellij.util.xmlb.annotations.OptionTag
import fr.socolin.rider.plugins.hsf.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.storage.converters.UserHsfRuleConfigurationConverter

class HsfUserSettingsState {
    @OptionTag(converter = UserHsfRuleConfigurationConverter::class)
    val rules = ArrayList<HsfRuleConfiguration>()
}