package com.sinolab.tool.socket.channel

import com.sinolab.tool.socket.listener.MessageListener
import java.io.IOException
import java.net.ServerSocket

class Server(form: MessageListener): SendAndReceiveAbstractObject(form) {

    lateinit var serverSocket:ServerSocket
    override fun open(ip: String, port: Int) {
        thread = Thread {
            try {
                serverSocket = ServerSocket(port)
                socket = serverSocket.accept()
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                form.onReceive(Message("客户端 ${socket!!.inetAddress.hostAddress} 连入成功！"))
            }catch (e:IOException){
                e.printStackTrace()
            }

            while (!thread!!.isInterrupted){
                receive(Message(readInputStream()))
            }
        }
        thread!!.start()
    }

    override fun close() {
        super.close()
        serverSocket.close()
    }
}