package pl.debiljpg.pocoled.demo
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt
import java.util.Random
import kotlin.concurrent.thread
class PocoShakeService : Service(), SensorEventListener {
    private lateinit var sm: SensorManager
    private var acc: Sensor? = null
    private val r = Random()
    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("c", "S", NotificationManager.IMPORTANCE_LOW))
        val n = NotificationCompat.Builder(this, "c").setSmallIcon(android.R.drawable.ic_menu_compass).setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(99, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(99, n)
        }
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_UI)
    }
    override fun onSensorChanged(e: SensorEvent) {
        val v = sqrt(e.values[0]*e.values[0] + e.values[1]*e.values[1] + e.values[2]*e.values[2]) - 9.81f
        if (v > 12) {
            val c = LedManager.DISCO_COLORS[r.nextInt(LedManager.DISCO_COLORS.size)]
            LedManager.setLedDisco(c)
            thread {
                Thread.sleep(500)
                LedManager.turnOff()
            }
        }
    }
    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
    override fun onBind(i: Intent?): IBinder? = null
    override fun onDestroy() {
        sm.unregisterListener(this)
        LedManager.turnOff()
        super.onDestroy()
    }
}
