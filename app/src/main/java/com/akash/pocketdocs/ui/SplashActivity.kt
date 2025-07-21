package com.akash.pocketdocs.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.akash.pocketdocs.R
import com.akash.pocketdocs.security.LockManager
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LockManager.init(this)
        lifecycleScope.launch {
            if (LockManager.isPinSet()) {
                startActivity(Intent(this@SplashActivity, AppLockActivity::class.java))
            }
            else{
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            finish()
        }
    }
}