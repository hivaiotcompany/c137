package com.example.c137

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.text.TextUtils
import com.google.gson.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import go.Seq
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import libv2ray.V2RayVPNServiceSupportsSet
import java.lang.ref.SoftReference
import java.net.URL
import java.net.HttpURLConnection

interface ServiceControl {
    fun getService(): Service

    fun startService()

    fun stopService()

    fun vpnProtect(socket: Int): Boolean
}
class MainActivity : AppCompatActivity() {
//    var serviceControl: SoftReference<ServiceControl>? = null
//        set(value) {
//            Seq.setContext(value?.get()?.getService()?.applicationContext)
//            Libv2ray.initV2Env(Utils.userAssetPath(value?.get()?.getService()))
//        }


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


    fun userAssetPath(context: Context?): String {
        if (context == null)
            return ""
        val extDir = context.getExternalFilesDir("assets")
            ?: return context.getDir("assets", 0).absolutePath
        return extDir.absolutePath
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

            Seq.setContext(this)
            Libv2ray.initV2Env(userAssetPath(this))

        findViewById<Button>(R.id.button).setOnClickListener{
            val intent = Intent(applicationContext,V2rayService::class.java)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                this.startForegroundService(intent)
            } else {
                this.startService(intent)
            }
        }

//        val v2rayPoint: V2RayPoint = Libv2ray.newV2RayPoint(V2RayCallback(), Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
//
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//
//        findViewById<Button>(R.id.button).setOnClickListener{
//            if (!v2rayPoint.isRunning) {
//                v2rayPoint.configureFileContent = getV2rayCong()
//                v2rayPoint.domainName="my.hellodear.online:443"
////                v2rayPoint.domainName="hey.hellodear.online:443"
////                v2rayPoint.domainName="94.182.190.234:995"
//                try {
//                    v2rayPoint.runLoop(false)
//                } catch (e: Exception) {
//                    Log.d("com.example.c137", e.toString())
//                }
//                Toast.makeText(this, v2rayPoint.isRunning.toString(), Toast.LENGTH_SHORT).show()
//            }
//            else {
//                v2rayPoint.stopLoop()
//                Toast.makeText(this, v2rayPoint.isRunning.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
    }
}