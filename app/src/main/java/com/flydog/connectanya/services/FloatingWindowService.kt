package com.flydog.connectanya.services

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.lifecycle.LifecycleService
import com.flydog.connectanya.R
import com.flydog.connectanya.utils.PermissionUtil
import java.util.*

class FloatingWindowService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null//悬浮窗View
    private var clickNumber = 0
    private var windowX = 0
    private var windowY = 0

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

            flags = if (clickNumber == 0) {
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            } else {
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }

            //位置大小设置
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = Gravity.LEFT or Gravity.TOP
            //设置剧中屏幕显示
            if (clickNumber == 0) {
                x = outMetrics.widthPixels / 2 - width / 2
                y = outMetrics.heightPixels / 2 - height / 2
                windowX = x
                windowY = y
            } else {
                x = windowX
                y = windowY
            }
        }
        // 新建悬浮窗控件
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_float, null)
        floatRootView?.setOnTouchListener(FloatWindowTouchListener(layoutParam, windowManager))
        if (FloatWindowViewModel.onWindowClickListener.value != null) {
            floatRootView?.setOnClickListener(FloatWindowViewModel.onWindowClickListener.value)
            floatRootView?.setOnLongClickListener {
                Log.i(TAG, "n: $clickNumber")
                FloatWindowViewModel.isShowSuspendWindow.postValue(false)
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        FloatWindowViewModel.isShowSuspendWindow.postValue(true)
                    }
                }, 10)

                if (clickNumber == 0) {
                    clickNumber++
                } else {
                    clickNumber = 0
                }
                false
            }
        } else {
            Log.i(TAG, "onCLickListener is null")
        }
        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(floatRootView, layoutParam)
    }

    inner class FloatWindowTouchListener(
        private val wl: WindowManager.LayoutParams, val windowManager: WindowManager
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
                        windowX = x
                        windowY = y
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

    companion object {
        const val TAG = "FloatingWindowService"
    }
}