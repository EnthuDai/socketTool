package com.sinolab.tool.socket

import com.sinolab.tool.socket.exception.UnSupportOperatorException

/**
 * 这是一套自定义规则的，字节粒度的描述字符（类似mock中的数据模板）解析器
 * 在做socket通信时，绝大多数场景下都为 一发一收 的数据传输方式
 * 例如： server发送： 0x71 0x56，根据规定的协议服务端接收到后需要固定返回：0x71 0x57 作为响应
 * 这是一个简单的例子，真实条件下往往要稍复杂一些，比如，响应帧中的某个字节需要跟发送帧对应位置上的字节有关，而不是固定
 * 的某个值。假设有这样一个协议：server 发送：0x71 0x56，接收方响应的报文中，每个字节都是发送报文中对应字节 +1 后的值，
 * 即0x71 + 1, 0x56 + 1，也就是 0x72，0x57。
 * 本类定义了一套用于描述响应内容与发送内容关系的规则系统，用于对设备进行自动化测试。
 * 规则如下：
 *
 * 发送帧(16进制表示)：0x01 0x02 0x03 0x04 0x05 ...
 *
 * 描述规则：
 * 1、具体的16进制数据，即表示此位为此固定值
 * 2、${n} 表示发送帧中下标为 n 的字节，例如 ${0} 代指 0x01
 * 3、${n}支持的运算符：+、-、&、|
 * 4、除 ${n}中的n采用10进制外，其他数字默认为16进制
 * 5、|| 运算符，代表运算符左右两侧数据均为预期有效值
 *
 */


/**
 * 运算符抽象
 */
@ExperimentalUnsignedTypes
interface Operator{
    fun operate(left:UByte, right:UByte):UByteArray
}

/**
 * 加法运算符，对应符号 +
 */
class PlusOperator:Operator{
    override fun operate(left: UByte, right: UByte): UByteArray {
        return ubyteArrayOf((left + right).toUByte())
    }
}

/**
 * 减法运算符，对应符号 -
 */
class SubtractOperator:Operator{
    override fun operate(left: UByte, right: UByte): UByteArray {
        return ubyteArrayOf((left - right).toUByte())
    }
}

/**
 * 与运算符，对应符号 &
 */
class AndOperator:Operator{
    override fun operate(left: UByte, right: UByte): UByteArray {
        return ubyteArrayOf(left and right)
    }
}

/**
 * 或运算符，对应符号 |
 */
class OrOperator:Operator{
    override fun operate(left: UByte, right: UByte): UByteArray {
        return ubyteArrayOf(left or right)
    }
}

/**
 * 并集运算符，对应符号 ||
 */
class UnionOperator:Operator{
    override fun operate(left: UByte, right: UByte): UByteArray {
        return ubyteArrayOf(left, right)
    }

}

class OperatorFactory{
    fun getOperator(operator: String){
        when(operator){
            "+" -> PlusOperator()
            "-" -> SubtractOperator()
            "&" -> AndOperator()
            "|" -> OrOperator()
            "||" -> UnionOperator()
            else -> throw UnSupportOperatorException(operator)
        }
    }
}
class RuleParser {

}