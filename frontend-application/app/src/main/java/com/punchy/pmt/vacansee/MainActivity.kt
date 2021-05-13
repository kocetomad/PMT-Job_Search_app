package com.punchy.pmt.vacansee

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity


/**
 * @param activity
 * @return Returns true if there is network connectivity
 */
fun checkWIFI(activity: Context?): Boolean {
    val cm = activity!!
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = cm.activeNetworkInfo
    return if (netInfo != null && netInfo.isConnectedOrConnecting) {
        true
    } else if (netInfo != null
        && (netInfo.state == NetworkInfo.State.DISCONNECTED || netInfo.state == NetworkInfo.State.DISCONNECTING || netInfo.state == NetworkInfo.State.SUSPENDED || netInfo
            .state == NetworkInfo.State.UNKNOWN)
    ) {
        false
    } else {
        false
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}