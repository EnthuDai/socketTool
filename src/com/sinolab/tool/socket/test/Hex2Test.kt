package com.sinolab.tool.socket.test

import com.sinolab.tool.socket.util.Hex2
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class Hex2Test {

    @Test
    fun testUByteToHexString() {
        assertEquals("FF", Hex2.ubyteToHexString(0xFFu ))
        assertEquals("00", Hex2.ubyteToHexString(0x00u ))
    }
}