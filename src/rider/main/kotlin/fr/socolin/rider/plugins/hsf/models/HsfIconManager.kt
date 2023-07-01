package fr.socolin.rider.plugins.hsf.models

import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.icons.CachedImageIcon
import com.intellij.ui.scale.ScaleContext
import com.intellij.ui.scale.UserScaleContext
import com.intellij.util.io.systemIndependentPath
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.util.reactive.Signal
import icons.RiderIcons
import java.net.URL
import java.nio.file.Path
import javax.swing.Icon
import kotlin.io.path.*

class HsfIconManager(private val project: Project) {
    val onReload = Signal<Collection<HsfIcon>>()

    private val logger: Logger = Logger.getInstance(HsfIconManager::class.java)
    private val allIcons: ArrayList<HsfIcon> = ArrayList()
    private val iconsByIds: HashMap<String, HsfIcon> = HashMap()

    val icons: Collection<HsfIcon>
        get() = allIcons

    init {
        loadIcons()
    }

    private fun addProjectIcons(project: Project) {
        val iconFolders = listIconsFolders(project)
        for (iconFolder in iconFolders) {
            val iconFiles = iconFolder.listDirectoryEntries("*.*")
            for (iconFile in iconFiles) {
                if (iconFile.fileName.nameWithoutExtension.endsWith("_dark"))
                    continue;
                addProjectIcon(iconFile)
            }
        }
    }

    fun reloadIcons() {
        allIcons.clear()
        // FIXME: Only reload project icons ?
        loadIcons()
        onReload.fire(icons)
    }

    private fun loadIcons() {
        allIcons.add(None)
        addDefaultIcons()
        addProjectIcons(project)
        addBuiltInIcons()
    }

    private fun addDefaultIcons() {
        addDefaultIcon(
            "hsf.icons.defaults.exceptions_folder",
            "Exceptions folder",
            "/icons/exceptions_folder.svg",
        )
        addDefaultIcon(
            "hsf.icons.defaults.request",
            "Request",
            "/icons/request.svg",
        )
        addDefaultIcon(
            "hsf.icons.defaults.response",
            "Response",
            "/icons/response.svg",
        )
    }

    private fun addBuiltInIcons() {
        addBuiltInIcon("Nodes.Folder", AllIcons.Nodes.Folder)
        addBuiltInIcon("Nodes.ConfigFolder", AllIcons.Nodes.ConfigFolder)
        addBuiltInIcon("Nodes.WebFolder", AllIcons.Nodes.WebFolder)
        addBuiltInIcon("Nodes.LogFolder", AllIcons.Nodes.LogFolder)
        addBuiltInIcon("Nodes.ResourceBundle", AllIcons.Nodes.ResourceBundle)
        addBuiltInIcon("Nodes.ResourcesRoot", AllIcons.Modules.ResourcesRoot)
        addBuiltInIcon("Nodes.TestGroup", AllIcons.Nodes.TestGroup)

        addBuiltInIcon("Debugger.Db_exception_breakpoint", AllIcons.Debugger.Db_exception_breakpoint)
        addBuiltInIcon("RunConfigurations.TestError", AllIcons.RunConfigurations.TestError)
        addBuiltInIcon("RunConfigurations.TestFailed", AllIcons.RunConfigurations.TestFailed)
        addBuiltInIcon("Nodes.AbstractException", AllIcons.Nodes.AbstractException)
        addBuiltInIcon("Nodes.Annotationtype", AllIcons.Nodes.Annotationtype)
        addBuiltInIcon("Nodes.Class", AllIcons.Nodes.Class)
        addBuiltInIcon("Nodes.ExceptionClass", AllIcons.Nodes.ExceptionClass)
        addBuiltInIcon("Nodes.Function", AllIcons.Nodes.Function)
        addBuiltInIcon("Nodes.Interface", AllIcons.Nodes.Interface)
        addBuiltInIcon("Nodes.Method", AllIcons.Nodes.Method)
        addBuiltInIcon("Nodes.Static", AllIcons.Nodes.Static)
        addBuiltInIcon("Nodes.Type", AllIcons.Nodes.Type)
        addBuiltInIcon("Nodes.Test", AllIcons.Nodes.Test)
        addBuiltInIcon("Nodes.Variable", AllIcons.Nodes.Variable)
        addBuiltInIcon("RiderIcons.Diagramming.Injection", RiderIcons.Diagramming.Injection)
    }

    private fun addDefaultIcon(id: String, name: String, filePath: String) {
        val hsfIcon = HsfIcon(id, name, IconLoader.getIcon(filePath, HsfIconManager::class.java))
        addIcon(hsfIcon)
    }

    private fun addBuiltInIcon(name: String, icon: Icon) {
        val hsfIcon = HsfIcon("hsf.icons.built-in.$name", name, icon)
        addIcon(hsfIcon)
    }

    private fun addProjectIcon(iconFile: Path) {
        val icon: Icon? = loadIconFromDisk(iconFile)
        val iconName = iconFile.name.substringBeforeLast('.')
        val hsfIcon = HsfIcon("hsf.icons.project." + iconFile.fileName, iconName, icon)
        addIcon(hsfIcon)
    }

    private fun loadIconFromDisk(iconFile: Path): Icon? {
        try {
            val filePathUrl = if (SystemInfo.isWindows) "file:/" + iconFile.systemIndependentPath else "file://" + iconFile.systemIndependentPath
            return IconLoader.findIcon(URL(filePathUrl));
        } catch (e: Exception) {
            logger.error("Failed to load project icon $iconFile", e)
        }
        return null;
    }

    private fun addIcon(hsfIcon: HsfIcon) {
        allIcons.add(hsfIcon)
        iconsByIds[hsfIcon.id] = hsfIcon
    }

    fun getIcon(id: String?): HsfIcon {
        return iconsByIds.getOrDefault(id, None)
    }

    companion object {
        val None = HsfIcon("none", "None", null)

        fun getInstance(project: Project): HsfIconManager {
            return project.getService(HsfIconManager::class.java)
        }

        private const val DIRECTORY_ICONS_NAME = "socolin.fr.hsf.icons"
        fun listIconsFolders(project: Project, shouldExists: Boolean = true): ArrayList<Path> {
            val iconFolders = ArrayList<Path>()

            val basePath = project.basePath
            if (basePath != null) {
                val mainIdeaFolder = Path(basePath, Project.DIRECTORY_STORE_FOLDER)
                val iconFolder = mainIdeaFolder.resolve(DIRECTORY_ICONS_NAME)
                if (iconFolder.exists() || !shouldExists)
                    iconFolders.add(iconFolder)
            }

            val projectFilePath = project.projectFilePath
            if (projectFilePath != null) {
                val projectIdeaFolder = Path(projectFilePath).parent
                val iconFolder = projectIdeaFolder.resolve(DIRECTORY_ICONS_NAME)
                if (iconFolder.exists() || !shouldExists)
                    iconFolders.add(iconFolder)
            }

            return iconFolders
        }
    }
}