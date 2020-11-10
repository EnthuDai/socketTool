package com.sinolab.tool.socket

import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.util.logging.Logger
import javax.swing.JFrame
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger = Logger.getLogger("main")
        val frame = JFrame("SOCKET调试工具")
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: UnsupportedLookAndFeelException) {
            e.printStackTrace()
        }
        var config:Config? = null
        val file = File("config.json")
        if (file.exists()) {
            logger.info("找到配置文件，文件路径：" + file.absolutePath)
            config = Gson().fromJson(FileReader(file), Config::class.java)
        } else {
            logger.warning("未在同目录下找到名为config.json的配置文件！")
            logger.warning("当前目录为: " + file.absolutePath)
        }
        frame.contentPane = MainForm(frame,config).panel1
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()
        frame.isVisible = true
        frame.setLocationRelativeTo(null)
    }
}