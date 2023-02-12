package com.flydog.connectanya.services

import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.flydog.connectanya.MainActivity
import com.flydog.connectanya.R
import com.flydog.connectanya.datalayer.repository.UserData
import com.flydog.connectanya.utils.ClipboardUtil
import com.flydog.connectanya.utils.HttpUtils
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule

class ConnectService : Service() {

    private val binder = MsgBinder()

    var onClipboardDataUpdateListener: OnClipboardUpdateListener? = null

    private val notificationChannelId = "connectService_id_02"
    private val notificationClipboardChannelId = "connectService_id_03"

    private var userData: UserData = UserData("", "")

    private var lastClipboardData = ""
    private var clipboardTextData = ""

    private var returnClipboardDataTimer: Timer? = null

    private var job = MainScope()

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

        FloatWindowViewModel.onWindowClickListener.postValue(OnFloatWindowClickListener())

        val notification = createForegroundNotification()
        startForeground(2, notification)

        val task = ServerConnectTask()
        Timer().schedule(task, 1000, 1000)

        Timer().schedule(1000) {
            clipboardTextData = ClipboardUtil.getClipboardTextFront(this@ConnectService)
            onClipboardDataUpdateListener?.onClipboardUpdate(clipboardTextData)
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
        job.cancel()
        Log.w("Info", "ConnectService destroy")
    }

    private fun createForegroundNotification(): Notification {

//        val notifyIntent = Intent(this, SelectDeviceActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE)

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

    fun updateUserData(userData: UserData) {
        this.userData = userData
    }

    fun getHostAddress(): String {
        val sharePreferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        val address = sharePreferenceManager.getString("host", "-1")
        return if (address == "-1" || address == null) {
            "101.42.233.83:8686"
        } else {
            address
        }
    }

    fun updateClipboard(data: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("recive text", data)
        clipboard.setPrimaryClip(clip)
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
                lastClipboardData = clipboardTextData
                clipboardTextData = tempClipboard

                if (returnClipboardDataTimer != null) {
                    returnClipboardDataTimer?.cancel()
                }

                val notification = createClipboardNotification()
                with(NotificationManagerCompat.from(this@ConnectService)) {
                    notify(id, notification)
                }

                // 同步UI
                onClipboardDataUpdateListener?.onClipboardUpdate(clipboardTextData)

                job.launch(Dispatchers.Default) {
                    // 发送新复制的信息给服务器
                    withContext(Dispatchers.IO) {
                        HttpUtils.addMessage(getHostAddress(), clipboardTextData, userData.deviceId)
                    }
                }
            }

            // 和服务器同步
            Log.i(TAG, clipboardTextData)

            if (userData.username != "") {
                job.launch(Dispatchers.Default) {
                    // 从服务器获取新的剪切板信息
                    withContext(Dispatchers.IO) {
//                    HttpUtils.updateMessage(getHostAddress(), userData.deviceId)
                        val clipboard = HttpUtils.updateBaseMessage(getHostAddress(), userData.deviceId)
                        if (clipboard != null && clipboard.type == "text") {
                            lastClipboardData = clipboardTextData
                            clipboardTextData = clipboard.data
                            updateClipboard(clipboardTextData)
                            onClipboardDataUpdateListener?.onClipboardUpdate(clipboardTextData)

                            if (returnClipboardDataTimer != null) {
                                returnClipboardDataTimer?.cancel()
                            }
                            returnClipboardDataTimer = Timer()
                            returnClipboardDataTimer?.schedule(6 * 1000) {
                                clipboardTextData = lastClipboardData
                                updateClipboard(clipboardTextData)
                                returnClipboardDataTimer = null
                            }
                        }
                    }
                }
            }
        }
    }

    interface OnClipboardUpdateListener {
        fun onClipboardUpdate(data: String)
    }

    inner class OnFloatWindowClickListener : View.OnClickListener {
        override fun onClick(p0: View?) {
            Log.i(TAG, "click")
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val clipboardData = clip.getItemAt(0).coerceToText(this@ConnectService).toString()
                if (clipboardData != "") {
                    lastClipboardData = clipboardTextData
                    clipboardTextData = clipboardData


                    // 同步UI
                    onClipboardDataUpdateListener?.onClipboardUpdate(clipboardTextData)

                    job.launch(Dispatchers.Default) {
                        // 发送新复制的信息给服务器
                        withContext(Dispatchers.IO) {
                            HttpUtils.addMessage(getHostAddress(), clipboardTextData, userData.deviceId)
                        }
                    }
                    Log.i(TAG, "getClipboardData: $clipboardData, $clipboardTextData")
                }
            } else {
                Log.i(TAG, "Can't get clipboard data")
            }
        }
    }

    companion object {
        private const val TAG = "ConnectService"
    }
}