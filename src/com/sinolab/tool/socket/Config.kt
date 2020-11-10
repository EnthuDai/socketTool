package com.sinolab.tool.socket

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileWriter


/**
 * 界面配置
 */
class Config {
    var doc:String = ""
    var instructionTabDesc:String = ""
    var instructionTab: List<TabConfig> = listOf()
    // 将配置信息同步到文件
    fun syncToFile(){
        val file = File("config.json")
        val fw = FileWriter(file)
        val gson = GsonBuilder()
                .setPrettyPrinting()
                .create()
        fw.write(gson.toJson(this))
        fw.flush()
        fw.close()
    }
}

class TabConfig{
    var title:String = ""
    var rows:List<RowConfig> = listOf()

    fun toArray(): Array<Array<String>> {
        return Array(rows.size){ index ->
            arrayOf(index.toString(), rows[index].name, rows[index].sampleInstruction)
        }
    }
}
class RowConfig{
    var name:String = ""
    var sampleInstruction = ""
}