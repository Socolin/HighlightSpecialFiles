package fr.socolin.rider.plugins.hsf.settings.models

import java.util.*

interface IHsfRuleConfiguration<T : IHsfRuleConfiguration<T>> {
    val isDisabled: Boolean
    val id: UUID
    val order: Int
    val isShared: Boolean
    fun isDifferentFrom(other: T): Boolean
}