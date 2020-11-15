package com.sinolab.tool.socket.exception

import java.lang.Exception

class AlertException(val info:Any):Exception(info.toString())

class UnSupportOperatorException(operator:String):Exception("不支持的运算符 $operator")

class ErrorExpression(expression:String):Exception("错误的表达式 $expression")