package com.example.mobileappsdevelopmentproject4

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobileappsdevelopmentproject4.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    // ViewBinding gives us access to the views in activity_main.xml by their IDs.
    private lateinit var binding: ActivityMainBinding
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep the form clear of the status bar, navigation bar, and keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, keyboard.bottom)
            )
            insets
        }

        // The button's listener runs the calculation when the user taps it.
        binding.calculateButton.setOnClickListener {
            calculateTip()
        }
    }

    private fun calculateTip() {
        binding.billInput.error = null
        binding.tipInput.error = null

        // Parse decimal values safely. Empty or invalid text produces null.
        val bill = binding.billInput.text.toString().trim().toBigDecimalOrNull()
        val percentage = binding.tipInput.text.toString().trim().toBigDecimalOrNull()

        if (bill == null || bill < BigDecimal.ZERO || bill.scale() > 2) {
            binding.billInput.error = getString(R.string.cm_bill_error)
            binding.billInput.requestFocus()
            clearResults()
            return
        }

        if (percentage == null || percentage < BigDecimal.ZERO || percentage.scale() > 2) {
            binding.tipInput.error = getString(R.string.cm_tip_error)
            binding.tipInput.requestFocus()
            clearResults()
            return
        }

        // Divide the percentage by 100 and multiply by the bill.
        // Decimal arithmetic avoids binary floating-point errors in money values.
        val tip = bill.multiply(percentage).movePointLeft(2)
            .setScale(2, RoundingMode.HALF_UP)

        // Add the rounded tip to the original bill to get the final total.
        val total = bill.add(tip).setScale(2, RoundingMode.HALF_UP)

        // Show both results as US dollar amounts with two decimal places.
        binding.tipAmount.text = currencyFormat.format(tip)
        binding.totalAmount.text = currencyFormat.format(total)
    }

    private fun clearResults() {
        // Remove old results when a calculation cannot be completed.
        binding.tipAmount.setText(R.string.cm_pending)
        binding.totalAmount.setText(R.string.cm_pending)
    }
}