package com.bromag.launcher

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(8, 11, 18)
    private val panel = Color.rgb(18, 23, 34)
    private val accent = Color.rgb(0, 229, 190)
    private val muted = Color.rgb(155, 169, 188)
    private lateinit var root: LinearLayout
    private lateinit var appArea: LinearLayout
    private lateinit var search: EditText
    private var allApps: List<AppEntry> = emptyList()
    private var showAll = false

    data class AppEntry(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        buildHome()
        loadApps()
        renderApps("")
    }

    override fun onResume() {
        super.onResume()
        if (::root.isInitialized) {
            loadApps()
            if (::search.isInitialized) renderApps(search.text.toString())
        }
    }

    private fun buildHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(26), dp(20), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(bg, Color.rgb(10, 18, 29), bg)
            )
        }
        setContentView(root)

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(text("BROMAG", 25f, Color.WHITE, true))
        brand.addView(text("HOME LAUNCHER  •  BETA", 10f, accent, true))
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val menu = button("⋮", panel, Color.WHITE) { showMenu() }
        top.addView(menu, LinearLayout.LayoutParams(dp(46), dp(46)))
        root.addView(top)

        val clockBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(34), 0, dp(26))
        }
        clockBlock.addView(text(
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            52f, Color.WHITE, false
        ))
        clockBlock.addView(text(
            SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
            14f, muted, false
        ).apply { setPadding(0, dp(2), 0, 0) })
        root.addView(clockBlock)

        val status = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(panel, 18)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        status.addView(text("◈", 18f, accent, true))
        status.addView(text("  YOUR SPACE. YOUR RULES.", 11f, Color.WHITE, true),
            LinearLayout.LayoutParams(0, -2, 1f))
        val battery = (getSystemService(BATTERY_SERVICE) as android.os.BatteryManager)
            .getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        status.addView(text("$battery%", 12f, accent, true))
        val statusLp = LinearLayout.LayoutParams(-1, -2)
        statusLp.bottomMargin = dp(22)
        root.addView(status, statusLp)

        search = EditText(this).apply {
            hint = "Search apps…"
            setHintTextColor(muted)
            setTextColor(Color.WHITE)
            textSize = 15f
            singleLine = true
            setPadding(dp(16), 0, dp(16), 0)
            background = rounded(panel, 16)
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    showAll = true
                    renderApps(s?.toString().orEmpty())
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        val searchLp = LinearLayout.LayoutParams(-1, dp(52))
        searchLp.bottomMargin = dp(18)
        root.addView(search, searchLp)

        val appHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        appHeader.addView(text("APPS", 12f, muted, true), LinearLayout.LayoutParams(0, -2, 1f))
        appHeader.addView(button("ALL APPS  ↗", panel, accent) {
            showAll = !showAll
            renderApps(search.text.toString())
        }.apply { textSize = 10f })
        val headerLp = LinearLayout.LayoutParams(-1, -2)
        headerLp.bottomMargin = dp(12)
        root.addView(appHeader, headerLp)

        appArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(appArea, LinearLayout.LayoutParams(-1, 0, 1f))

        val footer = text("BROMAG  /  BUILT FOR YOUR FLOW", 10f, muted, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }
        root.addView(footer)
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        allApps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { ri ->
                val ai = ri.activityInfo ?: return@mapNotNull null
                if (ai.packageName == packageName) return@mapNotNull null
                try {
                    AppEntry(
                        ri.loadLabel(pm).toString(),
                        ai.packageName,
                        ri.loadIcon(pm)
                    )
                } catch (_: Exception) { null }
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun renderApps(query: String) {
        if (!::appArea.isInitialized) return
        appArea.removeAllViews()
        val filtered = allApps.filter {
            it.label.contains(query.trim(), ignoreCase = true) ||
                it.packageName.contains(query.trim(), ignoreCase = true)
        }
        val shown = if (showAll || query.isNotBlank()) filtered else filtered.take(8)
        if (shown.isEmpty()) {
            appArea.addView(text("No apps found. Try another search.", 14f, muted, false))
            return
        }

        val columns = 4
        shown.chunked(columns).forEach { rowApps ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
            }
            rowApps.forEach { app ->
                val item = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(2), dp(7), dp(2), dp(10))
                    isClickable = true
                    isFocusable = true
                    setOnClickListener { launchApp(app.packageName) }
                    setOnLongClickListener {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(app.label)
                            .setItems(arrayOf("Open app", "App info")) { _, which ->
                                if (which == 0) launchApp(app.packageName) else openAppInfo(app.packageName)
                            }.show()
                        true
                    }
                }
                val iconView = ImageView(this).apply {
                    setImageDrawable(app.icon)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    background = rounded(panel, 18)
                    setPadding(dp(10), dp(10), dp(10), dp(10))
                }
                item.addView(iconView, LinearLayout.LayoutParams(dp(54), dp(54)))
                item.addView(text(app.label, 10f, Color.WHITE, false).apply {
                    gravity = Gravity.CENTER
                    maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    setPadding(0, dp(7), 0, 0)
                }, LinearLayout.LayoutParams(-1, -2))
                row.addView(item, LinearLayout.LayoutParams(0, -2, 1f))
            }
            repeat(columns - rowApps.size) { row.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f)) }
            appArea.addView(row)
        }
    }

    private fun launchApp(pkg: String) {
        try {
            val launch = packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) startActivity(launch)
            else Toast.makeText(this, "App cannot be launched", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppInfo(pkg: String) {
        try {
            startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.parse("package:$pkg")))
        } catch (_: Exception) {}
    }

    private fun showMenu() {
        val options = arrayOf("Refresh app list", "Change wallpaper", "Launcher info", "Android Home settings")
        AlertDialog.Builder(this)
            .setTitle("Bromag controls")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { loadApps(); renderApps(search.text.toString()); Toast.makeText(this, "App list refreshed", Toast.LENGTH_SHORT).show() }
                    1 -> {
                        try { startActivity(Intent(Intent.ACTION_SET_WALLPAPER)) }
                        catch (_: Exception) { Toast.makeText(this, "Wallpaper picker unavailable", Toast.LENGTH_SHORT).show() }
                    }
                    2 -> AlertDialog.Builder(this)
                        .setTitle("Bromag Home Launcher")
                        .setMessage("Version 0.1.0 • Cyber-premium starter build\n\nMade for customization and daily use. More features can be added in later builds.")
                        .setPositiveButton("OK", null).show()
                    3 -> {
                        try { startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS)) }
                        catch (_: Exception) { Toast.makeText(this, "Open Settings → Apps → Default apps → Home app", Toast.LENGTH_LONG).show() }
                    }
                }
            }.show()
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = if (bold) Typeface.create("sans-serif", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL)
        }

    private fun button(label: String, color: Int, textColor: Int, action: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 17f
            setTextColor(textColor)
            background = rounded(color, 14)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

    private fun rounded(color: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
