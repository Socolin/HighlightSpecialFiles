package fr.socolin.rider.plugins.hsf.settings.ui

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.io.exists
import com.intellij.util.io.systemIndependentPath
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.projectView.solutionDirectoryPath
import fr.socolin.rider.plugins.hsf.models.HsfIconManager
import fr.socolin.rider.plugins.hsf.settings.models.HsfNestingRuleConfiguration
import fr.socolin.rider.plugins.hsf.settings.models.HsfRuleConfiguration
import java.nio.file.LinkOption
import javax.swing.JPanel
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.relativeTo

class HsfSettingsComponent(
    private val project: Project,
    lifetime: Lifetime
) {
    private val rulesComponent = RulesComponent(project, lifetime)
    private val nestingRulesComponent = NestingRulesComponent(project, lifetime)
    private val hsfIconManager = HsfIconManager.getInstance(project)

    fun getPanel(): JPanel {
        return panel {
            group("Icons") {
                row {
                    text("You can provide additional icons by adding them in on of the following folders").resizableColumn()
                    button("Reload Icons") { hsfIconManager.reloadIcons() }
                }
                group("Icon Folders") {
                    for (iconFolder in HsfIconManager.listIconsFolders(project, shouldExists = false)) {
                        row {
                            link(iconFolder.relativeTo(project.solutionDirectoryPath).systemIndependentPath) {
                                if (!iconFolder.exists(LinkOption.NOFOLLOW_LINKS))
                                    iconFolder.createDirectory()
                                RevealFileAction.openDirectory(iconFolder)
                            }
                        }
                    }
                }
            }
            row {
                cell(rulesComponent.panel).align(AlignX.FILL)
            }
            row {
                cell(nestingRulesComponent.panel).align(AlignX.FILL)
            }
        }
    }

    fun setRules(rules: Collection<HsfRuleConfiguration>) {
        rulesComponent.setRules(rules)
    }

    fun getRules(): List<HsfRuleConfiguration> {
        return rulesComponent.getRules()
    }

    fun setNestingRules(rules: Collection<HsfNestingRuleConfiguration>) {
        nestingRulesComponent.setRules(rules)
    }

    fun getNestingRules(): List<HsfNestingRuleConfiguration> {
        return nestingRulesComponent.getRules()
    }
}