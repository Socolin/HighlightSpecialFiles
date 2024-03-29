package fr.socolin.rider.plugins.hsf.virtual_folder.node

import com.jetbrains.rider.model.RdProjectModelItemDescriptor
import com.jetbrains.rider.model.RdProjectModelItemLocation

class VirtualFolderItemDescriptor(
    name: String, // Used for alpha sorting
    location: RdProjectModelItemLocation // Used default for sorting
) : RdProjectModelItemDescriptor(name, location) {
}