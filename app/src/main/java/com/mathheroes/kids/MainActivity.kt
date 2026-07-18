package com.mathheroes.kids

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.icu.text.BreakIterator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : Activity() {

    // ---------- main palette ----------
    private val cBg = Color.parseColor("#FFF6E9")
    private val cPink = Color.parseColor("#FF6FA5")
    private val cPurple = Color.parseColor("#9B6FE8")
    private val cBlue = Color.parseColor("#4FA9F5")
    private val cGreen = Color.parseColor("#3FBF7F")
    private val cOrange = Color.parseColor("#FFA43B")
    private val cRed = Color.parseColor("#F45B5B")
    private val cYellow = Color.parseColor("#FFD34E")
    private val cDark = Color.parseColor("#4A3B62")
    private val cGrey = Color.parseColor("#D9D2E9")
    private val cGold = Color.parseColor("#E8A93B")

    // ---------- pastel palette for the profile form ----------
    private val pRedHead = Color.parseColor("#E57373");  private val pRedBg = Color.parseColor("#FCE8E6")
    private val pOrangeHead = Color.parseColor("#F0A15F"); private val pOrangeBg = Color.parseColor("#FDEEDF")
    private val pYellowHead = Color.parseColor("#DDB84A"); private val pYellowBg = Color.parseColor("#FBF3D9")
    private val pGreenHead = Color.parseColor("#77C08F"); private val pGreenBg = Color.parseColor("#E5F4EA")
    private val pBlueHead = Color.parseColor("#7DA7E8");  private val pBlueBg = Color.parseColor("#E7EFFC")

    // ---------- levels ----------
    enum class Level(val label: String, val emoji: String, val color: Int) {
        EASY("Easy", "🌱", Color.parseColor("#3FBF7F")),
        NORMAL("Normal", "⭐", Color.parseColor("#4FA9F5")),
        HERO("Hero", "🦸", Color.parseColor("#FFA43B")),
        LEGEND("Legend", "👑", Color.parseColor("#F45B5B"))
    }

    // ---------- session configuration ----------
    private val ops = linkedSetOf<Char>()            // '+', '-', 'x', '/'
    private var addMax = 100
    private var subMax = 100
    private val timesTables = sortedSetOf<Int>()
    private val divTables = sortedSetOf<Int>()
    private var timerSec = 60
    private var chosenLevel = Level.NORMAL

    // ---------- active profile ----------
    private var activeProfileId: String = ""
    private var activeName: String = ""
    private var activeEmoji: String = "🙂"

    // ---------- challenge state ----------
    private var timer: CountDownTimer? = null
    private var correct = 0
    private var attempted = 0
    private val correctByOp = mutableMapOf<Char, Int>()
    private var onHomeScreen = false
    private val handler = Handler(Looper.getMainLooper())

    // ---------- emoji blocklist for profile logo (best-effort) ----------
    private val emojiBlocklist = setOf(
        "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐨","🐯","🦁","🐮","🐷","🐸","🐵","🐔","🐧",
        "🐦","🐤","🦆","🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝","🐛","🦋","🐌","🐞","🐜","🕷️",
        "🦂","🐢","🐍","🦎","🦖","🦕","🐙","🦑","🦐","🦞","🦀","🐡","🐠","🐟","🐬","🐳","🐋",
        "🦈","🐊","🐅","🐆","🦓","🦍","🦧","🐘","🦛","🦏","🐪","🐫","🦒","🦘","🐃","🐂","🐄",
        "🐎","🐖","🐏","🐑","🦙","🐐","🦌","🐕","🐩","🐈","🐓","🦃","🦚","🦜","🦢","🐇","🦝",
        "🦡","🦫","🦥","🐁","🐀","🐿️","🦔","🐾",
        "🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝",
        "🍅","🍆","🥑","🥦","🥬","🥒","🌶️","🫑","🌽","🥕","🥔","🍠","🥐","🥯","🍞","🥖","🥨",
        "🧀","🥚","🍳","🥞","🧇","🥓","🍗","🍖","🌭","🍔","🍟","🍕","🥪","🌮","🌯","🥗","🍝",
        "🍜","🍲","🍣","🍱","🍤","🍙","🍚","🍥","🍢","🍧","🍨","🍦","🧁","🍰","🎂","🍭","🍬",
        "🍫","🍿","🍩","🍪","🥜","🍯","🥛","🍼","☕","🍵","🍺","🍷",
        "🧸","🪀","🪁","🎈","🎲","🎮","🕹️","🪃","🪅","🎯","🧩","🪄","🎳","🏀","⚽","🏈","⚾",
        "🎾","🏐","🎱","🛹","🛼","🛴","🚲"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showProfileSelect()
    }

    override fun onDestroy() {
        timer?.cancel()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (onHomeScreen) {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        } else {
            timer?.cancel()
            showProfileSelect()
        }
    }

    // ============================================================
    //  Small UI helpers
    // ============================================================

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radiusDp: Int = 22): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
        }

    private fun fullWidth(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun space(h: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(h))
    }

    private fun hspace(w: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(dp(w), 1)
    }

    private fun label(text: String, size: Float = 34f, color: Int = cDark): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            setTypeface(Typeface.DEFAULT_BOLD)
            gravity = Gravity.CENTER
        }

    private fun bigButton(text: String, color: Int, onClick: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            textSize = 22f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT_BOLD)
            background = rounded(color)
            stateListAnimator = null
            elevation = dp(3).toFloat()
            setPadding(dp(20), dp(16), dp(20), dp(16))
            setOnClickListener { onClick() }
        }

    private fun smallButton(text: String, color: Int, onClick: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            textSize = 15f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT_BOLD)
            background = rounded(color, 16)
            stateListAnimator = null
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setOnClickListener { onClick() }
        }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun scaffold(
        showClose: Boolean,
        scrollable: Boolean = true,
        onClose: (() -> Unit)? = null
    ): LinearLayout {
        val frame = FrameLayout(this).apply { setBackgroundColor(cBg) }

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(56), dp(24), dp(24))
        }

        if (scrollable) {
            val scroll = ScrollView(this).apply {
                isFillViewport = true
                addView(
                    col,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
            frame.addView(
                scroll,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        } else {
            frame.addView(
                col,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        if (showClose) {
            val x = TextView(this).apply {
                text = "✕"
                textSize = 20f
                setTextColor(Color.WHITE)
                setTypeface(Typeface.DEFAULT_BOLD)
                gravity = Gravity.CENTER
                background = rounded(cRed, 40)
                setOnClickListener {
                    timer?.cancel()
                    onClose?.invoke() ?: showProfileSelect()
                }
            }
            val lp = FrameLayout.LayoutParams(dp(44), dp(44)).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dp(14)
                rightMargin = dp(14)
            }
            frame.addView(x, lp)
        }

        setContentView(frame)
        onHomeScreen = false
        return col
    }

    // ============================================================
    //  Emoji helpers
    // ============================================================

    private fun firstGrapheme(s: String): String {
        if (s.isEmpty()) return s
        val bi = BreakIterator.getCharacterInstance()
        bi.setText(s)
        val end = bi.next()
        return if (end == BreakIterator.DONE) s else s.substring(0, end)
    }

    private fun enforceSingleEmoji(e: EditText, blocked: Set<String>? = null, warnMsg: String = "") {
        e.addTextChangedListener(object : TextWatcher {
            var guard = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (guard) return
                guard = true
                val text = s.toString()
                var trimmed = firstGrapheme(text)
                if (blocked != null && blocked.contains(trimmed)) {
                    toast(warnMsg)
                    trimmed = ""
                }
                if (trimmed != text) {
                    e.setText(trimmed)
                    e.setSelection(trimmed.length)
                }
                guard = false
            }
        })
    }

    private fun letterFilter(): InputFilter =
        InputFilter { source, start, end, _, _, _ ->
            val sb = StringBuilder()
            for (i in start until end) {
                val c = source[i]
                if (Character.isLetter(c)) sb.append(c)
            }
            if (sb.length == end - start) null else sb
        }

    // ============================================================
    //  Storage
    // ============================================================

    private fun prefs() = getSharedPreferences("math_heroes_v2", MODE_PRIVATE)

    private fun loadProfiles(): JSONArray = JSONArray(prefs().getString("profiles", "[]"))
    private fun saveProfiles(arr: JSONArray) = prefs().edit().putString("profiles", arr.toString()).apply()

    private fun addProfile(o: JSONObject) {
        val arr = loadProfiles()
        arr.put(o)
        saveProfiles(arr)
    }

    private fun deleteProfileById(id: String) {
        val arr = loadProfiles()
        val fresh = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") != id) fresh.put(o)
        }
        saveProfiles(fresh)
        prefs().edit().remove("history_$id").apply()
    }

    private fun findProfile(id: String): JSONObject? {
        val arr = loadProfiles()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") == id) return o
        }
        return null
    }

    private fun updateProfile(id: String, mutate: (JSONObject) -> Unit) {
        val arr = loadProfiles()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") == id) mutate(o)
        }
        saveProfiles(arr)
    }

    private fun addTokensToProfile(id: String, delta: Int) =
        updateProfile(id) { it.put("tokens", it.optInt("tokens", 0) + delta) }

    private fun isBirthdayToday(o: JSONObject): Boolean {
        val cal = Calendar.getInstance()
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH) + 1
        return o.optInt("bday") == d && o.optInt("bmonth") == m
    }

    // ============================================================
    //  Screen — Profile select (home)
    // ============================================================

    private fun showProfileSelect() {
        timer?.cancel()
        val col = scaffold(showClose = false)
        onHomeScreen = true

        col.addView(label("🌟 Math Legends 🌟", 30f))
        col.addView(space(4))
        col.addView(label("Who is playing today?", 16f, cPurple))
        col.addView(space(20))

        val profiles = loadProfiles()

        if (profiles.length() == 0) {
            col.addView(label("No legends yet!", 18f, cDark))
            col.addView(space(6))
            col.addView(label("Create your first profile to start 🚀", 14f, cPurple))
            col.addView(space(20))
        }

        for (i in 0 until profiles.length()) {
            val o = profiles.getJSONObject(i)
            val id = o.getString("id")
            val name = o.getString("name")
            val emoji = o.optString("logo", "🙂")
            val tokens = o.optInt("tokens", 0)
            val bday = isBirthdayToday(o)

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = rounded(Color.WHITE, 18)
                setPadding(dp(14), dp(12), dp(14), dp(12))
                isClickable = true
                setOnClickListener {
                    activeProfileId = id
                    activeName = name
                    activeEmoji = emoji
                    if (bday) toast("🎉 Happy Birthday, $name! 🎉")
                    showLevelPick()
                }
            }

            // ---- top row: logo + name + tokens ----
            val topRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            topRow.addView(TextView(this).apply {
                text = emoji
                textSize = 28f
                gravity = Gravity.CENTER
                background = rounded(cGrey, 26)
            }, LinearLayout.LayoutParams(dp(52), dp(52)))
            topRow.addView(hspace(12))
            topRow.addView(TextView(this).apply {
                text = if (bday) "$name 🎂" else name
                textSize = 21f
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextColor(cDark)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            topRow.addView(TextView(this).apply {
                text = "🪙 $tokens"
                textSize = 15f
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextColor(cGold)
            })
            card.addView(topRow, fullWidth())

            card.addView(space(10))

            // ---- bottom row: Games / Stats / Manage ----
            val btnRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            fun rowBtn(text: String, color: Int, onClick: () -> Unit): Button =
                smallButton(text, color, onClick)
            val lpBtn = { LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dp(4), 0, dp(4), 0)
            } }
            btnRow.addView(rowBtn("🎮 Games", cPurple) { showGames(id) }, lpBtn())
            btnRow.addView(rowBtn("📊 Stats", cBlue) { showProgress(id, name) }, lpBtn())
            btnRow.addView(rowBtn("⚙️ Manage", cGrey.let { Color.parseColor("#8A7FA8") }) { showManage(id) }, lpBtn())
            card.addView(btnRow, fullWidth())

            col.addView(card, fullWidth().apply { bottomMargin = dp(12) })
        }

        col.addView(space(10))
        col.addView(bigButton("➕ Add a new legend", cPurple) { showCreateProfile() }, fullWidth())
    }

    // ============================================================
    //  Pastel form building blocks
    // ============================================================

    private fun pastelCard(bgColor: Int, headColor: Int, headText: String): LinearLayout {
        val cardCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(bgColor, 18)
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        cardCol.addView(TextView(this).apply {
            text = headText
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT_BOLD)
            background = rounded(headColor, 12)
            setPadding(dp(14), dp(8), dp(14), dp(8))
        }, fullWidth())
        cardCol.addView(space(10))
        return cardCol
    }

    private fun textField(hint: String, maxLen: Int, filters: List<InputFilter> = emptyList()): EditText =
        EditText(this).apply {
            this.hint = hint
            textSize = 18f
            setTextColor(cDark)
            background = rounded(Color.WHITE, 14)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            this.filters = (filters + InputFilter.LengthFilter(maxLen)).toTypedArray()
        }

    private fun emojiField(hint: String): EditText =
        EditText(this).apply {
            this.hint = hint
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(cDark)
            background = rounded(Color.WHITE, 14)
            setPadding(dp(16), dp(10), dp(16), dp(10))
            inputType = InputType.TYPE_CLASS_TEXT
        }

    // ============================================================
    //  Screen — Create / Manage profile (shared form)
    // ============================================================

    private fun showCreateProfile() = showProfileForm(null)
    private fun showManage(id: String) = showProfileForm(id)

    private fun showProfileForm(editId: String?) {
        val existing = editId?.let { findProfile(it) }
        val col = scaffold(showClose = true)

        col.addView(label(if (existing == null) "Create a new legend! 🦸" else "Manage profile ⚙️", 23f))
        col.addView(space(18))

        // ---- name + logo (reddish) ----
        val nameCard = pastelCard(pRedBg, pRedHead, "Name & logo")
        val nameField = textField("Name (letters only, max 10)", 10, listOf(letterFilter()))
        existing?.let { nameField.setText(it.getString("name")) }
        nameCard.addView(nameField, fullWidth())
        nameCard.addView(space(8))
        val logoField = emojiField("Logo emoji (not animal/food/toy)")
        existing?.let { logoField.setText(it.optString("logo", "")) }
        enforceSingleEmoji(logoField, emojiBlocklist, "That one's saved for your favourites! Try a different emoji 😊")
        nameCard.addView(logoField, fullWidth())
        col.addView(nameCard, fullWidth())
        col.addView(space(14))

        // ---- birthday (orangish) — only at creation ----
        var dayPicker: NumberPicker? = null
        var monthPicker: NumberPicker? = null
        if (existing == null) {
            val bdayCard = pastelCard(pOrangeBg, pOrangeHead, "Birthday — 🎁 for special gifts on your special day!")
            val pickerRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            dayPicker = NumberPicker(this).apply { minValue = 1; maxValue = 31; value = 1 }
            monthPicker = NumberPicker(this).apply {
                minValue = 1; maxValue = 12; value = 1
                displayedValues = arrayOf(
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                )
            }
            pickerRow.addView(dayPicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            pickerRow.addView(monthPicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            bdayCard.addView(pickerRow, fullWidth())
            col.addView(bdayCard, fullWidth())
            col.addView(space(14))
        }

        // ---- pet (yellowish) ----
        val petCard = pastelCard(pYellowBg, pYellowHead, "Favourite pet — name + animal emoji")
        val petName = textField("Pet's name", 16)
        existing?.let { petName.setText(it.optString("petName", "")) }
        petCard.addView(petName, fullWidth())
        petCard.addView(space(8))
        val petEmoji = emojiField("Animal emoji")
        existing?.let { petEmoji.setText(it.optString("petEmoji", "")) }
        enforceSingleEmoji(petEmoji)
        petCard.addView(petEmoji, fullWidth())
        col.addView(petCard, fullWidth())
        col.addView(space(14))

        // ---- toy (greenish) ----
        val toyCard = pastelCard(pGreenBg, pGreenHead, "Favourite toy — name + toy emoji")
        val toyName = textField("Toy's name", 16)
        existing?.let { toyName.setText(it.optString("toyName", "")) }
        toyCard.addView(toyName, fullWidth())
        toyCard.addView(space(8))
        val toyEmoji = emojiField("Toy emoji")
        existing?.let { toyEmoji.setText(it.optString("toyEmoji", "")) }
        enforceSingleEmoji(toyEmoji)
        toyCard.addView(toyEmoji, fullWidth())
        col.addView(toyCard, fullWidth())
        col.addView(space(14))

        // ---- food (bluish) ----
        val foodCard = pastelCard(pBlueBg, pBlueHead, "Favourite food — name + food emoji")
        val foodName = textField("Food's name", 16)
        existing?.let { foodName.setText(it.optString("foodName", "")) }
        foodCard.addView(foodName, fullWidth())
        foodCard.addView(space(8))
        val foodEmoji = emojiField("Food emoji")
        existing?.let { foodEmoji.setText(it.optString("foodEmoji", "")) }
        enforceSingleEmoji(foodEmoji)
        foodCard.addView(foodEmoji, fullWidth())
        col.addView(foodCard, fullWidth())

        col.addView(space(22))
        col.addView(bigButton(if (existing == null) "Create profile ✔" else "Save changes ✔", cGreen) {
            val name = nameField.text.toString().trim()
            val logo = logoField.text.toString().trim()
            val pn = petName.text.toString().trim(); val pe = petEmoji.text.toString().trim()
            val tn = toyName.text.toString().trim(); val te = toyEmoji.text.toString().trim()
            val fn = foodName.text.toString().trim(); val fe = foodEmoji.text.toString().trim()

            if (name.isEmpty() || logo.isEmpty() || pn.isEmpty() || pe.isEmpty() ||
                tn.isEmpty() || te.isEmpty() || fn.isEmpty() || fe.isEmpty()
            ) {
                toast("Please fill in every field! 😊")
                return@bigButton
            }

            if (existing == null) {
                val o = JSONObject()
                    .put("id", System.currentTimeMillis().toString() + Random.nextInt(1000, 9999))
                    .put("name", name)
                    .put("logo", logo)
                    .put("bday", dayPicker!!.value)
                    .put("bmonth", monthPicker!!.value)
                    .put("petName", pn).put("petEmoji", pe)
                    .put("toyName", tn).put("toyEmoji", te)
                    .put("foodName", fn).put("foodEmoji", fe)
                    .put("tokens", 0)
                addProfile(o)
                toast("Welcome, $name! 🎉")
            } else {
                updateProfile(editId!!) {
                    it.put("name", name).put("logo", logo)
                        .put("petName", pn).put("petEmoji", pe)
                        .put("toyName", tn).put("toyEmoji", te)
                        .put("foodName", fn).put("foodEmoji", fe)
                }
                toast("Saved! ✔")
            }
            showProfileSelect()
        }, fullWidth())

        if (existing != null) {
            col.addView(space(14))
            col.addView(bigButton("🗑 Delete this profile", cRed) {
                AlertDialog.Builder(this)
                    .setTitle("Delete ${existing.getString("name")}?")
                    .setMessage("This will erase all their progress and tokens. This can't be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteProfileById(editId!!)
                        showProfileSelect()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }, fullWidth())
        }
    }

    // ============================================================
    //  Screen — Games hub
    // ============================================================

    private fun showGames(id: String) {
        val o = findProfile(id) ?: return showProfileSelect()
        val col = scaffold(showClose = true)

        col.addView(label("🎮 ${o.getString("name")}'s Games", 25f))
        col.addView(space(6))
        col.addView(label("🪙 ${o.optInt("tokens", 0)} tokens ready to spend!", 14f, cGold))
        col.addView(space(20))

        val grid = GridLayout(this).apply { columnCount = 2 }

        data class GameDef(val emoji: String, val title: String, val color: Int, val locked: Boolean)
        val games = listOf(
            GameDef(o.optString("petEmoji", "🐾"), "Save my Pet", cGreen, false),
            GameDef(o.optString("toyEmoji", "🧸"), "Find my Toy", cPink, false),
            GameDef(o.optString("foodEmoji", "🍳"), "Math Chief", cOrange, false),
            GameDef("🔒", "Mystery", Color.parseColor("#9E96B5"), true)
        )

        for (g in games) {
            val b = Button(this).apply {
                text = "${g.emoji}\n${g.title}"
                textSize = 17f
                isAllCaps = false
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextColor(Color.WHITE)
                stateListAnimator = null
                background = rounded(g.color, 18)
                setOnClickListener {
                    if (g.locked) toast("🔒 Locked! Finish the 3 games to unlock the Mystery…")
                    else showUnderConstruction(g.emoji, g.title)
                }
            }
            val lp = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(110)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }
            grid.addView(b, lp)
        }
        col.addView(grid, fullWidth())

        col.addView(space(20))
        col.addView(bigButton("🏠 Home", cPurple) { showProfileSelect() }, fullWidth())
    }

    private fun showUnderConstruction(emoji: String, title: String) {
        val col = scaffold(showClose = true)
        col.gravity = Gravity.CENTER

        col.addView(label("🚧", 64f))
        col.addView(space(10))
        col.addView(label("$emoji $title", 26f))
        col.addView(space(8))
        col.addView(label("Under construction — coming soon!", 16f, cPurple))
        col.addView(space(30))
        col.addView(bigButton("◀ Back", cBlue) { showProfileSelect() }, fullWidth())
    }

    // ============================================================
    //  Math flow — 1) Level pick
    // ============================================================

    private fun timesTablesFor(level: Level): List<Int> = when (level) {
        Level.EASY -> listOf(1, 10, 11)
        Level.NORMAL -> listOf(1, 10, 11, 2, 5, 4)
        Level.HERO -> listOf(1, 10, 11, 2, 5, 4, 3, 9, 6)
        Level.LEGEND -> listOf(1, 10, 11, 2, 5, 4, 3, 9, 6, 7, 8, 12)
    }

    private fun defaultMaxFor(level: Level): Int = when (level) {
        Level.EASY -> 20
        Level.NORMAL -> 100
        Level.HERO -> 200
        Level.LEGEND -> 1000
    }

    private fun showLevelPick() {
        val col = scaffold(showClose = true)
        col.gravity = Gravity.CENTER

        col.addView(label("Choose your level", 24f))
        col.addView(space(10))
        col.addView(label("Harder levels earn more tokens per correct answer!", 13f, cPurple))
        col.addView(space(24))

        for (lvl in Level.values()) {
            col.addView(bigButton("${lvl.emoji}  ${lvl.label}", lvl.color) {
                chosenLevel = lvl
                addMax = defaultMaxFor(lvl)
                subMax = defaultMaxFor(lvl)
                timesTables.clear(); timesTables.addAll(timesTablesFor(lvl))
                divTables.clear(); divTables.addAll(timesTablesFor(lvl))
                showOperations()
            }, fullWidth())
            col.addView(space(12))
        }
    }

    // ============================================================
    //  Math flow — 2) Operations
    // ============================================================

    private fun symbolFor(op: Char): String = when (op) {
        '+' -> "+"
        '-' -> "−"
        'x' -> "×"
        else -> "÷"
    }

    private fun showOperations() {
        ops.clear()
        val col = scaffold(showClose = true)

        col.addView(label("Hi $activeName! $activeEmoji", 24f))
        col.addView(space(4))
        col.addView(label("${chosenLevel.emoji} ${chosenLevel.label} — choose your operations", 15f, cPurple))
        col.addView(space(20))

        val grid = GridLayout(this).apply { columnCount = 2 }
        val defs = listOf(
            Triple('+', "Addition", cGreen),
            Triple('-', "Subtraction", cBlue),
            Triple('x', "Times", cOrange),
            Triple('/', "Division", cPink)
        )
        for ((sym, name, color) in defs) {
            val b = Button(this).apply {
                textSize = 20f
                isAllCaps = false
                setTypeface(Typeface.DEFAULT_BOLD)
                stateListAnimator = null
                fun refresh() {
                    val on = ops.contains(sym)
                    background = rounded(if (on) color else cGrey, 18)
                    setTextColor(if (on) Color.WHITE else cDark)
                    text = "${symbolFor(sym)}  $name" + if (on) "  ✓" else ""
                }
                refresh()
                setOnClickListener {
                    if (ops.contains(sym)) ops.remove(sym) else ops.add(sym)
                    refresh()
                }
            }
            val lp = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(100)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }
            grid.addView(b, lp)
        }
        col.addView(grid, fullWidth())

        col.addView(space(26))
        col.addView(bigButton("Next  ▶", cPurple) {
            if (ops.isEmpty()) toast("Pick at least one operation! 😊")
            else showLimits()
        }, fullWidth())
    }

    // ============================================================
    //  Math flow — 3) Custom menu (pre-filled by level)
    // ============================================================

    private fun sectionLabel(text: String, color: Int): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT_BOLD)
            background = rounded(color, 12)
            setPadding(dp(14), dp(8), dp(14), dp(8))
        }

    private fun numberInput(value: Int): EditText =
        EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(4))
            setText(value.toString())
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(cDark)
            background = rounded(Color.WHITE, 14)
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

    private fun readNum(e: EditText, fallback: Int): Int =
        (e.text.toString().toIntOrNull() ?: fallback).coerceIn(0, 1000)

    private fun tableGrid(set: MutableSet<Int>, color: Int): GridLayout {
        val grid = GridLayout(this).apply { columnCount = 4 }
        for (t in 1..12) {
            val b = Button(this).apply {
                textSize = 17f
                setTypeface(Typeface.DEFAULT_BOLD)
                stateListAnimator = null
                fun refresh() {
                    val on = set.contains(t)
                    background = rounded(if (on) color else cGrey, 14)
                    setTextColor(if (on) Color.WHITE else cDark)
                    text = t.toString()
                }
                refresh()
                setOnClickListener {
                    if (set.contains(t)) set.remove(t) else set.add(t)
                    refresh()
                }
            }
            val lp = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(50)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            grid.addView(b, lp)
        }
        return grid
    }

    private fun showLimits() {
        val col = scaffold(showClose = true)

        col.addView(label("Adjust your challenge", 24f))
        col.addView(space(6))
        col.addView(label("We pre-filled everything for ${chosenLevel.label} — change what you like!", 13f, cPurple))
        col.addView(space(16))

        var addInput: EditText? = null
        var subInput: EditText? = null

        if (ops.contains('+')) {
            col.addView(sectionLabel("＋ Addition — biggest answer (max 1000)", cGreen), fullWidth())
            col.addView(space(8))
            addInput = numberInput(addMax)
            col.addView(addInput, fullWidth())
            col.addView(space(16))
        }
        if (ops.contains('-')) {
            col.addView(sectionLabel("－ Subtraction — biggest top number (max 1000)", cBlue), fullWidth())
            col.addView(space(8))
            subInput = numberInput(subMax)
            col.addView(subInput, fullWidth())
            col.addView(space(16))
        }
        if (ops.contains('x')) {
            col.addView(sectionLabel("× Times tables to practise", cOrange), fullWidth())
            col.addView(space(8))
            col.addView(tableGrid(timesTables, cOrange), fullWidth())
            col.addView(space(16))
        }
        if (ops.contains('/')) {
            col.addView(sectionLabel("÷ Division tables to practise", cPink), fullWidth())
            col.addView(space(8))
            col.addView(tableGrid(divTables, cPink), fullWidth())
            col.addView(space(16))
        }

        col.addView(space(8))
        col.addView(bigButton("Next  ▶", cPurple) {
            if (ops.contains('+')) addMax = readNum(addInput!!, addMax)
            if (ops.contains('-')) subMax = readNum(subInput!!, subMax)
            if (ops.contains('x') && timesTables.isEmpty()) {
                toast("Pick at least one times table! 😊"); return@bigButton
            }
            if (ops.contains('/') && divTables.isEmpty()) {
                toast("Pick at least one division table! 😊"); return@bigButton
            }
            showTimerPick()
        }, fullWidth())
    }

    // ============================================================
    //  Math flow — 4) Timer choice
    // ============================================================

    private fun showTimerPick() {
        val col = scaffold(showClose = true)
        col.gravity = Gravity.CENTER

        col.addView(label("⏱ How long is your challenge?", 24f))
        col.addView(space(28))

        val options = listOf(
            Triple(30, "⚡ 30 seconds", cGreen),
            Triple(60, "🚀 1 minute", cBlue),
            Triple(120, "🔥 2 minutes", cOrange),
            Triple(300, "🏆 5 minutes", cRed)
        )
        for ((sec, text, color) in options) {
            col.addView(bigButton(text, color) {
                timerSec = sec
                showChallenge()
            }, fullWidth())
            col.addView(space(14))
        }
    }

    // ============================================================
    //  Question generators — level patterns + user caps/tables
    // ============================================================

    private fun addEasy(): Pair<Int, Int> = when (Random.nextInt(4)) {
        0 -> Random.nextInt(1, 51) to 0
        1 -> Random.nextInt(0, 51) to 1
        2 -> Random.nextInt(0, 51) to 10
        else -> { val n = Random.nextInt(1, 13); n to n }
    }

    private fun addNormal(): Pair<Int, Int> = if (Random.nextBoolean()) addEasy() else when (Random.nextInt(4)) {
        0 -> Random.nextInt(0, 51) to 2
        1 -> Random.nextInt(0, 51) to 5
        2 -> { val n = Random.nextInt(2, 13); n to (n + 1) }
        else -> {
            val tens = Random.nextInt(0, 10) * 10
            val a = Random.nextInt(1, 10)
            val b = 10 - a
            (tens + a) to b
        }
    }

    private fun addHero(): Pair<Int, Int> = if (Random.nextInt(3) != 0) addNormal() else {
        val tens = if (Random.nextBoolean()) 0 else Random.nextInt(1, 10) * 10
        val units = Random.nextInt(1, 10)
        val a = tens + units
        val bMin = maxOf(2, 10 - units)
        val b = if (bMin > 9) 9 else Random.nextInt(bMin, 10)
        a to b
    }

    private fun addLegend(): Pair<Int, Int> = if (Random.nextInt(3) != 0) addHero() else {
        Random.nextInt(10, 1000) to Random.nextInt(10, 1000)
    }

    private fun subEasy(): Pair<Int, Int> = when (Random.nextInt(4)) {
        0 -> Random.nextInt(1, 51) to 0
        1 -> Random.nextInt(1, 51) to 1
        2 -> Random.nextInt(10, 100) to 10
        else -> { val n = Random.nextInt(1, 13); (n + n) to n }
    }

    private fun subNormal(): Pair<Int, Int> = if (Random.nextBoolean()) subEasy() else {
        val base = listOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100).random()
        val unitsA = Random.nextInt(2, 10)
        val a = base + unitsA
        val b = Random.nextInt(0, unitsA + 1)
        a to b
    }

    private fun subHero(): Pair<Int, Int> = if (Random.nextInt(3) != 0) subNormal() else {
        val base = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100).random()
        val unitsA = Random.nextInt(0, 9)
        val a = base + unitsA
        val b = Random.nextInt(unitsA + 1, 10)
        a to b
    }

    private fun subLegend(): Pair<Int, Int> {
        if (Random.nextInt(3) == 0) return subHero()
        repeat(15) {
            val a = Random.nextInt(20, 1000)
            val b = Random.nextInt(10, a)
            if (b % 10 > a % 10) return a to b
        }
        return subHero()
    }

    private fun genAddition(level: Level): Pair<String, Int> {
        repeat(25) {
            val (a, b) = when (level) {
                Level.EASY -> addEasy()
                Level.NORMAL -> addNormal()
                Level.HERO -> addHero()
                Level.LEGEND -> addLegend()
            }
            if (a + b <= addMax) return "$a + $b = ?" to (a + b)
        }
        val a = Random.nextInt(0, addMax + 1)
        val b = Random.nextInt(0, addMax - a + 1)
        return "$a + $b = ?" to (a + b)
    }

    private fun genSubtraction(level: Level): Pair<String, Int> {
        repeat(25) {
            var (a, b) = when (level) {
                Level.EASY -> subEasy()
                Level.NORMAL -> subNormal()
                Level.HERO -> subHero()
                Level.LEGEND -> subLegend()
            }
            if (b > a) { val t = a; a = b; b = t }
            if (a <= subMax) return "$a − $b = ?" to (a - b)
        }
        val a = Random.nextInt(0, subMax + 1)
        val b = Random.nextInt(0, a + 1)
        return "$a − $b = ?" to (a - b)
    }

    private fun genTimes(): Pair<String, Int> {
        val t = timesTables.random()
        val k = Random.nextInt(1, 13)
        return "$t × $k = ?" to (t * k)
    }

    private fun genDivision(): Pair<String, Int> {
        val d = divTables.random()
        val k = Random.nextInt(1, 13)
        return "${d * k} ÷ $d = ?" to k
    }

    private fun generateQuestion(op: Char): Pair<String, Int> = when (op) {
        '+' -> genAddition(chosenLevel)
        '-' -> genSubtraction(chosenLevel)
        'x' -> genTimes()
        else -> genDivision()
    }

    // ============================================================
    //  Token coefficients
    // ============================================================

    private fun coeffFor(op: Char, level: Level): Int = when (op) {
        '+' -> when (level) { Level.EASY -> 1; Level.NORMAL -> 2; Level.HERO -> 3; Level.LEGEND -> 5 }
        '-' -> when (level) { Level.EASY -> 1; Level.NORMAL -> 2; Level.HERO -> 4; Level.LEGEND -> 7 }
        'x' -> when (level) { Level.EASY -> 1; Level.NORMAL -> 2; Level.HERO -> 3; Level.LEGEND -> 4 }
        else -> when (level) { Level.EASY -> 1; Level.NORMAL -> 2; Level.HERO -> 4; Level.LEGEND -> 6 }
    }

    // ============================================================
    //  Screen — The challenge!
    // ============================================================

    private var currentOp: Char = '+'

    private fun showChallenge() {
        correct = 0
        attempted = 0
        correctByOp.clear()

        val col = scaffold(showClose = true, scrollable = false) {
            timer?.cancel()
            showProfileSelect()
        }
        col.setPadding(dp(20), dp(18), dp(20), dp(16))

        val timerView = label("", 19f, cRed)
        val question = label("", 38f)
        val feedback = label(" ", 19f)
        val answerView = label(" ", 32f, cPurple).apply {
            background = rounded(Color.WHITE, 16)
            minHeight = dp(58)
        }

        col.addView(timerView, fullWidth())
        col.addView(space(8))

        val qCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(cYellow, 20)
            setPadding(dp(12), dp(18), dp(12), dp(18))
            addView(question, fullWidth())
        }
        col.addView(qCard, fullWidth())
        col.addView(space(6))
        col.addView(feedback, fullWidth())
        col.addView(space(6))
        col.addView(answerView, fullWidth())

        col.addView(View(this), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        var entry = ""
        var answer = 0

        fun renderEntry() {
            answerView.text = if (entry.isEmpty()) " " else entry
        }

        fun nextQuestion() {
            val op = ops.random()
            currentOp = op
            val q = generateQuestion(op)
            question.text = q.first
            answer = q.second
            entry = ""
            renderEntry()
        }
        nextQuestion()

        fun submit() {
            val n = entry.toIntOrNull() ?: return
            attempted++
            if (n == answer) {
                correct++
                correctByOp[currentOp] = (correctByOp[currentOp] ?: 0) + 1
                feedback.text = "✅ Great job!"
                feedback.setTextColor(cGreen)
            } else {
                feedback.text = "❌ It was $answer"
                feedback.setTextColor(cRed)
            }
            handler.postDelayed({ feedback.text = " " }, 1200)
            nextQuestion()
        }

        val pad = GridLayout(this).apply { columnCount = 3 }
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "⌫")
        for (k in keys) {
            val b = Button(this).apply {
                text = k
                textSize = 22f
                isAllCaps = false
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextColor(Color.WHITE)
                stateListAnimator = null
                background = rounded(
                    when (k) {
                        "C" -> cOrange
                        "⌫" -> cPink
                        else -> cBlue
                    }, 16
                )
                setOnClickListener {
                    when (k) {
                        "C" -> { entry = ""; renderEntry() }
                        "⌫" -> {
                            if (entry.isNotEmpty()) entry = entry.dropLast(1)
                            renderEntry()
                        }
                        else -> {
                            if (entry.length < 4) { entry += k; renderEntry() }
                        }
                    }
                }
            }
            val lp = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(58)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(5), dp(5), dp(5), dp(5))
            }
            pad.addView(b, lp)
        }
        col.addView(pad, fullWidth())
        col.addView(space(8))
        col.addView(bigButton("Submit ✔", cGreen) { submit() }, fullWidth())

        timer = object : CountDownTimer(timerSec * 1000L, 250L) {
            override fun onTick(msLeft: Long) {
                val s = ((msLeft + 999) / 1000).toInt()
                timerView.text = "⏱ %d:%02d left".format(s / 60, s % 60)
            }
            override fun onFinish() {
                showResults()
            }
        }.start()
    }

    // ============================================================
    //  Screen — Results
    // ============================================================

    private fun showResults() {
        timer?.cancel()
        val pct = if (attempted == 0) 0 else correct * 100 / attempted
        val avg = if (attempted == 0) 0.0 else timerSec.toDouble() / attempted

        var rawScore = 0
        val breakdown = StringBuilder()
        for (op in ops) {
            val raw = correctByOp[op] ?: 0
            val coef = coeffFor(op, chosenLevel)
            val sub = raw * coef
            rawScore += sub
            if (ops.size > 1) breakdown.append("${symbolFor(op)}  $raw × $coef = $sub\n")
        }
        val finalScore = maxOf(0, rawScore)
        addTokensToProfile(activeProfileId, finalScore)
        saveSession(pct, avg, finalScore)

        val col = scaffold(showClose = false)
        col.gravity = Gravity.CENTER

        val headline = when {
            attempted == 0 -> "⏰ Time's up, $activeName!"
            pct >= 80 -> "🏆 Amazing, $activeName!"
            pct >= 50 -> "🌟 Well done, $activeName!"
            else -> "💪 Keep practising, $activeName!"
        }
        col.addView(label(headline, 26f))
        col.addView(space(20))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 20)
            setPadding(dp(20), dp(18), dp(20), dp(18))
        }
        card.addView(label("✅ Correct: $correct / $attempted", 19f), fullWidth())
        card.addView(space(8))
        card.addView(label("🎯 Score: $pct%", 19f), fullWidth())
        card.addView(space(8))
        val speedText = if (attempted == 0) "⚡ Speed: —"
        else "⚡ Speed: ${"%.1f".format(avg)} s per answer"
        card.addView(label(speedText, 19f), fullWidth())
        card.addView(space(8))
        card.addView(label("${chosenLevel.emoji} Level: ${chosenLevel.label}", 19f), fullWidth())
        if (breakdown.isNotEmpty()) {
            card.addView(space(10))
            card.addView(TextView(this).apply {
                text = breakdown.toString().trim()
                textSize = 14f
                setTextColor(cDark)
                gravity = Gravity.CENTER
            })
        }
        col.addView(card, fullWidth())

        col.addView(space(16))
        val tokenCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(cYellow, 18)
            setPadding(dp(18), dp(14), dp(18), dp(14))
        }
        tokenCard.addView(label("🪙 +$finalScore tokens earned!", 20f))
        val totalNow = findProfile(activeProfileId)?.optInt("tokens", 0) ?: finalScore
        tokenCard.addView(label("Wallet total: $totalNow 🪙", 14f, cDark))
        col.addView(tokenCard, fullWidth())

        col.addView(space(26))
        col.addView(bigButton("🔁 Play again", cOrange) { showChallenge() }, fullWidth())
        col.addView(space(12))
        col.addView(bigButton("📊 My progress", cBlue) { showProgress(activeProfileId, activeName) }, fullWidth())
        col.addView(space(12))
        col.addView(bigButton("🏠 Home", cPurple) { showProfileSelect() }, fullWidth())
    }

    // ============================================================
    //  Progress tracking
    // ============================================================

    private fun saveSession(pct: Int, avg: Double, tokensEarned: Int) {
        if (activeProfileId.isEmpty()) return
        val entry = JSONObject()
            .put("date", SimpleDateFormat("dd MMM yyyy HH:mm", Locale.UK).format(Date()))
            .put("ops", ops.joinToString(" ") { symbolFor(it) })
            .put("mode", chosenLevel.label)
            .put("correct", correct)
            .put("attempted", attempted)
            .put("pct", pct)
            .put("avg", avg)
            .put("dur", timerSec)
            .put("tokens", tokensEarned)
        val arr = JSONArray(prefs().getString("history_$activeProfileId", "[]"))
        arr.put(entry)
        prefs().edit().putString("history_$activeProfileId", arr.toString()).apply()
    }

    private fun showProgress(id: String, name: String) {
        val col = scaffold(showClose = true)

        col.addView(label("📊 $name's Progress", 25f))
        col.addView(space(16))

        val arr = JSONArray(prefs().getString("history_$id", "[]"))

        if (arr.length() == 0) {
            col.addView(label("No challenges yet — go play! 🎮", 18f, cPurple))
        } else {
            var totalCorrect = 0
            var totalAttempted = 0
            var totalTokens = 0
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                totalCorrect += o.getInt("correct")
                totalAttempted += o.getInt("attempted")
                totalTokens += o.optInt("tokens", 0)
            }
            val totalPct = if (totalAttempted == 0) 0 else totalCorrect * 100 / totalAttempted
            col.addView(
                label("All time: $totalCorrect/$totalAttempted correct ($totalPct%)  •  🪙 $totalTokens earned", 14f, cDark),
                fullWidth()
            )
            col.addView(space(14))

            for (i in arr.length() - 1 downTo 0) {
                val o = arr.getJSONObject(i)
                val secs = o.getInt("dur")
                val durText = if (secs >= 60) "${secs / 60} min" else "$secs s"
                val card = TextView(this).apply {
                    text = "${o.getString("date")}   •   ${o.getString("ops")}   •   ${o.optString("mode", "?")}   •   $durText\n" +
                            "${o.getInt("correct")}/${o.getInt("attempted")} correct  •  " +
                            "${o.getInt("pct")}%  •  ${"%.1f".format(o.getDouble("avg"))}s/answer  •  🪙 ${o.optInt("tokens", 0)}"
                    textSize = 13f
                    setTextColor(cDark)
                    background = rounded(Color.WHITE, 14)
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                }
                col.addView(card, fullWidth().apply { bottomMargin = dp(8) })
            }
        }

        col.addView(space(14))
        col.addView(bigButton("🏠 Home", cPurple) { showProfileSelect() }, fullWidth())
    }
}
