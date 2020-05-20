package pl.sokolowskibartlomiej.languagesar.utils

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.net.ConnectivityManager
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import pl.sokolowskibartlomiej.languagesar.R
import java.util.*
import kotlin.math.min

// region STRING
fun String.similarity(s2: String): Double {
    val longer = if (this.length >= s2.length) this else s2
    val shorter = if (this.length < s2.length) this else s2
    if (longer.isEmpty()) return 1.0
    return (longer.length - editDistance(
        longer.toLowerCase(Locale.ROOT), shorter.toLowerCase(Locale.ROOT)
    )) / longer.length.toDouble()
}

private fun editDistance(s1: String, s2: String): Int {
    val costs = IntArray(s2.length + 1)
    for (i in 0..s1.length) {
        var lastValue = i
        for (j in 0..s2.length) {
            if (i == 0) costs[j] = j
            else if (j > 0) {
                var newValue = costs[j - 1]
                if (s1[i - 1] != s2[j - 1]) newValue = min(min(newValue, lastValue), costs[j]) + 1
                costs[j - 1] = lastValue
                lastValue = newValue
            }
        }
        if (i > 0) costs[s2.length] = lastValue
    }
    return costs[s2.length]
}
// endregion STRING

// region CONTEXT
fun Context.showBasicAlertDialog(titleId: Int?, messageId: Int) {
    val alertDialog = AlertDialog.Builder(this)
        .setMessage(messageId)
        .setCancelable(false)
        .setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
    titleId?.let { alertDialog.setTitle(titleId) }
    alertDialog.show()
}

fun Context.showShortToast(messageId: Int) =
    Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show()
// endregion CONTEXT

// region ACTIVITY
fun Activity.showNoInternetDialogWithTryAgain(
    function: () -> Unit, functionCancel: () -> Unit
): Unit =
    AlertDialog.Builder(this)
        .setTitle(R.string.no_internet_title)
        .setMessage(R.string.no_internet_reconnect_message)
        .setCancelable(false)
        .setPositiveButton(R.string.try_again) { dialog, _ ->
            dialog.dismiss()
            if (checkNetworkConnection()) function()
            else if (!isFinishing && !isDestroyed)
                showNoInternetDialogWithTryAgain(function, functionCancel)
        }
        .setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
            functionCancel()
        }
        .create()
        .show()

@Suppress("DEPRECATION")
fun Activity.checkNetworkConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities =
            connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities != null
    } else {
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

fun Activity.tryToRunFunctionOnInternet(function: () -> Unit, functionCancel: () -> Unit) {
    if (checkNetworkConnection())
        try {
            function()
        } catch (exc: Throwable) {
            showNoInternetDialogWithTryAgain(function, functionCancel)
        }
    else showNoInternetDialogWithTryAgain(function, functionCancel)
}
// endregion ACTIVITY

// region VIEW
fun View.getChildViewByName(name: String): View =
    findViewById(resources.getIdentifier(name, "id", context.packageName))

fun View.rotate(rotation: Float) {
    this.animate()
        .rotation(rotation)
        .duration = 200
}

@SuppressLint("ObjectAnimatorBinding")
fun View.animateBackgroundTintChange(startColor: Int, endColor: Int) {
    val animator =
        ObjectAnimator.ofInt(this, "backgroundTint", startColor, endColor)
    animator
        .setDuration(300)
        .setEvaluator(ArgbEvaluator())
    animator.addUpdateListener { anim ->
        backgroundTintList = ColorStateList.valueOf(anim.animatedValue as Int)
    }
    animator.start()
}
// endregion VIEW

// region IMAGE
fun Image.toBitmap(rotation: Float): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix()
    matrix.postRotate(rotation)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
// endregion IMAGE