package com.flydog.connectanya.services

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.lifecycle.LifecycleService
import com.flydog.connectanya.R
import com.flydog.connectanya.utils.PermissionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FloatingWindowService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null//悬浮窗View
    private var host = ""
    private var deviceId = ""
    private var onClickListener: View.OnClickListener? = null

    override fun onCreate() {
        super.onCreate()
        initObserve()
    }

    private fun initObserve() {
        FloatWindowViewModel.apply {
            isVisible.observe(this@FloatingWindowService) {
                floatRootView?.visibility = if (it) View.VISIBLE else View.GONE
            }
            isShowSuspendWindow.observe(this@FloatingWindowService) {
                if (it) {
                    showWindow()
                } else {
                    if (!PermissionUtil.isNull(floatRootView)) {
                        if (!PermissionUtil.isNull(floatRootView?.windowToken)) {
                            if (!PermissionUtil.isNull(windowManager)) {
                                windowManager.removeView(floatRootView)
                            }
                        }
                    }
                }
            }
            deviceChange.observe(this@FloatingWindowService) {
                deviceId = it
            }
            hostChange.observe(this@FloatingWindowService) {
                host = it
            }
            onWindowClickListener.observe(this@FloatingWindowService) {
                onClickListener = it
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        val layoutParam = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            //位置大小设置
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = Gravity.LEFT or Gravity.TOP
            //设置剧中屏幕显示
            x = outMetrics.widthPixels / 2 - width / 2
            y = outMetrics.heightPixels / 2 - height / 2
        }
        // 新建悬浮窗控件
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_float, null)
        floatRootView?.setOnTouchListener(FloatWindowTouchListener(layoutParam, windowManager))
        if (onClickListener != null) {
            floatRootView?.setOnClickListener(onClickListener)
        }
        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(floatRootView, layoutParam)
    }

    class FloatWindowTouchListener(
        val wl: WindowManager.LayoutParams, val windowManager: WindowManager
    ) : View.OnTouchListener {
        private var x = 0
        private var y = 0
        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = motionEvent.rawX.toInt()
                    y = motionEvent.rawY.toInt()

                }
                MotionEvent.ACTION_MOVE -> {
                    val nowX = motionEvent.rawX.toInt()
                    val nowY = motionEvent.rawY.toInt()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    wl.apply {
                        x += movedX
                        y += movedY
                    }
                    //更新悬浮球控件位置
                    windowManager.updateViewLayout(view, wl)
                }
                else -> {

                }
            }
            return false
        }
    }

    class FloatWindowClickListener(val context: Context) : View.OnClickListener {
        override fun onClick(p0: View?) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val clipboardData = clip.getItemAt(0).coerceToText(context).toString()
                GlobalScope.launch(Dispatchers.Default) {
                    // 发送新复制的信息给服务器
                    withContext(Dispatchers.IO) {
//                        HttpUtils.addMessage(host, clipboardData, deviceId)
                    }
                }
                Log.i(TAG, "getClipboardData: $clipboardData")
            } else {
                Log.i(TAG, "Can't get clipboard data")
            }
        }
    }

    companion object {
        val TAG = "FloatingWindowService"
    }
}