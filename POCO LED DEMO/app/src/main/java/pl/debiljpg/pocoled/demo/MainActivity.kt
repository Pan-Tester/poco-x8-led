package pl.debiljpg.pocoled.demo
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(1001)
        }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isRunning by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        IconButton(onClick = { 
                            isRunning = !isRunning
                            val intent = Intent(this@MainActivity, PocoShakeService::class.java)
                            if (isRunning) startForegroundService(intent) else stopService(intent)
                        }, modifier = Modifier.size(100.dp)) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(60.dp), tint = if (isRunning) androidx.compose.ui.graphics.Color.Cyan else androidx.compose.ui.graphics.Color.Gray)
                        }
                        Spacer(Modifier.height(50.dp))
                        Button(onClick = { LedManager.setLedDisco(Color.BLUE) }, modifier = Modifier.size(100.dp, 50.dp)) { }
                    }
                }
            }
        }
    }
}
