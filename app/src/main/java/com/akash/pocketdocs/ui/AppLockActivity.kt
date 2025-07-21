package com.akash.pocketdocs.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.Visibility
import com.akash.pocketdocs.R
import com.akash.pocketdocs.databinding.ActivityAppLockBinding
import com.akash.pocketdocs.security.LockManager
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class AppLockActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAppLockBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executor = ContextCompat.getMainExecutor(this)

        controlBiometricButton()
        monitorPinTextChange()
        controlUnlockButtonClick()
    }

    private fun controlBiometricButton() {
        lifecycleScope.launch {
            //TODO remove || true after making profile page
            val isBioMetricEnabled = LockManager.isBioMetricEnabled() || true
            if(isBioMetricEnabled && isBioMetricAvailable()) {
                initBiometric()
                binding.biometricButton.setOnClickListener {
                    biometricPrompt.authenticate(promptInfo)
                }
            }
            else{
                binding.biometricButton.isEnabled = false
                binding.biometricButton.alpha = 0.3f
            }
        }
    }

    private fun initBiometric() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    goToMain()
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock PocketDocs")
            .setSubtitle("Use your biometrics")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun monitorPinTextChange() {
        binding.pinEditText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(pin: CharSequence?, start: Int, before: Int, count: Int) {
                val pinLength = pin?.length ?:0
                if (pinLength<6){
                    binding.unlockButton.isEnabled = false
                    binding.pinErrorText.text = "PIN must be 6 digits"
                    binding.pinErrorText.visibility = View.VISIBLE
                }
                else{
                    binding.unlockButton.isEnabled = true
                    binding.pinErrorText.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun controlUnlockButtonClick() {
        binding.unlockButton.setOnClickListener {
            val inputPin = binding.pinEditText.text.toString()
            lifecycleScope.launch {
              val savedPin = LockManager.getPin()
              if (inputPin == savedPin) {
                  goToMain()
              }
              else{
                   binding.pinErrorText.text = "Incorrect Pin"
                   binding.pinErrorText.visibility = View.VISIBLE
              }
            }
        }
    }

    private fun isBioMetricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}