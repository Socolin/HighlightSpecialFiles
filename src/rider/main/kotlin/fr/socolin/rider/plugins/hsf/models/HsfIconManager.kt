package fr.socolin.rider.plugins.hsf.models

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader

class HsfIconManager {
    private val icons: ArrayList<HsfIcon> = ArrayList()
    private val iconsByIds: HashMap<String, HsfIcon> = HashMap()

    init {
        icons.add(None)
        addDefaultIcons()
    }

    private fun addDefaultIcons() {
        addDefaultIcon(
            "hsf.defaults.specflow",
            "Specflow",
            "/icons/specflow.svg",
        )
        addDefaultIcon(
            "hsf.defaults.kubernetes",
            "Kubernetes",
            "/icons/kubernetes-icon-color.svg",
        )
    }

    private fun addDefaultIcon(id: String, name: String, svgFilePath: String) {
        val icon = HsfIcon(id, name, IconLoader.getIcon(svgFilePath, HsfIconManager::class.java))
        addIcon(icon)
    }

    private fun addIcon(icon: HsfIcon) {
        icons.add(icon)
        iconsByIds[icon.id] = icon
    }

    fun getIcon(id: String?): HsfIcon {
        return iconsByIds.getOrDefault(id, None)
    }

    fun getIcons(): List<HsfIcon> {
        return icons
    }

    companion object {
        val None = HsfIcon("none", "None", null)

        fun getInstance(project: Project): HsfIconManager {
            return project.getService(HsfIconManager::class.java)
        }
    }
}