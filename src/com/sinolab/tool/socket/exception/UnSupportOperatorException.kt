package com.sinolab.tool.socket.exception

import java.lang.Exception

class UnSupportOperatorException(operator:String):Exception("不支持的运算符 $operator")