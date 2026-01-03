package com.example.fitnesstracker

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnesstracker.models.Workout
import com.google.android.material.appbar.AppBarLayout
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_detail)

        // Nastavení paddingu pro AppBarLayout (aby tlačítko Zpět nebylo pod notch)
        val appBar = findViewById<AppBarLayout>(R.id.appBarLayout)

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Načtení dat z Intentu
        val workout = intent.getParcelableExtra<Workout>("WORKOUT_DATA")
        if (workout == null) {
            finish()
            return
        }

        // Vyplnění UI
        findViewById<TextView>(R.id.tvDetailName).text = workout.name

        val sdf = SimpleDateFormat("dd. MMM yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.tvDetailDate).text = sdf.format(workout.date).uppercase()

        val minutes = workout.durationSeconds / 60
        findViewById<TextView>(R.id.tvDetailDuration).text = "$minutes MIN"

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Generování karet pro cviky
        val container = findViewById<LinearLayout>(R.id.detailContainer)

        for (exercise in workout.exercises) {
            val card = createExerciseCard(exercise.name)
            val contentLayout = card.getChildAt(0) as LinearLayout

            for ((index, set) in exercise.sets.withIndex()) {
                val rowLayout = LinearLayout(this)
                rowLayout.orientation = LinearLayout.HORIZONTAL
                rowLayout.gravity = Gravity.CENTER_VERTICAL
                rowLayout.setPadding(0, 12, 0, 12)

                // Číslo série
                val tvNum = TextView(this)
                tvNum.text = "${index + 1}"
                tvNum.setTextColor(Color.parseColor("#9CA3AF"))
                tvNum.textSize = 14f
                tvNum.typeface = Typeface.DEFAULT_BOLD
                tvNum.gravity = Gravity.CENTER
                tvNum.width = 60

                // Výkon
                val tvValues = TextView(this)
                tvValues.text = "${set.weight} kg   ×   ${set.reps}"
                tvValues.setTextColor(Color.parseColor("#374151"))
                tvValues.textSize = 18f
                tvValues.typeface = Typeface.DEFAULT_BOLD
                tvValues.setPadding(32, 0, 0, 0)

                rowLayout.addView(tvNum)
                rowLayout.addView(tvValues)
                contentLayout.addView(rowLayout)
            }
            container.addView(card)
        }
    }

    private fun createExerciseCard(title: String): CardView {
        val card = CardView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 48)
        card.layoutParams = params
        card.radius = 48f
        card.cardElevation = 0f
        card.setCardBackgroundColor(Color.WHITE)

        val innerLayout = LinearLayout(this)
        innerLayout.orientation = LinearLayout.VERTICAL
        innerLayout.setPadding(48, 48, 48, 48)

        val titleView = TextView(this)
        titleView.text = title
        titleView.textSize = 22f
        titleView.typeface = Typeface.DEFAULT_BOLD
        titleView.setTextColor(Color.parseColor("#111827"))
        titleView.setPadding(0, 0, 0, 24)

        val divider = View(this)
        val divParams = LinearLayout.LayoutParams(100, 8)
        divParams.setMargins(0, 0, 0, 32)
        divider.layoutParams = divParams
        divider.setBackgroundColor(Color.parseColor("#4F46E5"))

        innerLayout.addView(titleView)
        innerLayout.addView(divider)
        card.addView(innerLayout)
        return card
    }
}
