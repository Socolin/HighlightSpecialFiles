package fr.socolin.rider.plugins.hsf.settings

import com.intellij.util.xmlb.annotations.OptionTag
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRuleConfigurationManager
import fr.socolin.rider.plugins.hsf.settings.converters.HighlightingRuleManagerConverter

class HsfProjectSettingsState {
    @OptionTag(converter = HighlightingRuleManagerConverter::class)
    val rulesManager = HsfHighlightingRuleConfigurationManager()
}