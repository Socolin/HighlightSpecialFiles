package fr.socolin.rider.plugins.hsf.settings.storage

import com.intellij.util.xmlb.annotations.OptionTag
import fr.socolin.rider.plugins.hsf.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.storage.converters.HsfRuleConfigurationConverter
import fr.socolin.rider.plugins.hsf.settings.storage.converters.ProjectHsfRuleConfigurationConverter

class HsfProjectSettingsState {
    @OptionTag(converter = ProjectHsfRuleConfigurationConverter::class)
    val rules = ArrayList<HsfRuleConfiguration>()
}