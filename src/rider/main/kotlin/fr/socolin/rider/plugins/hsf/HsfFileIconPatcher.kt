package fr.socolin.rider.plugins.hsf

import com.intellij.ide.FileIconPatcher
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class HsfFileIconPatcher : FileIconPatcher {
    override fun patchIcon(baseIcon: Icon, file: VirtualFile, flags: Int, project: Project?): Icon {
        if (project == null) return baseIcon

        val hsfActiveRuleManager = HsfActiveRuleManager.getInstance(project)
        for (rule in hsfActiveRuleManager.rules) {
            if (rule.icon.icon == null)
                continue

            val match = rule.pattern.matcher(file.name)
            if (match.matches()) {
                return rule.icon.icon
            }
        }
        return baseIcon
    }
}