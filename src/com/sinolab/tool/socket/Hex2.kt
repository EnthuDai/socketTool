package com.sinolab.tool.socket


object Hex2 {
    fun ubyteToHexString(uByte: UByte):String{
        return String.format("%02X", uByte.toInt())
    }

    fun hexStringToUByte(str:String):UByte{
        return str.toInt(16).toUByte()
    }
}