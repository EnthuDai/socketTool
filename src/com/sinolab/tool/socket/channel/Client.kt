package com.sinolab.tool.socket.channel

import com.sinolab.tool.socket.exception.AlertException
import com.sinolab.tool.socket.listener.MessageListener
import java.net.Socket
import java.util.logging.Logger

class Client(form: MessageListener): SendAndReceiveAbstractObject(form) {
    private val logger = Logger.getLogger(this.javaClass.name)


    var port:Int = 0
    var ip:String = ""



    override fun open(ip: String, port: Int){
        thread = Thread{
            try {
                this.socket = Socket(ip, port)
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                form.onReceive(Message("服务端连接成功!"))
            }catch (e: Exception){
                e.printStackTrace()
                throw AlertException("连接失败")
            }
            while (!thread!!.isInterrupted ){
                receive(Message(readInputStream()))
            }
            logger.info("接收线程终止！")
        }
        thread!!.start()

    }


}