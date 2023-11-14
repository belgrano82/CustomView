package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = findViewById<StatsView>(R.id.stats)
        view.data = listOf(
            400F,
            400F,
            400F,
            400F,
            400F
        )

    }


}

