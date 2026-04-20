package pl.debiljpg.pocoled.test
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
class MainActivity : ComponentActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}
fun runShizukuCommand(cmd: String, onResult: (String) -> Unit) {
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
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var logBuffer by remember { mutableStateOf("System Ready.\n") }
    var red by remember { mutableFloatStateOf(0f) }
    var green by remember { mutableFloatStateOf(1f) }
    var blue by remember { mutableFloatStateOf(0f) }
    var mode by remember { mutableStateOf("1") }
    var brightness by remember { mutableStateOf("100") }
    var onMs by remember { mutableStateOf("2000") }
    var offMs by remember { mutableStateOf("500") }
    var pkg by remember { mutableStateOf("com.android.camera") }
    var style by remember { mutableStateOf("12") }
    val currentColor = Color(red, green, blue)
    val colorInt = currentColor.toArgb()
    fun appendLog(msg: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logBuffer = "[$time] $msg\n$logBuffer".take(2000)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Podstawy") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Ekspert") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Logi") })
        }
        when (selectedTab) {
            0 -> BasicTab(red, green, blue, {red = it}, {green = it}, {blue = it}, { 
                val cmd = buildLedCommand(colorInt, mode.toIntOrNull()?:1, onMs.toIntOrNull()?:0, offMs.toIntOrNull()?:0, brightness.toIntOrNull()?:0, pkg, style.toIntOrNull()?:0)
                runShizukuCommand(cmd) { appendLog(it) }
            })
            1 -> ExpertTab(colorInt, mode, onMs, offMs, brightness, pkg, style, { mode = it }, { onMs = it }, { offMs = it }, { brightness = it }, { pkg = it }, { style = it }, { cmd -> runShizukuCommand(cmd) { appendLog(it) } })
            2 -> StatusTab(logBuffer)
        }
    }
}
@Composable
fun BasicTab(r: Float, g: Float, b: Float, onUpdateR: (Float)->Unit, onUpdateG: (Float)->Unit, onUpdateB: (Float)->Unit, onSend: () -> Unit) {
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Kolor LED", style = MaterialTheme.typography.titleMedium)
        Card {
            Column(Modifier.padding(12.dp)) {
                Box(Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(r,g,b)).border(1.dp, Color.Gray, RoundedCornerShape(8.dp)))
                Spacer(Modifier.height(12.dp))
                ColorSlider("R", r, onUpdateR)
                ColorSlider("G", g, onUpdateG)
                ColorSlider("B", b, onUpdateB)
            }
        }
        Text("Szybkie Presety", fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PresetButton("Alarm", Color.Red, Modifier.weight(1f)) { onUpdateR(1f); onUpdateG(0f); onUpdateB(0f) }
            PresetButton("Zen", Color.Cyan, Modifier.weight(1f)) { onUpdateR(0f); onUpdateG(1f); onUpdateB(1f) }
            PresetButton("Wyłącz", Color.Black, Modifier.weight(1f)) { onUpdateR(0f); onUpdateG(0f); onUpdateB(0f) }
        }
        Button(onClick = onSend, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Send, null)
            Spacer(Modifier.width(8.dp))
            Text("WYŚLIJ DO LED", fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun ExpertTab(color: Int, mode: String, on: String, off: String, br: String, pkg: String, style: String, onM: (String)->Unit, onO: (String)->Unit, onOf: (String)->Unit, onB: (String)->Unit, onP: (String)->Unit, onS: (String)->Unit, onSend: (String)->Unit) {
    var rawCmd by remember { mutableStateOf("") }
    val currentAutoCmd = buildLedCommand(color, mode.toIntOrNull()?:1, on.toIntOrNull()?:0, off.toIntOrNull()?:0, br.toIntOrNull()?:0, pkg, style.toIntOrNull()?:0)
    LaunchedEffect(color, mode, on, off, br, pkg, style) { rawCmd = currentAutoCmd }
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Parametry ILightsManager", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = mode, onValueChange = onM, label = {Text("Mode")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = br, onValueChange = onB, label = {Text("Bright")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = on, onValueChange = onO, label = {Text("OnMs")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = off, onValueChange = onOf, label = {Text("OffMs")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
        OutlinedTextField(value = pkg, onValueChange = onP, label = {Text("Package")}, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = style, onValueChange = onS, label = {Text("StyleType")}, modifier = Modifier.fillMaxWidth())
        Text("Ręczna Komenda (Shell)", fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            OutlinedTextField(value = rawCmd, onValueChange = { rawCmd = it }, modifier = Modifier.fillMaxWidth().padding(4.dp), textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), maxLines = 5)
        }
        Button(onClick = { onSend(rawCmd) }, Modifier.fillMaxWidth()) { Text("WYKONAJ KOMENDĘ") }
    }
}
@Composable
fun StatusTab(logs: String) {
    Column(Modifier.padding(16.dp)) {
        Text("Konsola Wyjściowa", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxSize().background(Color.Black, RoundedCornerShape(8.dp)).padding(8.dp).border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))) {
            Text(logs, color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.verticalScroll(rememberScrollState()))
        }
    }
}
@Composable
fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(20.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Slider(value = value, onValueChange = onValueChange, modifier = Modifier.weight(1f))
        Text((value * 255).toInt().toString().padStart(3), Modifier.width(35.dp), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}
@Composable
fun PresetButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp)
    }
}
