package com.sinolab.tool.socket

/**
 * 界面配置
 */
class Config {
    var instructionTab: List<TabConfig> = listOf()
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