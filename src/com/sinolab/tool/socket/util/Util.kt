package com.sinolab.tool.socket.util

import java.lang.Exception

object Util {

    /**
     * 数字字符串数组转Int数组
     * @param str 原字符串
     * @param splitRegex 字符串中的分隔符
     */
    fun stringToIntArray(str:String, splitRegex:Regex):ArrayList<Int>{
        val itemList = str.split(splitRegex)
        val arrayList = ArrayList<Int>(itemList.size)
        for(item in itemList){
            if(item.isNotBlank()){
                try{
                    arrayList.add(item.trim().toInt())
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

        }
        return arrayList
    }

}