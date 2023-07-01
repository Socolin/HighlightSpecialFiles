package fr.socolin.rider.plugins.hsf.models

import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.scale.ScaleContext
import com.intellij.ui.scale.UserScaleContext
import com.intellij.util.io.systemIndependentPath
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.util.reactive.Signal
import java.net.URL
import java.nio.file.Path
import javax.swing.Icon
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class HsfIconManager(private val project: Project) {
    val onReload = Signal<Collection<HsfIcon>>()

    private val logger: Logger = Logger.getInstance(HsfIconManager::class.java)
    private val allIcons: ArrayList<HsfIcon> = ArrayList()
    private val filesIcons: ArrayList<HsfIcon> = ArrayList()
    private val folderIcons: ArrayList<HsfIcon> = ArrayList()
    private val iconsByIds: HashMap<String, HsfIcon> = HashMap()

    val icons: Collection<HsfIcon>
        get() = filesIcons
    val iconsForFolder: Collection<HsfIcon>
        get() = folderIcons

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
        filesIcons.clear()
        folderIcons.clear()
        // FIXME: Only reload project icons ?
        loadIcons()
        onReload.fire(icons)
    }

    private fun loadIcons() {
        allIcons.add(None)
        filesIcons.add(None)
        folderIcons.add(None)
        addBuiltInIcons()
        addDefaultIcons()
        addProjectIcons(project)
    }

    private fun addDefaultIcons() {
        addDefaultIcon(
            "hsf.icons.defaults.exceptions_folder",
            "Exceptions folder",
            "/icons/exceptions_folder.svg",
        )
    }

    private fun addBuiltInIcons() {
        addBuiltInIcon("Nodes.Folder", AllIcons.Nodes.Folder, true)
        addBuiltInIcon("Nodes.ConfigFolder", AllIcons.Nodes.ConfigFolder, true)
        addBuiltInIcon("Nodes.WebFolder", AllIcons.Nodes.WebFolder, true)
        addBuiltInIcon("Nodes.LogFolder", AllIcons.Nodes.LogFolder, true)
        addBuiltInIcon("Nodes.ResourceBundle", AllIcons.Nodes.ResourceBundle, true)
        addBuiltInIcon("Nodes.ResourcesRoot", AllIcons.Modules.ResourcesRoot, true)
        addBuiltInIcon("Nodes.TestGroup", AllIcons.Nodes.TestGroup, true)

        addBuiltInIcon("Debugger.Db_exception_breakpoint", AllIcons.Debugger.Db_exception_breakpoint, false)
        addBuiltInIcon("RunConfigurations.TestError", AllIcons.RunConfigurations.TestError, false)
        addBuiltInIcon("RunConfigurations.TestFailed", AllIcons.RunConfigurations.TestFailed, false)
        addBuiltInIcon("Nodes.AbstractException", AllIcons.Nodes.AbstractException, false)
        addBuiltInIcon("Nodes.Annotationtype", AllIcons.Nodes.Annotationtype, false)
        addBuiltInIcon("Nodes.Class", AllIcons.Nodes.Class, false)
        addBuiltInIcon("Nodes.ExceptionClass", AllIcons.Nodes.ExceptionClass, false)
        addBuiltInIcon("Nodes.Function", AllIcons.Nodes.Function, false)
        addBuiltInIcon("Nodes.Interface", AllIcons.Nodes.Interface, false)
        addBuiltInIcon("Nodes.Method", AllIcons.Nodes.Method, false)
        addBuiltInIcon("Nodes.Static", AllIcons.Nodes.Static, false)
        addBuiltInIcon("Nodes.Type", AllIcons.Nodes.Type, false)
        addBuiltInIcon("Nodes.Test", AllIcons.Nodes.Test, false)
        addBuiltInIcon("Nodes.Variable", AllIcons.Nodes.Variable, false)
    }

    private fun addDefaultIcon(id: String, name: String, filePath: String) {
        val hsfIcon = HsfIcon(id, name, IconLoader.getIcon(filePath, HsfIconManager::class.java), false)
        addIcon(hsfIcon)
    }

    private fun addBuiltInIcon(name: String, icon: Icon, folderOnly: Boolean) {
        val hsfIcon = HsfIcon("hsf.icons.built-in.$name", name, icon , folderOnly)
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
            val imageDataByUrlLoaderClass = Class.forName("com.intellij.openapi.util.ImageDataByUrlLoader")
            val imageDataByUrlLoaderConstructor = imageDataByUrlLoaderClass.constructors.find { c -> c.parameterCount == 4}
            val filePathUrl = if (SystemInfo.isWindows)  "file:/" + iconFile.systemIndependentPath else  "file://" + iconFile.systemIndependentPath
            val resolver = imageDataByUrlLoaderConstructor!!.newInstance(
                URL(filePathUrl),
                iconFile.systemIndependentPath,
                null,
                false
            );
            val resolveMethod = imageDataByUrlLoaderClass.getDeclaredMethod("resolve");
            resolveMethod.invoke(resolver);
            val cachedImageIconClass = Class.forName("com.intellij.openapi.util.CachedImageIcon")
            val scalableIconClass = Class.forName("com.intellij.openapi.util.ScalableIcon")
            val scaleContextAwareClass = Class.forName("com.intellij.ui.scale.ScaleContextAware")
            val cachedImageIconConstructor = cachedImageIconClass.constructors.find { c -> c.parameterCount == 2 && c.parameterTypes[0].name == "java.lang.String" }
            var cachedImageIcon = cachedImageIconConstructor!!.newInstance(
                iconFile.systemIndependentPath, resolver
            )
            val scaleToWidth = scalableIconClass.getDeclaredMethod("scaleToWidth", Float::class.java);
            val copyMethod = cachedImageIconClass.getDeclaredMethod("copy");
            val getScaleContextMethod = scaleContextAwareClass.getDeclaredMethod("getScaleContext");
            val updateScaleContextMethod = scaleContextAwareClass.getDeclaredMethod("updateScaleContext", UserScaleContext::class.java);

            val scaleContext = ScaleContext.create()
            if (getScaleContextMethod.invoke(cachedImageIcon) != scaleContext) {
                // honor scale context as 'iconCache' doesn't do that
                cachedImageIcon = copyMethod(cachedImageIcon);
                updateScaleContextMethod.invoke(cachedImageIcon, scaleContext)
            }
            return scaleToWidth.invoke(cachedImageIcon, JBUI.pixScale(16.0f)) as Icon
        } catch (e: Exception) {
            logger.error("Failed to load project icon $iconFile", e)
        }
        return null;
    }
/*    private fun loadIconFromDiskInternalApi(iconFile: Path): Icon? {
        try {
            val filePathUrl = if (SystemInfo.isWindows)  "file:/" + iconFile.systemIndependentPath else  "file://" + iconFile.systemIndependentPath
            val resolver =
                ImageDataByUrlLoader(
                    URL(filePathUrl),
                    iconFile.systemIndependentPath,
                    null,
                    false
                )
            resolver.resolve()
            var cachedImageIcon = CachedImageIcon(iconFile.systemIndependentPath, resolver)

            val scaleContext = ScaleContext.create()
            if (cachedImageIcon.scaleContext != scaleContext) {
                // honor scale context as 'iconCache' doesn't do that
                cachedImageIcon = cachedImageIcon.copy()
                cachedImageIcon.updateScaleContext(scaleContext)
            }

            return cachedImageIcon.scaleToWidth(JBUI.pixScale(16.0f))
        } catch (e: Exception) {
            logger.error("Failed to load project icon $iconFile", e)
        }
        return null;
    }*/

    private fun addIcon(hsfIcon: HsfIcon) {
        allIcons.add(hsfIcon)
        iconsByIds[hsfIcon.id] = hsfIcon
        if (hsfIcon.folderOnly)
            folderIcons.add(hsfIcon)
        else {
            folderIcons.add(hsfIcon)
            filesIcons.add(hsfIcon)
        }
    }

    fun getIcon(id: String?): HsfIcon {
        return iconsByIds.getOrDefault(id, None)
    }

    companion object {
        val None = HsfIcon("none", "None", null, false)

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