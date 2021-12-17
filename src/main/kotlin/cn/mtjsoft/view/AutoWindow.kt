package cn.mtjsoft.view

import cn.mtjsoft.AutoScriptWindow
import cn.mtjsoft.utils.DomXmlUtils
import cn.mtjsoft.utils.FileUtils
import org.dom4j.Document
import org.dom4j.Element
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JTextField
import kotlin.system.exitProcess

class AutoWindow : JFrame() {

    private lateinit var autoScriptWindow: AutoScriptWindow

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    private val icon: Image

    private var autoBuildProgressNUmber = 10

    private var rpPath = ""
    private var ppPath = ""

    init {
        title = "资源替换自动打包工具v1.0.1"
        icon = Toolkit.getDefaultToolkit().createImage(this.javaClass.getResource("/image/icon72.png"))
        iconImage = icon
    }

    fun showWindow(
        resPath: String?,
        projectPath: String?,
        debugOpen: Boolean?,
        alphaOpen: Boolean?,
        releaseOpen: Boolean?
    ) {
        isResizable = true
        contentPane.add(AutoScriptWindow().apply {
            autoScriptWindow = this
            rpBtn.addActionListener {
                chooseFile(rp)
            }
            ppBtn.addActionListener {
                chooseFile(pp)
            }
            ok.addActionListener {
                okClick()
            }
            progressBar.isVisible = false
        }.root)
        setSize(500, 600)
        setLocationRelativeTo(null)
        isVisible = true
        addWindowCloseListener(this)
        autoScriptWindow.apply {
            if (!resPath.isNullOrEmpty()) {
                rp.text = resPath
            }
            if (!projectPath.isNullOrEmpty()) {
                pp.text = projectPath
            }
            debug.isSelected = debugOpen != null && debugOpen
            alpha.isSelected = alphaOpen != null && alphaOpen
            release.isSelected = releaseOpen != null && releaseOpen
        }
    }

    /**
     * 点击确认
     */
    private fun okClick() {
        autoScriptWindow.apply {
            rpPath = rp.text
            if (rpPath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "请输入待替换资源文件夹.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            val rpFileNames = FileUtils.findFileNameList(rpPath)
            if (rpFileNames.isEmpty()) {
                JOptionPane.showMessageDialog(null, "待替换资源文件夹为空.", "错误", JOptionPane.ERROR_MESSAGE)
                return
            }
            ppPath = pp.text
            if (ppPath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "请输入目标项目目录.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            val ppFileNames = FileUtils.findFileNameList(ppPath)
            if (ppFileNames.isEmpty()) {
                JOptionPane.showMessageDialog(null, "目标项目目录为空.", "错误", JOptionPane.ERROR_MESSAGE)
                return
            }
            // 进度条未走完
            if (progressBar.isVisible && progressBar.maximum > 0 && progressBar.value > 0 && progressBar.value < progressBar.maximum) {
                JOptionPane.showMessageDialog(null, "当前任务还未结束，请稍后.", "提示", JOptionPane.WARNING_MESSAGE)
                return
            }
            result.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date()) + "\n"
            val debug = debug.isSelected
            val alpha = alpha.isSelected
            val release = release.isSelected
            val res = "/app/src/main/res"
            // 获取总的需要替换的文件个数
            setProgressBar(rpPath)
            printResult("=====================================")
            val resFileNames = FileUtils.findFileNameList(rpPath)
            // 资源替换
            for (name in resFileNames) {
                if (name.startsWith("drawable") || name.startsWith("mipmap") || name.startsWith("anim") || name.startsWith(
                        "color"
                    ) || name.startsWith("raw")
                ) {
                    testRpDrawable(rpPath, ppPath + res, name)
                    continue
                }
                if (name.startsWith("values") || name.startsWith("xml")) {
                    testRpXml(rpPath, ppPath + res, name)
                }
            }
            // 自动打包
            if (debug) {
                cmd("cd /d $ppPath && gradlew :clean && gradlew :app:assembleDebug", "gradlew :app:assembleDebug")
            }
            if (alpha) {
                cmd("cd /d $ppPath && gradlew :clean && gradlew :app:assembleAlpha", "gradlew :app:assembleAlpha")
            }
            if (release) {
                cmd("cd /d $ppPath && gradlew :clean && gradlew :app:assembleRelease", "gradlew :app:assembleRelease")
            }
        }
    }

    /**
     * 初始化进度条
     */
    private fun setProgressBar(rp: String) {
        singleThreadExecutor.execute {
            var size = FileUtils.findAllFileList(rp).size
            // 计算每一个自动打包，算资源替换的10%，默认10
            val number = size * 0.1
            autoBuildProgressNUmber = if (number < 10) {
                10
            } else {
                number.toInt()
            }
            autoScriptWindow.apply {
                if (debug.isSelected) {
                    size += autoBuildProgressNUmber
                }
                if (alpha.isSelected) {
                    size += autoBuildProgressNUmber
                }
                if (release.isSelected) {
                    size += autoBuildProgressNUmber
                }
                progressBar.isVisible = size > 0
                progressBar.value = 0
                progressBar.minimum = 0
                progressBar.maximum = size
            }
        }
    }

    /**
     * 每循环一个文件，进度 + 1
     */
    private fun updateProgressBar(addNUm: Int = 1, isOver: Boolean = false) {
        var progress = autoScriptWindow.progressBar.value + addNUm
        if (progress > autoScriptWindow.progressBar.maximum || isOver) {
            progress = autoScriptWindow.progressBar.maximum
        }
        autoScriptWindow.progressBar.value = progress
        if (progress >= autoScriptWindow.progressBar.maximum) {
            // 完成时，将完整日志保存至，资源文件目录
            singleThreadExecutor.execute {
                val name = SimpleDateFormat("yyyyMMdd_HH_mm_ss_sss", Locale.CHINESE).format(Date())
                FileUtils.writeStringToFile(rpPath + File.separator + "$name.txt", autoScriptWindow.result.text)
            }
        }
    }

    /**
     * 选择文件
     */
    private fun chooseFile(jTextField: JTextField) {
        val jFrame = JFrame()
        jFrame.iconImage = icon
        val jfc = JFileChooser()
        jfc.dialogTitle = "选择文件夹"
        jfc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val returnVal = jfc.showOpenDialog(jFrame)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            val file = jfc.selectedFile
            file?.let {
                if (it.isDirectory) {
                    jTextField.text = it.absolutePath
                }
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
                    //
                    moveFile(
                        file.absolutePath,
                        resPath + File.separator + "replaceFiles/" + name + File.separator + file.name,
                        true
                    )
                }
                updateProgressBar()
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
                if (file.name.endsWith("xml")) {
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
                                //
                                moveFile(
                                    file.absolutePath,
                                    resPath + File.separator + "replaceFiles/" + name + File.separator + file.name,
                                    true
                                )
                            }
                            printResult("${file.name} >>> 结束")
                            printResult("=====================================")
                        }
                    }
                }
                updateProgressBar()
            }
        }
    }

    private fun cmd(stam: String, s: String) {
        singleThreadExecutor.execute {
            printResult("自动打包开始 \n >>> $s")
            var addCount = 0
            var readLineNumber = 0
            try {
                val processBuilder = ProcessBuilder().command("cmd.exe", "/c", stam)
                val process: Process = processBuilder.start()
                val bufferedReader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
                //循环等待进程输出，判断进程存活则循环获取输出流数据
                while (process.isAlive) {
                    while (bufferedReader.ready()) {
                        printResult(bufferedReader.readLine())
                        readLineNumber++
                        // 这里为了更新进度条，先规定每15行日志输出，就更新一下，最大到 autoBuildProgressNUmber - 1 ，留一个在结束时设置
                        if (readLineNumber % 20 == 0 && addCount < autoBuildProgressNUmber - 1) {
                            addCount++
                            updateProgressBar()
                        }
                    }
                }
                //获取执行结果
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
                printResult(">>> " + e.message)
                printResult("自动打包错误 >>> $s")
            } finally {
                printResult("自动打包结束 >>> $s")
                updateProgressBar(autoBuildProgressNUmber - addCount)
            }
        }
    }

    private fun moveFile(source: String, dest: String, isDeleteSource: Boolean = true) {
        val file = File(dest)
        if (!file.parentFile.exists() && !file.parentFile.mkdirs()) {
            return
        }
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    FileUtils.copy(source, dest)
                    if (isDeleteSource) {
                        // 删除源文件
                        File(source).delete()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
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