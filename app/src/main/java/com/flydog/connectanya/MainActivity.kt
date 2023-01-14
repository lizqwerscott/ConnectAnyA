package com.flydog.connectanya

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.flydog.connectanya.databinding.ActivityMainBinding
import com.flydog.connectanya.datalayer.model.RegisterModel
import com.flydog.connectanya.datalayer.repository.LoginResult
import com.flydog.connectanya.services.ConnectService
import com.flydog.connectanya.ui.MainViewModel
import com.flydog.connectanya.ui.setting.SettingActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

//    private var viewModel: MainViewModel = ViewModelProvider(
//        this, MainViewModelFactory(
//            UserDataRepository(dataStore)
//        )
//    ).get(MainViewModel::class.java)

    private var connectService: ConnectService? = null
    private var isBind = false
    private var conn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            isBind = true
            val myBinder = p1 as ConnectService.MsgBinder
            connectService = myBinder.getService()
            connectService!!.onClipboardDataUpdateListener =
                object : ConnectService.OnClipboardUpdateListener {
                    override fun onClipboardUpdate(data: String) {
                        if (data != "") {
                            runOnUiThread {
                                viewModel.setClipboardData(data)
                            }
                        }
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

        // check permissions is granted
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewModel.initialSetupEvent.observe(this) { initialSetupEvent ->

            if (initialSetupEvent.username == "") {
                this.showLoginDialog()
            }

            viewModel.userDataUiModel.observe(this) { userDataUiModel ->

                if (binding.navView.headerCount > 0) {
                    val header = binding.navView.getHeaderView(0)

                    val showUserName = header.findViewById<TextView>(R.id.sideUserName)
                    showUserName.text = userDataUiModel.username
                    val showDeviceId = header.findViewById<TextView>(R.id.sideDeviceName)
                    showDeviceId.text = userDataUiModel.deviceId
                }
            }
        }

        val sharePreferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        with(sharePreferenceManager.edit()) {
            putString("host", "mock.apifox.cn/m1/2074769-0-default")
            apply()
        }

        val a = sharePreferenceManager.getString("host", "-1")
        Toast.makeText(this, a, Toast.LENGTH_SHORT).show()

        startForegroundService(Intent(this, ConnectService::class.java))
        //Bind connectService
        bindService(Intent(this, ConnectService::class.java), conn, Context.BIND_AUTO_CREATE)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this, SettingActivity::class.java))
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun getHostAddress(): String {
        val sharePreferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        val address = sharePreferenceManager.getString("host", "-1")
        return if (address == "-1" || address == null) {
            "192.168.3.113:8686"
        } else {
            address
        }
    }

    private fun showLoginDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.activity_sign, null)

        AlertDialog.Builder(this).setTitle("登陆或者注册").setView(view).setPositiveButton("登陆") { _, _ ->
                val editTextUserName = view.findViewById<EditText>(R.id.userName)

                val name = editTextUserName.text.toString().trim()

                if (name == "") {
                    Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    GlobalScope.launch(Dispatchers.IO) {
                        val res = viewModel.login(getHostAddress(), name)
                        when (res) {
                            is LoginResult.Success<RegisterModel> -> {
                                if (res.data.code == 200) {
                                    viewModel.updateUserName(name)
                                    runOnUiThread {
                                        Toast.makeText(this@MainActivity, "提交完成", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@MainActivity, res.data.msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            else -> {
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, "网络连接错误，请检查网络或者检查默认服务器地址", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.cancel()
            }.create().show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}