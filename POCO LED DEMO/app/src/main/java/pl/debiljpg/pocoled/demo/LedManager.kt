package pl.debiljpg.pocoled.demo

import android.graphics.Color
import android.util.Log
import rikka.shizuku.Shizuku
import kotlin.concurrent.thread

object LedManager {
    val RED = Color.parseColor("#FF0000")
    val BLUE = Color.parseColor("#0000FF")
    val YELLOW = Color.parseColor("#FFFF00")
    val GREEN = Color.parseColor("#00FF00")
    val CYAN = Color.parseColor("#00FFFF")
    val ORANGE = Color.parseColor("#FF8C00")
    val DEEP_PURPLE = Color.parseColor("#8A2BE2")
    
    val DISCO_COLORS = listOf(RED, BLUE, YELLOW, GREEN, CYAN, ORANGE, DEEP_PURPLE)

    fun runShizukuCommand(cmd: String, onResult: (String) -> Unit = {}) {
        thread {
            try {
                val shizukuClass = Class.forName("rikka.shizuku.Shizuku")
                val newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
                newProcessMethod.isAccessible = true
                val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", cmd), null, null) as java.lang.Process
                
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                process.waitFor()
                val result = if (error.isNotBlank()) "ERR: $error\n$output" else output.ifBlank { "OK" }
                onResult(result)
            } catch (e: Exception) {
                onResult("Błąd: ${e.message}")
            }
        }
    }

    fun buildLedCommand(color: Int, mode: Int, on: Int, off: Int, br: Int, pkg: String, style: Int): String {
        return "service call miui.lights.ILightsManager 4 i32 $color i32 $mode i32 $on i32 $off i32 $br s16 \"$pkg\" i32 $style i32 0"
    }

    fun setLedDisco(color: Int) {
        val cmd = buildLedCommand(color, 1, 2000, 500, 100, "com.android.camera", 12)
        runShizukuCommand(cmd)
    }

    fun turnOff() {
        val cmd = buildLedCommand(0, 0, 0, 0, 0, "com.android.camera", 0)
        runShizukuCommand(cmd)
    }
}
