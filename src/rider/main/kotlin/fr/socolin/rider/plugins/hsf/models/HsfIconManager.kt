package fr.socolin.rider.plugins.hsf.models

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.IconLoader.CachedImageIcon
import com.intellij.openapi.util.ImageDataByUrlLoader
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.io.systemIndependentPath
import com.jetbrains.rd.util.reactive.Signal
import java.net.URL
import java.nio.file.Path
import java.util.*
import javax.swing.Icon
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

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
    }

    private fun addDefaultIcons() {
        // For future
        // addDefaultIcon(
        //     "hsf.icons.defaults.xxxxx",
        //     "Xxxxxx",
        //     "/icons/xxxxx.svg",
        // )
    }

    private fun addDefaultIcon(id: String, name: String, filePath: String) {
        val hsfIcon = HsfIcon(id, name, IconLoader.getIcon(filePath, HsfIconManager::class.java))
        addIcon(hsfIcon)
    }

    private fun addProjectIcon(iconFile: Path) {
        val icon: Icon? = loadIconFromDisk(iconFile)
        val iconName = iconFile.name.substringBeforeLast('.')
        val hsfIcon = HsfIcon("hsf.icons.project." + iconFile.fileName, iconName, icon)
        addIcon(hsfIcon)
    }

    @Suppress("UnstableApiUsage")
    private fun loadIconFromDisk(iconFile: Path): Icon? {
        try {
            val resolver =
                ImageDataByUrlLoader(
                    URL("file://" + iconFile.systemIndependentPath),
                    iconFile.systemIndependentPath,
                    null,
                    false
                )
            resolver.resolve()
            var cachedImageIcon = CachedImageIcon(iconFile.systemIndependentPath, resolver, null, null)

            val scaleContext = ScaleContext.create()
            if (cachedImageIcon.scaleContext != scaleContext) {
                // honor scale context as 'iconCache' doesn't do that
                cachedImageIcon = cachedImageIcon.copy()
                cachedImageIcon.updateScaleContext(scaleContext)
            }
            // FIXME: Is 16 always valid ?
            return cachedImageIcon.scaleToWidth(16.0f)
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