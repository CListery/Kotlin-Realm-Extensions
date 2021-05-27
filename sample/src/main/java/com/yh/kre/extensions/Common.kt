package com.yh.kre.extensions

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun Activity.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Activity.wait(segs: Int, closure: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Handler().postDelayed(closure, segs.toLong() * 1000)
    } else {
        Thread.sleep(segs.toLong() * 1000)
        closure()
    }
}

fun Activity.isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()
