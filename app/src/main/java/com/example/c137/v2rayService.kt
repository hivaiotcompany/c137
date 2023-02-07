package com.example.c137

import java.lang.ref.SoftReference
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.c137.MainActivity
import kotlin.concurrent.thread
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import com.example.c137.Callback
import com.example.c137.ServiceControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object V2RayServiceManager {
    private const val NOTIFICATION_ID = 1
    private const val NOTIFICATION_PENDING_INTENT_CONTENT = 0
    private const val NOTIFICATION_PENDING_INTENT_STOP_V2RAY = 1
    private const val NOTIFICATION_ICON_THRESHOLD = 3000
    var serviceControl: SoftReference<ServiceControl>? = null
    private val mMsgReceive = ReceiveMessageHandler()
    val v2rayPoint: V2RayPoint = Libv2ray.newV2RayPoint(
        Callback.V2RayCallback(),
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    )
    private var mNotificationManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null

    fun getV2rayCong(): String {
        val service = serviceControl?.get()?.getService() ?: return ""
        val assets = service.assets.open("nf.json").bufferedReader().use {
            it.readText()
        }
        if (TextUtils.isEmpty(assets)) {
            return ""
        }
        //val v2rayConfig = Gson().fromJson(assets, V2rayConfig::class.java) ?: return result
        //v2rayConfig.log.loglevel =  "debug"

        Log.d("com.example.c137", assets.toString())
        return assets.toString()
    }

    fun startV2rayPoint() {
        val service = serviceControl?.get()?.getService() ?: return
        if (!v2rayPoint.isRunning) {
            try {
                val mFilter = IntentFilter(AppConfig.BROADCAST_ACTION_SERVICE)
                mFilter.addAction(Intent.ACTION_SCREEN_ON)
                mFilter.addAction(Intent.ACTION_SCREEN_OFF)
                mFilter.addAction(Intent.ACTION_USER_PRESENT)
                service.registerReceiver(mMsgReceive, mFilter)
            } catch (e: Exception) {
                Log.d("com.v2ray.example", e.toString())
            }

            v2rayPoint.configureFileContent = getV2rayCong()
            v2rayPoint.domainName = "my.hellodear.online:443"
//                v2rayPoint.domainName="hey.hellodear.online:443"
//                v2rayPoint.domainName="94.182.190.234:995"
            try {
                v2rayPoint.runLoop(false)
            } catch (e: Exception) {
                Log.d("com.example.c137", e.toString())
            }
            Toast.makeText(service, v2rayPoint.isRunning.toString(), Toast.LENGTH_SHORT).show()
            showNotification()
        } else {
            v2rayPoint.stopLoop()
            Toast.makeText(service, v2rayPoint.isRunning.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun stopV2rayPoint() {
        val service = serviceControl?.get()?.getService() ?: return

        if (v2rayPoint.isRunning) {
            GlobalScope.launch(Dispatchers.Default) {
                try {
                    v2rayPoint.stopLoop()
                } catch (e: Exception) {
                    Log.d("com.example.c137", e.toString())
                }
            }
        }
        cancelNotification()
        try {
            service.unregisterReceiver(mMsgReceive)
        } catch (e: Exception) {
            Log.d("com.example.c137", e.toString())
        }
    }

    fun cancelNotification() {
        val service = serviceControl?.get()?.getService() ?: return
        service.stopForeground(true)
        mBuilder = null
    }

    private fun showNotification() {
        val service = serviceControl?.get()?.getService() ?: return
        val startMainIntent = Intent(service, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            service,
            NOTIFICATION_PENDING_INTENT_CONTENT, startMainIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val stopV2RayIntent = Intent(AppConfig.BROADCAST_ACTION_SERVICE)
        stopV2RayIntent.`package` = AppConfig.ANG_PACKAGE
        stopV2RayIntent.putExtra("key", AppConfig.MSG_STATE_STOP)

        val stopV2RayPendingIntent = PendingIntent.getBroadcast(
            service,
            NOTIFICATION_PENDING_INTENT_STOP_V2RAY, stopV2RayIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        mBuilder = NotificationCompat.Builder(service, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("hey!")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                com.google.android.material.R.drawable.ic_m3_chip_close,
                "stop",
                stopV2RayPendingIntent)
//        .build()

        mBuilder?.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
        service.startForeground(NOTIFICATION_ID, mBuilder?.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "RAY_NG_M_CH_ID"
        val channelName = "V2rayNG Background Service"
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.DKGRAY
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getNotificationManager()?.createNotificationChannel(chan)
        return channelId
    }

    private fun getNotificationManager(): NotificationManager? {
        val service = serviceControl?.get()?.getService() ?: return null
        if (mNotificationManager == null) {
            mNotificationManager =
                service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return mNotificationManager
    }
    private class ReceiveMessageHandler : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val serviceControl = serviceControl?.get() ?: return
            when (intent?.getIntExtra("key", 0)) {
                AppConfig.MSG_REGISTER_CLIENT -> {
                    //Logger.e("ReceiveMessageHandler", intent?.getIntExtra("key", 0).toString())
//                    if (v2rayPoint.isRunning) {
//                        MessageUtil.sendMsg2UI(serviceControl.getService(), AppConfig.MSG_STATE_RUNNING, "")
//                    } else {
//                        MessageUtil.sendMsg2UI(serviceControl.getService(), AppConfig.MSG_STATE_NOT_RUNNING, "")
//                    }
                }
                AppConfig.MSG_UNREGISTER_CLIENT -> {
                    // nothing to do
                }
                AppConfig.MSG_STATE_START -> {
                    // nothing to do
                }
                AppConfig.MSG_STATE_STOP -> {
                    serviceControl.stopService()
                }
                AppConfig.MSG_STATE_RESTART -> {
                    startV2rayPoint()
                }
            }
        }
    }
}