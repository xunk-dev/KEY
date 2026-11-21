package com.spacekeyboard.keyboard.translate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TranslateJsonTest {
    @Test
    fun encodesAllFieldsAndEscapes() {
        val request = TranslateRequest("a \"b\" \\ \n\t\u0001 مرحبا 😀", "auto", "en", TranslateStyle.CHAT)
        val json = TranslateJson.encode(request)
        val back = TranslateJson.parseFlat(json)!!
        assertEquals(request.text, back["text"])
        assertEquals("auto", back["source"])
        assertEquals("en", back["target"])
        assertEquals("chat", back["style"])
    }

    @Test
    fun parsesNumbersLiteralsAndWhitespace() {
        val map = TranslateJson.parseFlat(" { \"text\" : \"hi\\u0041\\/\" , \"remaining\": 7, \"x\": null, \"ok\": true, \"f\": -1.5 } ")!!
        assertEquals("hiA/", map["text"])
        assertEquals(7L, map["remaining"])
        assertNull(map["x"])
        assertEquals(true, map["ok"])
        assertEquals(-1.5, map["f"])
    }

    @Test
    fun parsesEmptyObject() {
        assertEquals(emptyMap<String, Any?>(), TranslateJson.parseFlat("{}"))
    }

    @Test
    fun badInputIsNull() {
        listOf("", "nope", "[1]", "{\"a\"}", "{\"a\":}", "{\"a\":\"x", "{\"a\":\"\\q\"}", "{\"a\":1").forEach {
            assertNull(it, TranslateJson.parseFlat(it))
        }
    }
}
