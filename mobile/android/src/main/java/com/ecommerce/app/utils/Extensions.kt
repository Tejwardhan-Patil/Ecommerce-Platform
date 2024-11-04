package com.ecommerce.app.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

// Function to convert dp to px
fun Int.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}

// Function to check network connectivity
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnected
    }
}

// Function to hide keyboard
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

// Function to show keyboard
fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

// Function to make view visible
fun View.show() {
    visibility = View.VISIBLE
}

// Function to make view gone
fun View.hide() {
    visibility = View.GONE
}

// Function to set a tint on an ImageView
fun ImageView.setTint(@ColorInt color: Int) {
    DrawableCompat.setTint(DrawableCompat.wrap(drawable), color)
}

// Function to load image using Glide
fun ImageView.loadImage(url: String?) {
    Glide.with(this.context)
        .load(url)
        .into(this)
}

// Function to show a Snackbar message
fun View.showSnackbar(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, length).show()
}

// Function to disable a button
fun AppCompatButton.disable() {
    isEnabled = false
    alpha = 0.5f
}

// Function to enable a button
fun AppCompatButton.enable() {
    isEnabled = true
    alpha = 1f
}

// Function for EditText to add a text watcher
fun EditText.onTextChanged(onTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(s.toString())
        }
    })
}

// Function to get color from resources
@ColorInt
fun Context.getColorCompat(colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}

// Function to set a custom drawable for a TextView
fun TextView.setDrawable(
    @DrawableRes drawableRes: Int,
    position: DrawablePosition = DrawablePosition.LEFT
) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    when (position) {
        DrawablePosition.LEFT -> setCompoundDrawables(drawable, null, null, null)
        DrawablePosition.TOP -> setCompoundDrawables(null, drawable, null, null)
        DrawablePosition.RIGHT -> setCompoundDrawables(null, null, drawable, null)
        DrawablePosition.BOTTOM -> setCompoundDrawables(null, null, null, drawable)
    }
}

// Enum class for drawable position
enum class DrawablePosition {
    LEFT, TOP, RIGHT, BOTTOM
}

// Function to convert color hex code to ColorInt
@ColorInt
fun String.toColorInt(): Int {
    return Color.parseColor(this)
}

// Function to format a price value
fun Double.formatAsPrice(currencySymbol: String = "$"): String {
    return String.format("%s%.2f", currencySymbol, this)
}

// Function to convert TextView text to String
fun TextView.getString(): String {
    return text.toString()
}

// Function to check if a String is a valid email
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Function to clear text in an EditText
fun EditText.clear() {
    text.clear()
}

// Function to add click listener with debounce
fun View.setDebouncedClickListener(debounceTime: Long = 600L, action: () -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= debounceTime) {
                lastClickTime = currentTime
                action()
            }
        }
    })
}

// Function to capitalize the first letter of a string
fun String.capitalizeFirstLetter(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

// Function to convert dp to sp
fun Int.toSp(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}

// Function to check if a string contains only digits
fun String.isDigitsOnly(): Boolean {
    return this.matches(Regex("\\d+"))
}