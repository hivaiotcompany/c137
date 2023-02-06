package com.example.c137

import android.app.*
import android.content.Context
import android.content.Intent
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
import libv2ray.V2RayVPNServiceSupportsSet

private const val NOTIFICATION_ID = 1
private const val NOTIFICATION_PENDING_INTENT_CONTENT = 0
private const val NOTIFICATION_PENDING_INTENT_STOP_V2RAY = 1
private const val NOTIFICATION_ICON_THRESHOLD = 3000

class V2rayService : Service() {
    val v2rayPoint: V2RayPoint = Libv2ray.newV2RayPoint(Callback.V2RayCallback(), Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
    private var mNotificationManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null

    fun getV2rayCong(): String {
        val assets = this.assets.open("nf.json").bufferedReader().use {
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

    fun startV2ray(){
        if (!v2rayPoint.isRunning) {
            v2rayPoint.configureFileContent = getV2rayCong()
            v2rayPoint.domainName="my.hellodear.online:443"
//                v2rayPoint.domainName="hey.hellodear.online:443"
//                v2rayPoint.domainName="94.182.190.234:995"
            try {
                v2rayPoint.runLoop(false)
            } catch (e: Exception) {
                Log.d("com.example.c137", e.toString())
            }
            Toast.makeText(this, v2rayPoint.isRunning.toString(), Toast.LENGTH_SHORT).show()
            showNotification()
        }
        else {
            v2rayPoint.stopLoop()
            Toast.makeText(this, v2rayPoint.isRunning.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startV2ray()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun showNotification() {
        val startMainIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(this,
            NOTIFICATION_PENDING_INTENT_CONTENT, startMainIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            })

        val stopV2RayIntent = Intent(AppConfig.BROADCAST_ACTION_SERVICE)
        stopV2RayIntent.`package` = AppConfig.ANG_PACKAGE
        stopV2RayIntent.putExtra("key", AppConfig.MSG_STATE_STOP)

        val stopV2RayPendingIntent = PendingIntent.getBroadcast(this,
            NOTIFICATION_PENDING_INTENT_STOP_V2RAY, stopV2RayIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            })

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        mBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("hey!")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)
//            .addAction(R.drawable.ic_close_grey_800_24dp,
//                service.getString(R.string.notification_action_stop_v2ray),
//                stopV2RayPendingIntent)
//        .build()

        //mBuilder?.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)  //取消震动,铃声其他都不好使
        this.startForeground(NOTIFICATION_ID, mBuilder?.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "RAY_NG_M_CH_ID"
        val channelName = "V2rayNG Background Service"
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH)
        chan.lightColor = Color.DKGRAY
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getNotificationManager()?.createNotificationChannel(chan)
        return channelId
    }

    private fun getNotificationManager(): NotificationManager? {
        if (mNotificationManager == null) {
            mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return mNotificationManager
    }
}