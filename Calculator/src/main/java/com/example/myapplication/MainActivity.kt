package com.example.myapplication

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import org.mariuszgromada.math.mxparser.Expression

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var stmerror = false
    private var lastdigit = true
    private var lastdot = false
    private var isLastOptr = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.overflowIcon?.setTint(ContextCompat.getColor(this,R.color.black))
        binding.result.text = "0"
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Handle Settings action
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_history -> {
                // Handle History action
                Toast.makeText(this, "History clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun num(view: View) {
        val buttonText = (view as Button).text.toString()
        val currentText = binding.result.text.toString()

        if (stmerror) {
            // Reset the display if there's an error
            binding.result.text = buttonText
            binding.operationtext.text = ""
            stmerror = false
        } else {
            if (currentText == "0" && buttonText != ".") {
                // Replace "0" with the new number unless it's a dot
                binding.result.text = buttonText
            } else {
                binding.result.append(buttonText)
            }
        }

        vibratePhone(10)
        lastdigit = true
    }

    fun operator(view: View) {
        val currentText = binding.result.text.toString()

        if (lastdigit && currentText.isNotEmpty()) {
            binding.result.append((view as Button).text.toString())
            lastdigit = false
            lastdot = false
        } else if (!lastdigit && currentText.isNotEmpty()) {
            // If the last character is an operator, replace it with the new one
            val newOperator = (view as Button).text.toString()
            binding.result.text = currentText.dropLast(1).plus(newOperator)
        }
        vibratePhone(20)
    }

    fun percentage(view: View) {
        if (lastdigit && !stmerror) {
            try {
                val currentText = binding.result.text.toString()
                val value = currentText.toDouble()
                val result = value / 100
                val formattedResult = if (result == result.toInt().toDouble()) {
                    result.toInt().toString()
                } else {
                    result.toString()
                }

                // Update result and operation text
                binding.result.text = formattedResult
                binding.operationtext.text = formattedResult

                lastdigit = true
                lastdot = formattedResult.contains(".")
            } catch (e: Exception) {
                showError()
            }
        } else {
            showError()
        }
        vibratePhone(20)
    }

    fun dot(view: View) {

        if (!lastdot) {
            // Append a dot if no dot exists yet
            if (lastdigit) {
                binding.result.append(".")
            } else {
                // Add "0." if dot is pressed after an operator
                binding.result.append("0.")
            }
            lastdot = true
            lastdigit = false
        } else {
            Toast.makeText(this, "Invalid decimal point placement", Toast.LENGTH_LONG).show()
        }
        vibratePhone(20)
    }

    fun clear(view: View) {
        binding.result.text = "0"
        binding.operationtext.text = ""
        lastdot = false
        lastdigit = true
        stmerror = false
        vibratePhone(20)
    }

    fun backspace(view: View) {
        val currentText = binding.result.text.toString()

        if (currentText.length > 1) {
            binding.result.text = currentText.dropLast(1)
            val newText = binding.result.text.toString()
            lastdigit = newText.isNotEmpty() && newText.lastOrNull()?.isDigit() ?: false
            lastdot = newText.contains(".")
        } else {
            // Reset to 0 if only one character is left
            binding.result.text = "0"
            lastdigit = true
            lastdot = false
        }
        binding.operationtext.text = ""
        vibratePhone(20)
    }


    fun equal(view: View) {
        if (lastdigit && !stmerror) {
            try {
                val expressionText = binding.result.text.toString()
                    .replace("×", "*")
                    .replace("÷", "/")

                val expression = Expression(expressionText)
                val result = expression.calculate()

                if (result.isNaN()) {
                    throw Exception("Invalid calculation")
                }

                val formattedResult = if (result == result.toInt().toDouble()) {
                    result.toInt().toString()
                } else {
                    result.toString()
                }

                binding.operationtext.text = formattedResult
                binding.result.text = expressionText

                lastdigit = true
                lastdot = formattedResult.contains(".")
            } catch (e: Exception) {
                showError()
            }
        } else {
            showError()
        }
        vibratePhone(30)
    }

    private fun showError() {
        // Display an error message and reset relevant flags
        binding.operationtext.text = "Error"
        binding.result.text = "0"
        stmerror = true
        lastdigit = false
        lastdot = false
    }

    private fun Context.vibratePhone(duration: Long = 100) {
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vib.vibrate(duration)
        }
    }
}