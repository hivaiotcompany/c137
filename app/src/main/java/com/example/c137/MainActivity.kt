package com.example.c137

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var btn =findViewById<Button>(R.id.button)
        btn.setOnClickListener{
            Toast.makeText(this, "hey!", Toast.LENGTH_SHORT).show()
        }
    }
}