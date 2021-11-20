package cn.mtjsoft.view

import cn.mtjsoft.AutoScriptWindow
import cn.mtjsoft.utils.DomXmlUtils
import cn.mtjsoft.utils.FileUtils
import org.dom4j.Document
import org.dom4j.Element
import java.awt.Toolkit
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import javax.swing.JFrame
import javax.swing.JOptionPane
import kotlin.system.exitProcess

class AutoWindow : JFrame() {

    private lateinit var autoScriptWindow: AutoScriptWindow

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    init {
        title = "资源文件替换脚本"
        iconImage = Toolkit.getDefaultToolkit().createImage(this.javaClass.getResource("/image/icon72.png"))
    }

    fun showWindow() {
        isResizable = true
        contentPane.add(AutoScriptWindow().apply {
            autoScriptWindow = this
            ok.addActionListener {
                okClick()
            }
        }.root)
        setSize(500, 600)
        setLocationRelativeTo(null)
        isVisible = true
        addWindowCloseListener(this)
    }

    /**
     * 点击确认
     */
    private fun okClick() {
        autoScriptWindow.apply {
            val rp = rp.text
            if (rp.isNullOrEmpty()) {
                JOptionPane.showMessageDialog(null, "请输入待替换资源文件夹.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            val rpFileNames = FileUtils.findFileNameList(rp)
            if (rpFileNames.isEmpty()) {
                JOptionPane.showMessageDialog(null, "待替换资源文件夹为空.", "错误", JOptionPane.ERROR_MESSAGE)
                return
            }
            val pp = pp.text
            if (pp.isNullOrEmpty()) {
                JOptionPane.showMessageDialog(null, "请输入目标项目目录.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            val ppFileNames = FileUtils.findFileNameList(pp)
            if (ppFileNames.isEmpty()) {
                JOptionPane.showMessageDialog(null, "目标项目目录为空.", "错误", JOptionPane.ERROR_MESSAGE)
                return
            }
            result.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date()) + "\n"
            printResult("=====================================")
            val debug = debug.isSelected
            val alpha = alpha.isSelected
            val release = release.isSelected
            val res = "/app/src/main/res"
            val resFileNames = FileUtils.findFileNameList(rp)
            // 资源替换
            for (name in resFileNames) {
                if (name.startsWith("drawable") || name.startsWith("mipmap") || name.startsWith("anim") || name.startsWith(
                        "color"
                    ) || name.startsWith("raw")
                ) {
                    testRpDrawable(rp, pp + res, name)
                    continue
                }
                if (name.startsWith("values") || name.startsWith("xml")) {
                    testRpXml(rp, pp + res, name)
                }
            }
            // 自动打包
            if (release) {
                cmd("cd /d $pp && gradlew :app:assembleRelease", "gradlew :app:assembleRelease")
            }
            if (alpha) {
                cmd("cd /d $pp && gradlew :app:assembleAlpha", "gradlew :app:assembleAlpha")
            }
            if (debug) {
                cmd("cd /d $pp && gradlew :app:assembleDebug", "gradlew :app:assembleDebug")
            }
        }
    }

    /**
     * 测试 资源文件 直接替换
     */
    private fun testRpDrawable(resPath: String, projectPath: String, name: String) {
        singleThreadExecutor.execute {
            printResult("=====================================")
            printResult("$name >>> 开始")
            val resFiles = FileUtils.findFileList(resPath + File.separator + name)
            for (file in resFiles) {
                val targetFile = File(projectPath + File.separator + name + File.separator + file.name)
                if (targetFile.exists() && targetFile.delete()) {
                    FileUtils.copy(file.absolutePath, targetFile.absolutePath)
                    printResult("$name >>> copy 【" + file.name + "】")
                }
            }
            printResult("$name >>> 结束")
            printResult("=====================================")
        }
    }

    /**
     * 测试 XML文件
     */
    private fun testRpXml(resPath: String, projectPath: String, name: String) {
        singleThreadExecutor.execute {
            val resFiles = FileUtils.findFileList(resPath + File.separator + name)
            for (file in resFiles) {
                if (!file.name.endsWith("xml")) {
                    continue
                }

                val targetFile = File(projectPath + File.separator + name + File.separator + file.name)
                if (targetFile.exists()) {
                    val resDocument: Document? = DomXmlUtils.load(file.absolutePath)
                    val targetDocument: Document? = DomXmlUtils.load(targetFile.absolutePath)
                    if (resDocument != null && targetDocument != null) {
                        printResult("=====================================")
                        printResult("${file.name} >>> 开始")
                        val resElements = resDocument.rootElement.elements()
                        val targetElements = targetDocument.rootElement.elements()
                        var changeCount = 0
                        for (element in resElements) {
                            val attrName = element.attributeValue("name")
                            for (i in targetElements.indices) {
                                val targetElement = targetElements[i]
                                val attrName2 = targetElement.attributeValue("name")
                                if (attrName == attrName2) {
                                    if (targetElement.parent.remove(targetElement)) {
                                        targetElements.removeAt(i)
                                        targetElements.add(i, element.clone() as Element)
                                        changeCount++
                                        printResult("update >>> ${targetElement.qName.name} >>> $attrName2")
                                    }
                                    break
                                }
                            }
                        }
                        if (changeCount > 0) {
                            DomXmlUtils.updateXml(targetDocument, targetFile.absolutePath)
                        }
                        printResult("${file.name} >>> 结束")
                        printResult("=====================================")
                    }
                }
            }
        }
    }

    private fun cmd(stam: String, s: String) {
        singleThreadExecutor.execute {
            printResult("自动打包开始 \n >>> $s")
            try {
                val processBuilder = ProcessBuilder().command("cmd.exe", "/c", stam)
                val process: Process = processBuilder.start()
                val bufferedReader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
                //循环等待进程输出，判断进程存活则循环获取输出流数据
                while (process.isAlive) {
                    while (bufferedReader.ready()) {
                        val log = bufferedReader.readLine()
                        printResult(log)
                    }
                }
                //获取执行结果
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
                printResult("自动打包错误 \n >>> $s \n" + e.message)
            } finally {
                printResult("自动打包结束")
            }
        }
    }

    /**
     * 输出结果
     */
    private fun printResult(s: String, isLine: Boolean = true) {
        autoScriptWindow.result.apply {
            append(s)
            if (isLine) {
                append("\n")
            }
        }
        // 处理自动滚动到最底部
        autoScriptWindow.result.caretPosition = autoScriptWindow.result.text.length
    }

    /**
     * 处理关闭
     */
    private fun addWindowCloseListener(jFrame: JFrame) {
        jFrame.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        jFrame.addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent?) {
            }

            override fun windowClosing(e: WindowEvent) {
                val option = JOptionPane.showConfirmDialog(
                    jFrame, "确定退出吗?", "提示",
                    JOptionPane.YES_NO_OPTION
                )
                if (option == JOptionPane.YES_OPTION) {
                    if (e.window === jFrame) {
                        jFrame.dispose()
                        exitProcess(0)
                    } else {
                        return
                    }
                } else if (option == JOptionPane.NO_OPTION) {
                    if (e.window === jFrame) {
                        return
                    }
                }
            }

            override fun windowClosed(e: WindowEvent?) {
            }

            override fun windowIconified(e: WindowEvent?) {
            }

            override fun windowDeiconified(e: WindowEvent?) {
            }

            override fun windowActivated(e: WindowEvent?) {
            }

            override fun windowDeactivated(e: WindowEvent?) {
            }
        })
    }

}