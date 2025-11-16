package fr.socolin.rider.plugins.hsf.settings.storage.converters

import com.intellij.util.xmlb.Converter
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import org.intellij.markdown.html.urlEncode
import java.net.URLDecoder
import java.util.*

open class HsfNestingRuleConfigurationConverter(private val shared: Boolean) : Converter<ArrayList<HsfNestingRuleConfiguration>>() {
    override fun fromString(s: String): ArrayList<HsfNestingRuleConfiguration> {
        val rules = ArrayList<HsfNestingRuleConfiguration>()
        val serializedRules = s.split('/')
        for (serializedRule in serializedRules) {
            var ruleId: UUID? = null
            var pattern: String? = null
            var order = 0
            var disabled = false

            for (param in serializedRule.split('&')) {
                val confArray = param.split('=', limit = 2)
                if (confArray.size != 2) {
                    continue
                }

                val (key, value) = confArray
                when (key) {
                    "ruleId" -> ruleId = UUID.fromString(value)
                    "pattern" -> pattern = URLDecoder.decode(value, Charsets.UTF_8)
                    "order" -> order = value.toInt()
                    "disabled" -> disabled = value.toBoolean()
                }
            }

            if (ruleId == null || pattern == null)
                continue

            rules.add(
                HsfNestingRuleConfiguration(
                    ruleId,
                    pattern,
                    order,
                    shared,
                    disabled
                )
            )
        }

        return rules
    }

    override fun toString(rules: ArrayList<HsfNestingRuleConfiguration>): String {
        val sb = StringBuilder()
        for (rule in rules) {
            sb.append("ruleId=").append(rule.id.toString())
            sb.append("&pattern=").append(urlEncode(rule.pattern))
            sb.append("&order=").append(rule.order)
            sb.append("&disabled=").append(rule.isDisabled)
            sb.append('/')
        }
        if (sb.length > 1)
            sb.setLength(sb.length - 1)
        return sb.toString()
    }
}

class ProjectHsfNestingRuleConfigurationConverter : HsfNestingRuleConfigurationConverter(true)
class UserHsfNestingRuleConfigurationConverter : HsfNestingRuleConfigurationConverter(false)