package fr.socolin.rider.plugins.hsf.settings.storage

import com.intellij.util.xmlb.annotations.OptionTag
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.storage.converters.UserHsfNestingRuleConfigurationConverter
import fr.socolin.rider.plugins.hsf.settings.storage.converters.UserHsfRuleConfigurationConverter

class HsfUserSettingsState {
    @OptionTag(converter = UserHsfRuleConfigurationConverter::class)
    val rules = ArrayList<HsfRuleConfiguration>()

    @OptionTag(converter = UserHsfNestingRuleConfigurationConverter::class)
    val nestingRules = ArrayList<HsfNestingRuleConfiguration>()
}