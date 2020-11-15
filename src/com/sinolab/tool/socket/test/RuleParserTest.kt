package com.sinolab.tool.socket.test

import com.sinolab.tool.socket.RuleParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class RuleParserTest{

    companion object{
        fun compareSet(set: Set<UByte>, set2: Set<UByte>): Boolean {
            if(set == set2) return true
            if(set.size != set2.size) return false
            val ite1: Iterator<*> = set.iterator()
            val ite2: Iterator<*> = set2.iterator()

            var isFullEqual = true

            while (ite2.hasNext()) {
                if (!set.contains(ite2.next())) {
                    isFullEqual = false
                }
            }
            return isFullEqual

        }
    }
    val rp = RuleParser()

    @Test
    fun testReplaceVariable(){
        val exp = "${'$'}{1}+(1+2)|8 (3&1)-1||${'$'}{0} ${'$'}{1}|${'$'}{0}"
        val instruction = "71 56 11 22 33 44 55 66 77 88 99 10"
        val result = "56+(1+2)|8 (3&1)-1||71 56|71"
        assertEquals(result, rp.replaceVariable(exp, instruction))
    }

    @Test
    fun testParser(){
        val ruleParser = RuleParser()

        val case4 = "FF||11||22||33"
        assert(compareSet(setOf(0xFFu, 0x11u,0x22u,0x33u), ruleParser.parse(case4)))

        val case3 = "FF-1+2+(12-2)"
        assertEquals(1, ruleParser.parse(case3).size)
        assertEquals(0x10, ruleParser.oneOf(ruleParser.parse(case3)).toInt())

        val case2 = "(34)|(55&(12|AC|BB&(DD|56&12)))" // 0x34 | (0x55&(0x12|0xAC|0xBB&(0xDD|0x56&0x12)))
        assertEquals(1, ruleParser.parse(case2).size)
        assertEquals(0x34, ruleParser.oneOf(ruleParser.parse(case2)).toInt())

        val case1 = "FF|(00&00)"
        assertEquals(1, ruleParser.parse(case1).size)
        assertEquals(0xFF, ruleParser.oneOf(ruleParser.parse(case1)).toInt())


    }


}