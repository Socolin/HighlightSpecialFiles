package fr.socolin.rider.plugins.hsf.settings.models

import java.util.*

class HsfNestingRuleConfiguration(
    override val id: UUID,
    val pattern: String,
    override val order: Int,
    override val isShared: Boolean = false,
    override val isDisabled: Boolean = false,
) : IHsfRuleConfiguration<HsfNestingRuleConfiguration> {
    override fun isDifferentFrom(other: HsfNestingRuleConfiguration): Boolean {
        return pattern != other.pattern
                || order != other.order
                || isShared != other.isShared
                || isDisabled != other.isDisabled
    }

    companion object {
        fun createFrom(source: HsfNestingRuleConfiguration): HsfNestingRuleConfiguration {
            return HsfNestingRuleConfiguration(UUID.randomUUID()
                , source.pattern
                , source.order
                , source.isShared
                , source.isDisabled
            )
        }
    }
}