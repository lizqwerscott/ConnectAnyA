package com.flydog.connectanya

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.flydog.connectanya.services.ConnectService
import com.flydog.connectanya.ui.MainViewModel
import com.flydog.connectanya.ui.setting.SettingActivity
import com.google.android.material.navigation.NavigationView


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
            Log.w(TAG, "bind service")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBind = false
            Log.w(TAG, "unBind service")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
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
            "101.42.233.83"
        } else {
            address
        }
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