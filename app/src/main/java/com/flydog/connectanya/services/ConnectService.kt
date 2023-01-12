package com.flydog.connectanya.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.flydog.connectanya.MainActivity
import com.flydog.connectanya.R

class ConnectService : Service() {

    private val binder = MsgBinder()

    override fun onCreate() {
        super.onCreate()
        Log.w("Info", "ConnectService created")

        val notification = createForegroundNotification()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.w("Info", "ConnectService start")
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent): IBinder {
        Log.w("Info", "ConnectService bind")
        return binder
    }

    override fun onDestroy() {
        stopForeground(true)
        Log.w("Info", "ConnectService destroy")
    }

    private fun createForegroundNotification(): Notification {

        val notificationChannelId = "connectService_id_02"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelName = "连接后台服务"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(notificationChannelId, channelName, importance)

        notificationChannel.description = "保证可以和其他设备同步"
        notificationManager.createNotificationChannel(notificationChannel)

//        val norifyIntent = Intent(this, SelectDeviceActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val notifyPendingIntent = PendingIntent.getActivity(this, 0, norifyIntent, PendingIntent.FLAG_IMMUTABLE)

        val intent = Intent(this, MainActivity::class.java)
        val notifyPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, notificationChannelId)
        builder.setSmallIcon(R.drawable.icon)
        builder.setContentTitle("连接服务")
        builder.setContentText("点击回到主界面")
        builder.setContentIntent(notifyPendingIntent)

        return builder.build()
    }

    inner class MsgBinder: Binder() {
        fun getService(): ConnectService {
            return this@ConnectService
        }
    }
}