package com.sinolab.tool.socket.exception

import java.lang.Exception

class AlertException(val info:Any):Exception(info.toString()){
    
}