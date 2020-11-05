package com.sinolab.tool.socket



interface MessageListener {

    fun onReceive(message: Message)

}