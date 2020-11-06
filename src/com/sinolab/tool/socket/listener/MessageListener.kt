package com.sinolab.tool.socket.listener

import com.sinolab.tool.socket.channel.Message


interface MessageListener {

    fun onReceive(message: Message)

}