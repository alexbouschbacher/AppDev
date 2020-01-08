package com.example.epicture

import com.example.epicture.ui.home.HomeFragment
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun convertBase64(){
        val home = HomeFragment()
        println(home)
        assertEquals("", home.encoder(""))
    }

    @Test
    fun convertBase64_2(){
        val home = HomeFragment()
        println(home)
        val result = home.encoder("@drawable/ic_upvote_empty.png")
        println(result)
        assertEquals("", home.encoder("@drawable/ic_upvote_empty.png"))
    }
}
