package com.example.c137

import android.util.Log
import libv2ray.V2RayVPNServiceSupportsSet

object Callback {
    class V2RayCallback : V2RayVPNServiceSupportsSet {
        override fun shutdown(): Long {
//            val serviceControl = serviceControl?.get() ?: return -1
//            // called by go
//            return try {
//                serviceControl.stopService()
//                0
//            } catch (e: Exception) {
//                Log.d(ANG_PACKAGE, e.toString())
//                -1
//            }
            return 0
        }

        override fun prepare(): Long {
            return 0
        }

        override fun protect(l: Long): Boolean {
            val serviceControl = V2RayServiceManager.serviceControl?.get() ?:return false
            Log.d("com.example.v2ray","protect" +l.toString())
            return serviceControl.vpnProtect(l.toInt())
            return true
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            //Logger.d(s)
            return 0
        }

        override fun setup(s: String): Long {
            val serviceControl = V2RayServiceManager.serviceControl?.get() ?:return-1
            Log.d("com.example.v2ray", s)
            return try {
                serviceControl.startService()
//                lastQueryTime = System.currentTimeMillis()
//                startSpeedNotification()
                0
            } catch (e: Exception) {
                Log.d("com.example.v2ray", e.toString())
                -1
            }
        }
    }
}
