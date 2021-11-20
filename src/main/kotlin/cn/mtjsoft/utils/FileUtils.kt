package cn.mtjsoft.utils

import java.io.*
import java.util.*


/**
 * 文件工具
 *
 * @author mtj
 * @date 2021-11-18 16:28:14
 */
object FileUtils {
    fun copy(source: String, dest: String) {
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            inputStream = FileInputStream(source)
            out = FileOutputStream(dest)
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } > 0) {
                out.write(buffer, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun writeStringToFile(filePath: String, saveString: String) {
        try {
            val fw = FileWriter(filePath, true)
            val bw = BufferedWriter(fw)
            bw.write(saveString)
            bw.close()
            fw.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun findAllFileList(path: String): List<File> {
        val list: MutableList<File> = LinkedList()
        try {
            findFileList(File(path), list, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun findFileList(path: String): List<File> {
        val list: MutableList<File> = LinkedList()
        try {
            findFileList(File(path), list, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun findFileNameList(path: String): List<String> {
        val list: MutableList<String> = LinkedList()
        try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) {
                return list
            }
            val filesName = dir.list()
            if (filesName != null) {
                list.addAll(Arrays.asList(*filesName))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun findFileList(dir: File, fileList: MutableList<File>, isAll: Boolean) {
        if (!dir.exists() || !dir.isDirectory) {
            return
        }
        if (dir.isFile) {
            fileList.add(dir)
            return
        }
        val files = dir.list()
        if (files != null) {
            for (s in files) {
                val file = File(dir, s)
                if (file.isFile) {
                    fileList.add(file)
                } else if (isAll) {
                    findFileList(file, fileList, true)
                }
            }
        }
    }
}