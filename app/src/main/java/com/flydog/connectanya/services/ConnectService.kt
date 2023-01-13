package com.flydog.connectanya.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.flydog.connectanya.MainActivity
import com.flydog.connectanya.R
import com.flydog.connectanya.utils.ClipboardUtil
import java.util.*
import kotlin.concurrent.schedule

class ConnectService : Service() {

    private val binder = MsgBinder()

    var onClipboardDataUpdateListener: OnClipboardUpdateListener? = null

    private val notificationChannelId = "connectService_id_02"
    private val notificationClipboardChannelId = "connectService_id_03"

    private var clipboardTextData = ""

    override fun onCreate() {
        super.onCreate()
        Log.w("Info", "ConnectService created")

        // 创建通知频道
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channelName = "连接后台服务"
        val notificationChannel =
            NotificationChannel(notificationChannelId, channelName, importance)
        notificationChannel.description = "保证可以和其他设备同步"
        notificationManager.createNotificationChannel(notificationChannel)

        val clipboardChannelName = "剪切板通知"
        val notificationClipChannel =
            NotificationChannel(notificationClipboardChannelId, clipboardChannelName, importance)
        notificationClipChannel.description = "剪切板改变通知"
        notificationClipChannel.setShowBadge(true)
        notificationClipChannel.lightColor = Color.BLUE
        notificationClipChannel.enableLights(true)
        notificationManager.createNotificationChannel(notificationClipChannel)

        val notification = createForegroundNotification()
        startForeground(2, notification)

        val task = ServerConnectTask()
        Timer().schedule(task, 1000, 1000)

        Timer().schedule(1000) {
            clipboardTextData = ClipboardUtil.getClipboardTextFront(this@ConnectService)
        }
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

//        val norifyIntent = Intent(this, SelectDeviceActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val notifyPendingIntent = PendingIntent.getActivity(this, 0, norifyIntent, PendingIntent.FLAG_IMMUTABLE)

        val intent = Intent(this, MainActivity::class.java)
        val notifyPendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, notificationChannelId)
        builder.setSmallIcon(R.drawable.icon)
        builder.setContentTitle("连接服务")
        builder.setContentText("点击回到主界面")
        builder.setContentIntent(notifyPendingIntent)

        return builder.build()
    }

    private fun createClipboardNotification(): Notification {

        val intent = Intent(this, MainActivity::class.java)
        val notifyPendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, notificationClipboardChannelId)
        builder.setSmallIcon(R.drawable.icon)
        builder.setWhen(System.currentTimeMillis())
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setOngoing(true)
        builder.setContentTitle("您复制了新的内容")
        builder.setContentText(clipboardTextData)
        builder.setAutoCancel(true)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(clipboardTextData)
        )
        builder.setContentIntent(notifyPendingIntent)

        return builder.build()
    }

    inner class MsgBinder : Binder() {
        fun getService(): ConnectService {
            return this@ConnectService
        }
    }

    inner class ServerConnectTask : TimerTask() {
        private val id = 1
        override fun run() {
            // 获取本地剪切板
            val tempClipboard = ClipboardUtil.getClipboardText()
            if (tempClipboard != "" && tempClipboard != clipboardTextData) {
                clipboardTextData = tempClipboard

                val notification = createClipboardNotification()
                with(NotificationManagerCompat.from(this@ConnectService)) {
                    notify(id, notification)
                }
            }
            // 同步UI
            onClipboardDataUpdateListener?.onClipboardUpdate(clipboardTextData)
            // 和服务器同步
            Log.i("ConnectService", clipboardTextData)
        }
    }

    interface OnClipboardUpdateListener {
        fun onClipboardUpdate(data: String)
    }
}