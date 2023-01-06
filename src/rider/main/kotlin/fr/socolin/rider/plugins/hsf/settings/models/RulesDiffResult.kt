package fr.socolin.rider.plugins.hsf.settings.models

import java.util.ArrayList

data class RulesDiffResult(
    val addedRules: ArrayList<HsfRuleConfiguration>,
    val updatedRules: ArrayList<HsfRuleConfiguration>,
    val removedRules: ArrayList<HsfRuleConfiguration>,
)
