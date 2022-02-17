package com.junerver.baidumapdemo

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.util.regex.Pattern


fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun View.gone() {
    this.visibility = View.GONE
}

fun gones(vararg views: View) {
    views.forEach {
        it.gone()
    }
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun invisibles(vararg views: View) {
    views.forEach {
        it.invisible()
    }
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun visibles(vararg views: View) {
    views.forEach {
        it.visible()
    }
}

fun <T> T.postUI(action: () -> Unit) {

    // Fragment
    if (this is Fragment) {
        val fragment = this
        if (!fragment.isAdded) return

        val activity = fragment.activity ?: return
        if (activity.isFinishing) return

        activity.runOnUiThread(action)
        return
    }

    // Activity
    if (this is Activity) {
        if (this.isFinishing) return

        this.runOnUiThread(action)
        return
    }

    // 主线程
    if (Looper.getMainLooper() === Looper.myLooper()) {
        action()
        return
    }

    // 子线程，使用handler
    KitUtil.handler.post { action() }
}

object KitUtil {
    val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
}
