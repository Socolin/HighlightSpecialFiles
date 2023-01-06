package fr.socolin.rider.plugins.hsf.settings.storage.converters

import com.intellij.util.xmlb.Converter
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import org.intellij.markdown.html.urlEncode
import java.net.URLDecoder
import java.util.*

open class HsfRuleConfigurationConverter(private val shared: Boolean) : Converter<ArrayList<HsfRuleConfiguration>>() {
    override fun fromString(s: String): ArrayList<HsfRuleConfiguration> {
        val rules = ArrayList<HsfRuleConfiguration>()
        val serializedRules = s.split('/')
        for (serializedRule in serializedRules) {
            var ruleId: UUID? = null
            var pattern: String? = null
            var order = 0
            var iconId: String = HsfIconManager.None.id
            var annotationText: String? = null
            var annotationStyle: String? = null
            var foregroundColorHex: String? = null
            var priority: Int? = null

            for (param in serializedRule.split('&')) {
                val confArray = param.split('=', limit = 2)
                if (confArray.size != 2) {
                    continue
                }

                val (key, value) = confArray;
                when (key) {
                    "ruleId" -> ruleId = UUID.fromString(value)
                    "pattern" -> pattern = URLDecoder.decode(value, Charsets.UTF_8)
                    "icon" -> iconId = URLDecoder.decode(value, Charsets.UTF_8)
                    "annotationText" -> annotationText = URLDecoder.decode(value, Charsets.UTF_8)
                    "annotationStyle" -> annotationStyle = URLDecoder.decode(value, Charsets.UTF_8)
                    "foregroundColorHex" -> foregroundColorHex = URLDecoder.decode(value, Charsets.UTF_8)
                    "priority" -> priority = value.toInt()
                    "order" -> order = value.toInt()
                }
            }

            if (ruleId == null || pattern == null)
                continue;

            rules.add(
                HsfRuleConfiguration(
                    ruleId,
                    pattern,
                    order,
                    iconId,
                    priority,
                    annotationText,
                    annotationStyle,
                    foregroundColorHex,
                    shared
                )
            )
        }

        return rules
    }

    override fun toString(rules: ArrayList<HsfRuleConfiguration>): String {
        val sb = StringBuilder()
        for (rule in rules) {
            sb.append("ruleId=").append(rule.id.toString())
            sb.append("&pattern=").append(urlEncode(rule.pattern))
            sb.append("&order=").append(rule.order)
            sb.append("&icon=").append(urlEncode(rule.iconId))
            if (rule.priority != null)
                sb.append("&priority=").append(rule.priority)
            if (rule.annotationText != null) {
                sb.append("&annotationText=").append(urlEncode(rule.annotationText))
                if (rule.annotationStyle != null)
                    sb.append("&annotationStyle=").append(urlEncode(rule.annotationStyle))
            }
            if (rule.foregroundColorHex != null)
                sb.append("&foregroundColorHex=").append(urlEncode(rule.foregroundColorHex))
            sb.append('/')
        }
        if (sb.length > 1)
            sb.setLength(sb.length - 1)
        return sb.toString()
    }
}

class ProjectHsfRuleConfigurationConverter : HsfRuleConfigurationConverter(true)
class UserHsfRuleConfigurationConverter : HsfRuleConfigurationConverter(false)