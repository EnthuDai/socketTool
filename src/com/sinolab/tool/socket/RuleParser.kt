package com.sinolab.tool.socket

import com.sinolab.tool.socket.exception.ErrorExpression
import com.sinolab.tool.socket.exception.UnSupportOperatorException
import java.lang.StringBuilder
import java.util.*
import java.util.regex.Pattern
import kotlin.math.exp
import kotlin.test.assertTrue

/**
 * 这是一套自定义规则的，字节粒度的 描述字符（类似mock中的数据模板）解析器
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
 * 3、${n}支持的运算符：+(加法)、-(减法)、&(按位或)、|(按位与)、||(并集)。
 * 4、除 ${n}中的n采用10进制外，其他数字为16进制
 * 5、并集符：||，代表运算符左右两侧数据均为预期有效值
 * 6、优先级：() > &、| > +、- > ||；相同优先级，运算顺序为自左向右
 * 7、通配符：*、**，均代表一个任意字节
 * 8、区间符：[a,b] a、b均为16字节字符串，表示a、a+1...b-1、b，等价于（a||a+1...b-1||b）
 *
 * 注意：
 * 按照普遍的按位与或计算法则，对于连续的不加括号的按位与或运算，似乎都只计算第一个与或运算（windows自带计算机、python脚本），
 * 例如直接输入表达式，0xDD|0x56&12，则运算结果位0xDF，也就是0xDD|0x56的结果，如果输入（0xDD|0x56）&0x12 则结果为0x12，
 * 暂时未找到相关的运算规定，表示疑惑。但在本套运算逻辑中 0xDD|0x56&12 等价于（0xDD|0x56）&0x12。
 */


/**
 * 运算符抽象
 */
@ExperimentalUnsignedTypes
interface Operator{
    fun operate(left:UByte, right:UByte):Set<UByte>
}

/**
 * 加法运算符，对应符号 +
 */
class PlusOperator:Operator{
    override fun operate(left: UByte, right: UByte): Set<UByte> {
        return setOf((left + right).toUByte())
    }
}

/**
 * 减法运算符，对应符号 -
 */
class SubtractOperator:Operator{
    override fun operate(left: UByte, right: UByte): Set<UByte> {
        return setOf((left - right).toUByte())
    }
}

/**
 * 与运算符，对应符号 &
 */
class AndOperator:Operator{
    override fun operate(left: UByte, right: UByte): Set<UByte> {
        return setOf(left and right)
    }
}

/**
 * 或运算符，对应符号 |
 */
class OrOperator:Operator{
    override fun operate(left: UByte, right: UByte): Set<UByte> {
        return setOf(left or right)
    }
}

/**
 * 并集运算符，对应符号 ||
 */
class UnionOperator:Operator{
    override fun operate(left: UByte, right: UByte): Set<UByte> {
        return setOf(left, right)
    }

}

class OperatorFactory{
    companion object{
        fun getOperator(operator: String):Operator{
            return when(operator){
                "+" -> PlusOperator()
                "-" -> SubtractOperator()
                "&" -> AndOperator()
                "|" -> OrOperator()
                "||" -> UnionOperator()
                else -> throw UnSupportOperatorException(operator)
            }
        }
    }


}
class RuleParser {
    companion object{
        @Suppress("UNCHECKED_CAST")
        val ALL_UBYTE_SET:Set<UByte> = setOf(0 .. 0xFF) as Set<UByte>

        val SUPPORTED_OPERATOR = arrayOf("+","-","&","|","||")
    }

    /**
     * 将变量 ${n}替换成具体的16进制字符串
     * @param expression 表达式
     * @param instruction 原请求命令
     */
    fun replaceVariable(expression: String, instruction: String):String{
        val regex = "\\$\\{\\d*}"                                                   // ${n} 的正则表达式
        val pattern = Pattern.compile(regex)
        val result = StringBuilder()                                                // 结果字符串
        val instructionBytes = instruction.split("\\s+".toRegex()) // 原命令分解成字节字符串数组
        for(item in expression.split("\\s+".toRegex())){                 // 分割表达式的每个字节
            var matcher = pattern.matcher(item)
            var res = item
            while(matcher.find()){
                res = res.replace(matcher.group(), instructionBytes[getIndex(matcher.group())])     // 替换变量
                matcher = matcher.reset(res)                                                        // 从头开始重新匹配变量
            }
            result.append(res).append(" ")
        }
        return result.toString().trim()
    }

    /**
     * 将字符串中的[a,b]等价替换为（a||a+1||a+2...b-1||b）
     */
    fun replaceIntervalOperator(expression: String):String{
        var index = 0
        val result = StringBuilder()
        while (index<expression.length){
            if(expression[index] == '['){
                result.append("(")
                val endIndex = handlePairChar(expression, index, '[', ']')
                val numberPair = expression.substring(index + 1, endIndex).split(",")
                if(numberPair.size !=2){
                    throw ErrorExpression("[" + expression.substring(index + 1, endIndex) +"]")
                }else{
                    for(num in Hex2.hexStringToUByte(numberPair[0]) until Hex2.hexStringToUByte(numberPair[1])){
                        result.append(Hex2.ubyteToHexString(num.toUByte()))
                        result.append("||")
                    }
                    result.append(numberPair[1])
                }
                index = endIndex + 1
            }else{
                result.append(expression[index])
                index += 1
            }
        }
        return result.toString()
    }

    /**
     *  由${n}获取n
     */
    private fun getIndex(string:String):Int{
        return string.substring(2, string.length - 1).toInt()
    }


    /**
     * 解析单个字节表达式
     * 基本思路：
     * 按照运算符的优先级，从优先级最高的运算符算起，遇到括号则递归括号中的内容
     * 计算时，数字字符串整体入栈，操作符入栈、非操作符入栈前检查栈首元素是否为操作符，进行出栈计算将结果入栈或直接入栈
     * 直至所有元素出栈
     * @param byteExpression 字节表达式
     */
    fun parse(byteExpression:String):Set<UByte>{
        if(byteExpression == "*" || byteExpression == "**") return ALL_UBYTE_SET    // 识别通配符
        var expression = byteExpression
        expression = calculate(expression, arrayOf("&", "|"))
        expression = calculate(expression, arrayOf("+", "-"))
        return calculateUnionOperator(expression)
    }

    /**
     * 输入“（”的下标，返回与之匹配的“）”的下标
     * @param expression 表达式
     * @param beginIndex 左括号在表达式中的下标
     * @return 对应右括号的下标
     */
    private fun handleBracket(expression: String,beginIndex: Int):Int{
        return handlePairChar(expression, beginIndex, '(', ')')
//        var endIndex = beginIndex + 1
//        val bracketStack = Stack<Char>();           // 括号栈
//        bracketStack.push('(')
//        while (endIndex < expression.length && bracketStack.isNotEmpty()){
//            if(expression[endIndex] == '(') bracketStack.push('(')
//            if(expression[endIndex] == ')') bracketStack.pop()
//            endIndex++
//        }
//        if(bracketStack.isEmpty()){
//            return endIndex-1
//        }else{
//            throw ErrorExpression("$expression，括号不匹配！")
//        }
    }

    /**
     * 选择匹配字符对，例如寻找左括号对应的右括号
     * @param expression 所在字符串
     * @param beginIndex 需要配对的left字符所在的下标
     * @param left 字符串对的左边字符
     * @param right 字符串对的右字符
     */
    private fun handlePairChar(expression: String, beginIndex:Int, left:Char, right:Char):Int{
        var endIndex = beginIndex + 1
        val bracketStack = Stack<Char>();           // 括号栈
        bracketStack.push(left)
        while (endIndex < expression.length && bracketStack.isNotEmpty()){
            if(expression[endIndex] == left) bracketStack.push(left)
            if(expression[endIndex] == right) bracketStack.pop()
            endIndex++
        }
        if(bracketStack.isEmpty()){
            return endIndex-1
        }else{
            throw ErrorExpression("$expression，$left 和 $right 不匹配！")
        }
    }

    /**
     * 取出set中的一个值
     */
     fun oneOf(set:Set<UByte>): UByte {
        assertTrue(set.isNotEmpty())
        for(item in set){
            return item
        }
        return 0u
    }

    /**
     * 判断字符是否是16进制字符
     */
    private fun isHexChar(char: Char): Boolean {
        if(char in '0'..'9') return true
        if(char in 'a'..'f') return true
        if(char in 'A'..'F') return true
        return false
    }

    private fun calculate(expression: String, operators:Array<String>):String {
        var index = 0
        val stack = Stack<String>()
        while(index<expression.length){
            var endIndex:Int = index
            // 对括号内的表达式递归运算
            if(expression[index] == '('){
                endIndex = handleBracket(expression, index)
                val tempResult = parse(expression.substring(index+1, endIndex))
                if(tempResult.size != 1)
                    throw ErrorExpression("$expression，不支持集合类型的中间结果！")
                else{
                    if(stack.isEmpty()) stack.push(Hex.byteToHexStr(oneOf(tempResult).toInt()))
                    else if(stack.peek() in operators){
                        val operator = OperatorFactory.getOperator(stack.pop())             // 操作符出栈
                        if(stack.isEmpty()) throw ErrorExpression(expression)
                        val temp = oneOf(operator.operate(stack.pop().toInt(16).toUByte(), oneOf(tempResult))) // 操作数出栈并运算
                        stack.push(Hex2.ubyteToHexString(temp))
                    }else{
                        stack.push(Hex2.ubyteToHexString(oneOf(tempResult)))
                    }
                }
                index = endIndex+1
            }
            // 16进制数字字符串入栈
            else if(isHexChar(expression[index])){
                while ( ++endIndex < expression.length  && isHexChar(expression[endIndex]));
                val numStr = expression.substring(index, endIndex)
                // 1、空栈则进栈
                // 2、非空栈则检查运算符是否为本轮运算的运算符，是则出栈运算，否则进栈
                if(stack.isEmpty()){
                    stack.push(numStr)
                }else{
                    var top = stack.peek()
                    if(top in operators){
                        top = stack.pop()
                        val operator = OperatorFactory.getOperator(top)
                        if(stack.isEmpty()) throw ErrorExpression(expression)
                        else{
                            top = stack.pop()
                            val value = operator.operate(Hex2.hexStringToUByte(top), Hex2.hexStringToUByte(numStr))
                            stack.push(Hex2.ubyteToHexString(oneOf(value)))
                        }
                    }else{
                        stack.push(numStr)
                    }
                }
                index = endIndex
            }
            // 操作符入栈
            else if(expression[index].toString() in SUPPORTED_OPERATOR) {
                if(expression[index] == '|' && expression[index+1] == '|'){
                    stack.push("||")
                    index+=2
                }else{
                    stack.push(expression[index].toString())
                    index+=1
                }
            }else{
                throw UnSupportOperatorException(expression[index].toString())
            }
        }
        // 栈还原成表达式字符串进行下一轮计算
        var result = ""
        while (stack.isNotEmpty()){
            result = stack.pop() + result
        }
        return result
    }

    /**
     * 处理并集 || 符号
     */
    private fun calculateUnionOperator(expression: String):Set<UByte>{
        var index = 0
        val result = mutableSetOf<UByte>()
        val stack = Stack<String>()
        while(index<expression.length){
            var endIndex:Int = index
            // 对括号内的表达式递归运算
            if(expression[index] == '('){
                endIndex = handleBracket(expression, index)
                val tempResult = calculateUnionOperator(expression.substring(index+1, endIndex))
                result.addAll(tempResult)
                index = endIndex+1
            }
            // 16进制数字字符串入栈
            else if(isHexChar(expression[index])) {
                while (++endIndex < expression.length && isHexChar(expression[endIndex]));
                val numStr = expression.substring(index, endIndex)
                // 1、空栈则进栈
                // 2、非空栈则检查运算符是否为本轮运算的运算符，是则出栈运算，否则进栈
                result.add(Hex2.hexStringToUByte(numStr))
                index = endIndex

            }
            // 操作符入栈
            else if(expression[index] == '|' && expression[index+1] == '|'){
                    index+=2
            }else{
                throw UnSupportOperatorException(expression[index].toString())
            }
        }

        return result
    }

}