package fr.socolin.rider.plugins.hsf.settings.models

import java.util.ArrayList

data class RulesDiffResult<TRuleConfiguration>(
    val addedRules: ArrayList<TRuleConfiguration>,
    val updatedRules: ArrayList<TRuleConfiguration>,
    val removedRules: ArrayList<TRuleConfiguration>,
)
