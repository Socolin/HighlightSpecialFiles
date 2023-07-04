package fr.socolin.rider.plugins.hsf.virtual_folder.file

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.ex.dummy.DummyFileSystem
import fr.socolin.rider.plugins.hsf.models.HsfHighlightingRule
import java.io.InputStream
import java.io.OutputStream

class VirtualFolderVirtualFile(
    private val parent: VirtualFile,
    private val rule: HsfHighlightingRule,
) : VirtualFile() {
    override fun getName(): String {
        return rule.virtualFolderName ?: "<No Virtual Folder Name>";
    }

    override fun getFileSystem(): VirtualFileSystem {
        return DummyFileSystem.getInstance()
    }

    override fun getPath(): String {
        return "virtual_path¨"
    }

    override fun isWritable(): Boolean {
        return false;
    }

    override fun isDirectory(): Boolean {
        return true;
    }

    override fun isValid(): Boolean {
        return true;
    }

    override fun getParent(): VirtualFile {
        return this.parent;
    }

    override fun getChildren(): Array<VirtualFile> {
        return emptyArray()
    }

    override fun getOutputStream(p0: Any?, p1: Long, p2: Long): OutputStream {
        return OutputStream.nullOutputStream()
    }

    override fun contentsToByteArray(): ByteArray {
        return ByteArray(0)
    }

    override fun getTimeStamp(): Long {
        return 0
    }

    override fun getLength(): Long {
        return 0
    }

    override fun refresh(p0: Boolean, p1: Boolean, p2: Runnable?) {
    }

    override fun getInputStream(): InputStream {
        return InputStream.nullInputStream();
    }

}