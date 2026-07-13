package com.mathheroes.kids

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : Activity() {

    // ---------- colour palette ----------
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

    // ---------- session configuration ----------
    private var player = ""
    private val ops = linkedSetOf<Char>()            // '+', '-', 'x', '/'
    private var addMax = 20
    private var subMax = 20
    private val timesTables = sortedSetOf(2, 3, 4, 5)
    private val divTables = sortedSetOf(2, 3, 4, 5)
    private var timerSec = 60

    // ---------- challenge state ----------
    private var timer: CountDownTimer? = null
    private var correct = 0
    private var attempted = 0
    private var onHomeScreen = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showGreeting()
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
            showGreeting()
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
            setPadding(dp(14), dp(8), dp(14), dp(8))
            setOnClickListener { onClick() }
        }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    /**
     * Builds a screen: cream background, vertical column, optional red X
     * in the top-right corner. Returns the column to fill with content.
     */
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
                    onClose?.invoke() ?: showGreeting()
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
    //  Screen 1 — Greeting
    // ============================================================

    private fun showGreeting() {
        timer?.cancel()
        val col = scaffold(showClose = false)
        onHomeScreen = true
        col.gravity = Gravity.CENTER

        col.addView(label("🌟 Math Heroes 🌟", 34f))
        col.addView(space(6))
        col.addView(label("Who is playing today?", 19f, cPurple))
        col.addView(space(30))

        col.addView(bigButton("🦄  Selsa", cPink) {
            player = "Selsa"; showOperations()
        }, fullWidth())
        col.addView(space(16))
        col.addView(bigButton("🐬  Emilia", cPurple) {
            player = "Emilia"; showOperations()
        }, fullWidth())

        col.addView(space(34))
        col.addView(label("See your progress:", 15f, cDark))
        col.addView(space(10))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(dp(6), 0, dp(6), 0)
        }
        row.addView(smallButton("📊 Selsa", cBlue) { showProgress("Selsa") }, lp)
        row.addView(
            smallButton("📊 Emilia", cGreen) { showProgress("Emilia") },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dp(6), 0, dp(6), 0)
            }
        )
        row.layoutParams = fullWidth()
        col.addView(row)
    }

    // ============================================================
    //  Screen 2 — Choose operations
    // ============================================================

    private fun symbolFor(op: Char): String = when (op) {
        '+' -> "+"
        '-' -> "−"
        'x' -> "×"
        else -> "÷"
    }

    private fun showOperations() {
        val col = scaffold(showClose = true)

        col.addView(label("Hi $player! 👋", 26f))
        col.addView(space(4))
        col.addView(label("Choose your operations", 18f, cPurple))
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
    //  Screen 3 — Limits
    // ============================================================

    private fun sectionLabel(text: String, color: Int): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 17f
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

        col.addView(label("Set your limits", 26f))
        col.addView(space(18))

        var addInput: EditText? = null
        var subInput: EditText? = null

        if (ops.contains('+')) {
            col.addView(sectionLabel("＋ Addition — biggest answer (0–1000)", cGreen), fullWidth())
            col.addView(space(8))
            addInput = numberInput(addMax)
            col.addView(addInput, fullWidth())
            col.addView(space(16))
        }
        if (ops.contains('-')) {
            col.addView(sectionLabel("－ Subtraction — biggest top number (0–1000)", cBlue), fullWidth())
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
    //  Screen 4 — Timer choice
    // ============================================================

    private fun showTimerPick() {
        val col = scaffold(showClose = true)
        col.gravity = Gravity.CENTER

        col.addView(label("⏱ How long is your challenge?", 25f))
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
    //  Screen 5 — The challenge!
    // ============================================================

    private fun generateQuestion(): Pair<String, Int> {
        return when (val op = ops.random()) {
            '+' -> {
                val a = Random.nextInt(0, addMax + 1)
                val b = Random.nextInt(0, addMax - a + 1)
                "$a + $b = ?" to (a + b)
            }
            '-' -> {
                val a = Random.nextInt(0, subMax + 1)
                val b = Random.nextInt(0, a + 1)
                "$a − $b = ?" to (a - b)
            }
            'x' -> {
                val t = timesTables.random()
                val k = Random.nextInt(1, 13)
                "$t × $k = ?" to (t * k)
            }
            else -> {
                val t = divTables.random()
                val k = Random.nextInt(1, 13)
                "${t * k} ÷ $t = ?" to k
            }
        }
    }

    private fun showChallenge() {
        correct = 0
        attempted = 0

        val col = scaffold(showClose = true, scrollable = false) {
            timer?.cancel()
            showGreeting()
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

        // flexible spacer pushes the keypad to the bottom
        col.addView(View(this), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        var entry = ""
        var answer = 0

        fun renderEntry() {
            answerView.text = if (entry.isEmpty()) " " else entry
        }

        fun nextQuestion() {
            val q = generateQuestion()
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
                feedback.text = "✅ Great job!"
                feedback.setTextColor(cGreen)
            } else {
                feedback.text = "❌ It was $answer"
                feedback.setTextColor(cRed)
            }
            handler.postDelayed({ feedback.text = " " }, 1200)
            nextQuestion()
        }

        // keypad: 1-9, C (clear), 0, backspace
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
    //  Screen 6 — Results
    // ============================================================

    private fun showResults() {
        timer?.cancel()
        val pct = if (attempted == 0) 0 else correct * 100 / attempted
        val avg = if (attempted == 0) 0.0 else timerSec.toDouble() / attempted
        saveSession(pct, avg)

        val col = scaffold(showClose = false)
        col.gravity = Gravity.CENTER

        val headline = when {
            attempted == 0 -> "⏰ Time's up, $player!"
            pct >= 80 -> "🏆 Amazing, $player!"
            pct >= 50 -> "🌟 Well done, $player!"
            else -> "💪 Keep practising, $player!"
        }
        col.addView(label(headline, 28f))
        col.addView(space(22))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 20)
            setPadding(dp(20), dp(18), dp(20), dp(18))
        }
        card.addView(label("✅ Correct: $correct / $attempted", 20f), fullWidth())
        card.addView(space(10))
        card.addView(label("🎯 Score: $pct%", 20f), fullWidth())
        card.addView(space(10))
        val speedText = if (attempted == 0) "⚡ Speed: —"
        else "⚡ Speed: ${"%.1f".format(avg)} s per answer"
        card.addView(label(speedText, 20f), fullWidth())
        col.addView(card, fullWidth())

        col.addView(space(26))
        col.addView(bigButton("🔁 Play again", cOrange) { showChallenge() }, fullWidth())
        col.addView(space(12))
        col.addView(bigButton("📊 My progress", cBlue) { showProgress(player) }, fullWidth())
        col.addView(space(12))
        col.addView(bigButton("🏠 Home", cPurple) { showGreeting() }, fullWidth())
    }

    // ============================================================
    //  Progress tracking
    // ============================================================

    private fun saveSession(pct: Int, avg: Double) {
        if (player.isEmpty()) return
        val prefs = getSharedPreferences("math_heroes", MODE_PRIVATE)
        val arr = JSONArray(prefs.getString("history_$player", "[]"))
        val entry = JSONObject()
            .put("date", SimpleDateFormat("dd MMM yyyy HH:mm", Locale.UK).format(Date()))
            .put("ops", ops.joinToString(" ") { symbolFor(it) })
            .put("correct", correct)
            .put("attempted", attempted)
            .put("pct", pct)
            .put("avg", avg)
            .put("dur", timerSec)
        arr.put(entry)
        prefs.edit().putString("history_$player", arr.toString()).apply()
    }

    private fun showProgress(name: String) {
        val col = scaffold(showClose = true)

        col.addView(label("📊 $name's Progress", 27f))
        col.addView(space(16))

        val prefs = getSharedPreferences("math_heroes", MODE_PRIVATE)
        val arr = JSONArray(prefs.getString("history_$name", "[]"))

        if (arr.length() == 0) {
            col.addView(label("No challenges yet — go play! 🎮", 18f, cPurple))
        } else {
            var totalCorrect = 0
            var totalAttempted = 0
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                totalCorrect += o.getInt("correct")
                totalAttempted += o.getInt("attempted")
            }
            val totalPct = if (totalAttempted == 0) 0 else totalCorrect * 100 / totalAttempted
            col.addView(
                label("All time: $totalCorrect correct out of $totalAttempted  ($totalPct%)", 16f, cDark),
                fullWidth()
            )
            col.addView(space(14))

            for (i in arr.length() - 1 downTo 0) {
                val o = arr.getJSONObject(i)
                val mins = o.getInt("dur")
                val durText = if (mins >= 60) "${mins / 60} min" else "$mins s"
                val card = TextView(this).apply {
                    text = "${o.getString("date")}   •   ${o.getString("ops")}   •   $durText\n" +
                            "${o.getInt("correct")}/${o.getInt("attempted")} correct  •  " +
                            "${o.getInt("pct")}%  •  ${"%.1f".format(o.getDouble("avg"))}s per answer"
                    textSize = 14f
                    setTextColor(cDark)
                    background = rounded(Color.WHITE, 14)
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                }
                col.addView(card, fullWidth().apply { bottomMargin = dp(8) })
            }
        }

        col.addView(space(14))
        col.addView(bigButton("🏠 Home", cPurple) { showGreeting() }, fullWidth())
    }
}
