package fr.socolin.rider.plugins.hsf.settings.converters

import com.intellij.util.xmlb.Converter
import fr.socolin.rider.plugins.hsf.models.HsfAnnotationTextStyles
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRuleConfigurationManager
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import org.intellij.markdown.html.urlEncode
import java.net.URLDecoder
import java.util.*

class HighlightingRuleManagerConverter : Converter<HsfHighlightingRuleConfigurationManager>() {

    override fun fromString(s: String): HsfHighlightingRuleConfigurationManager {
        val ruleManager = HsfHighlightingRuleConfigurationManager()
        val rules = s.split('/')
        for (rule in rules) {
            var ruleId: UUID? = null
            var pattern: String? = null
            var iconId: String = HsfIconManager.None.id
            var annotationText: String? = null
            var annotationStyle: String = HsfAnnotationTextStyles.defaultId
            var foregroundColorHex: String? = null
            var priority: Int? = null
            for (param in rule.split('&')) {
                val confArray = param.split('=', limit = 2)
                if (confArray.size != 2) {
                    continue
                }
                val key = confArray[0]
                val value = confArray[1]
                if (key == "ruleId")
                    ruleId = UUID.fromString(value)
                if (key == "pattern")
                    pattern = URLDecoder.decode(value, Charsets.UTF_8)
                if (key == "icon")
                    iconId = URLDecoder.decode(value, Charsets.UTF_8)
                if (key == "annotationText")
                    annotationText = URLDecoder.decode(value, Charsets.UTF_8)
                if (key == "annotationStyle")
                    annotationStyle = URLDecoder.decode(value, Charsets.UTF_8)
                if (key == "foregroundColorHex")
                    foregroundColorHex = URLDecoder.decode(value, Charsets.UTF_8)
                if (key == "priority")
                    priority = value.toInt()
            }
            if (ruleId != null && pattern != null)
                ruleManager.addRule(ruleId, pattern, iconId, priority, annotationText, annotationStyle, foregroundColorHex)
        }
        return ruleManager
    }

    override fun toString(ruleManager: HsfHighlightingRuleConfigurationManager): String {
        val sb = StringBuilder()
        for (rule in ruleManager.rules) {
            sb.append("ruleId=").append(rule.id.toString())
            sb.append("&pattern=").append(urlEncode(rule.pattern))
            sb.append("&icon=").append(urlEncode(rule.iconId))
            if (rule.priority != null)
                sb.append("&priority=").append(rule.priority)
            if (rule.annotationText != null) {
                sb.append("&annotationText=").append(urlEncode(rule.annotationText))
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