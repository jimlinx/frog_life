package com.froglife

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.froglife.ui.theme.FrogLifeTheme

/**
 * Simplified MainActivity for testing launcher issues
 * If this works, the problem is in the complex navigation setup
 * If this doesn't work, the problem is with theme/manifest/icons
 */
class MainActivitySimple : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FrogLifeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🐸",
                            fontSize = 72.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Frog Life",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "App is running!",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "If you see this, the app works.\nThe issue was with complex setup.",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
