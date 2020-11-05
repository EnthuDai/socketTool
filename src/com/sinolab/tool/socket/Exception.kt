package com.sinolab.tool.socket

import java.lang.Exception

class AlertException(val info:Any):Exception(info.toString()){
    
}