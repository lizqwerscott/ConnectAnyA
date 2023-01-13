package com.flydog.connectanya

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.flydog.connectanya.databinding.ActivityMainBinding
import com.flydog.connectanya.services.ConnectService
import com.flydog.connectanya.ui.MainViewModel
import com.flydog.connectanya.utils.ClipboardUtil

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private var deviceID: String = ""
    private var userName: String = ""

    private var currentClipboardData: String = ""

    private var connectService: ConnectService? = null
    private var isBind = false
    private var conn = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            isBind = true
            val myBinder = p1 as ConnectService.MsgBinder
            connectService = myBinder.getService()
            connectService!!.onClipboardDataUpdateListener = object : ConnectService.OnClipboardUpdateListener {
                override fun onClipboardUpdate(data: String) {
                    runOnUiThread {
                        viewModel.setClipboardData(data)
                    }
                    currentClipboardData = data
                }
            }
            Log.w("connectService", "bind service")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBind = false
            Log.w("connectService", "unBind service")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // 检查登陆信息
        if (loadIdName()) {
            this.showLoginDialog()
        } else {
            Log.i("MainActivity", "Start foreground service")
            startForegroundService(Intent(this, ConnectService::class.java))
            //Bind connectService
            bindService(Intent(this, ConnectService::class.java), conn, Context.BIND_AUTO_CREATE)
        }

        viewModel.setClipboardData(currentClipboardData)
    }

    override fun onResume() {
        super.onResume()
        viewModel.setClipboardData(currentClipboardData)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun updateSideUserShow(user: String, id: String) {
        if (binding.navView.headerCount > 0) {
            val header = binding.navView.getHeaderView(0)

            val showUserName = header.findViewById<TextView>(R.id.sideUserName)
            showUserName.text = user
            val showDeviceId = header.findViewById<TextView>(R.id.sideDeviceName)
            showDeviceId.text = id
        }
    }

    private fun isSaveName(): Boolean {
        val setting = getSharedPreferences("settings", 0)
        val id = setting.getString("deviceId", "-1").toString()
        val name = setting.getString("userName", "-1").toString()
        return id != "-1" && name != "-1"
    }

    private fun loadIdName(): Boolean {
        val setting = getSharedPreferences("settings", 0)
        val id = setting.getString("deviceId", "-1").toString()
        val name = setting.getString("userName", "-1").toString()

        updateSideUserShow(name, id)

        this.deviceID = id
        this.userName = name
        return id == "-1" && name == "-1"
    }

    private fun updateIdName(name: String, id: String) {
        this.deviceID = id
        this.userName = name
        val settings = getSharedPreferences("settings", 0)
        val editor = settings.edit()
        editor.putString("deviceId", this.deviceID)
        editor.putString("userName", this.userName)
        editor.apply()

        updateSideUserShow(name, id)
    }

    private fun showLoginDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.activity_sign, null)

        AlertDialog.Builder(this)
            .setTitle("登陆")
            .setView(view)
            .setPositiveButton("登陆") { _, _ ->
                val editTextDeviceId = view.findViewById<EditText>(R.id.deviceID)
                val editTextUserName = view.findViewById<EditText>(R.id.userName)

                val id = editTextDeviceId.text.toString().trim()
                val name = editTextUserName.text.toString().trim()

                if (id == "" || name == "") {
                    Toast.makeText(this, "请输入设备id和用户名, 重新登陆", Toast.LENGTH_SHORT).show()
                } else {
                    if (isSaveName()) {
                        updateIdName(name, id)
                        startForegroundService(Intent(this, ConnectService::class.java))
                        //Bind connectService
                        bindService(Intent(this, ConnectService::class.java), conn, Context.BIND_AUTO_CREATE)
                    }
                    updateIdName(name, id)
                    Toast.makeText(this, "提交完成", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

}