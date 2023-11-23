package fr.socolin.rider.plugins.hsf.settings.storage

import com.intellij.util.xmlb.annotations.OptionTag
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.storage.converters.ProjectHsfNestingRuleConfigurationConverter
import fr.socolin.rider.plugins.hsf.settings.storage.converters.ProjectHsfRuleConfigurationConverter

class HsfProjectSettingsState {
    @OptionTag(converter = ProjectHsfRuleConfigurationConverter::class)
    val rules = ArrayList<HsfRuleConfiguration>()

    @OptionTag(converter = ProjectHsfNestingRuleConfigurationConverter::class)
    val nestingRules = ArrayList<HsfNestingRuleConfiguration>()
}