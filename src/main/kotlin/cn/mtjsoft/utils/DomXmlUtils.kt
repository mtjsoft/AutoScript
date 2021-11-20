package cn.mtjsoft.utils

import org.dom4j.Document
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

object DomXmlUtils {
    fun load(filePath: String): Document? {
        var document: Document? = null
        try {
            val saxReader = SAXReader()
            val inputStream = FileInputStream(filePath)
            document = saxReader.read(inputStream)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return document
    }

    fun updateXml(document: Document, xmlPath: String) {
        val format = OutputFormat.createPrettyPrint()
        format.encoding = "UTF-8"
        var writer: XMLWriter? = null
        try {
            writer = XMLWriter(FileOutputStream(xmlPath), format)
            writer.write(document)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                writer?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}