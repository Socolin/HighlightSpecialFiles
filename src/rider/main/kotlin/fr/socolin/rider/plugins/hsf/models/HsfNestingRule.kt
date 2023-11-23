package fr.socolin.rider.plugins.hsf.models

import java.util.*
import java.util.regex.Pattern

class HsfNestingRule(
    val id: UUID,
    val pattern: Pattern,
    val order: Int
) {
}