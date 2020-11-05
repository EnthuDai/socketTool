package com.sinolab.tool.socket

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.logging.Logger

/**
 * 这是统一客户端和服务端的收发功能抽象类
 * ps:命名太难了
 */
abstract class SendAndReceiveAbstractObject {
    private val logger = Logger.getLogger(this.javaClass.name)

    /**
     * 界面
     */
    lateinit var form: MessageListener

    var socket: Socket? = null

     var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    var thread:Thread? = null

    constructor(form: MessageListener) {
        this.form = form
    }

    /**
     * 发送
     */
    fun send(byteArray: ByteArray){
        if(socket!=null && !socket!!.isClosed){
            outputStream?.write(byteArray)
            outputStream?.flush()
        }else{
            if(socket == null){
                logger.warning("无法发送 ${Hex.bytesToHexString(byteArray," ",false)}，原因：socket连接未建立！")
            }else{
                logger.warning("无法发送 ${Hex.bytesToHexString(byteArray," ",false)}，原因：socket连接已关闭！")
            }

        }
    }

    /**
     * 接收到消息后通知界面
     */
    fun receive(message: Message) {
        form.onReceive(message)
    }

    abstract fun open(ip: String, port: Int)
    open fun close(){
        thread?.interrupt()
        this.outputStream?.close()
        this.inputStream?.close()
        this.socket?.close()

    }

    var cache = ByteArray(100)
    fun readInputStream(): ByteArray {
//        Thread.sleep(1000)
        val readNum = inputStream?.read(cache)
        return cache.copyOf(readNum?:0)
    }
}

class Message {
    var isBytes: Boolean = true
    lateinit var bytes: ByteArray
    lateinit var dataStr: String

    constructor(byteArray: ByteArray) {
        this.isBytes = true
        this.bytes = byteArray
    }

    constructor(dataStr: String) {
        this.isBytes = false
        this.dataStr = dataStr
    }

    override fun toString(): String {
        if (isBytes) {
            return Hex.bytesToHexString(bytes, " ", false)
        } else {
            return dataStr
        }
    }
}