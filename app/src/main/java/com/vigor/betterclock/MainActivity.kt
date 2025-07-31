package com.vigor.betterclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.vigor.betterclock.services.ToyService
import com.vigor.betterclock.ui.screens.MainScreen
import com.vigor.betterclock.ui.viewmodels.MainViewModel
import com.vigor.betterclock.ui.theme.BetterClockTheme
import com.vigor.betterclock.utils.DndUtils
import com.vigor.betterclock.utils.PrefUtils
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val viewmodel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BetterClockTheme {
                MainScreen(viewmodel)
            }
        }
        viewmodel.update_settings_data(this)
        viewmodel.check_permissions = {
            DndUtils.grantPermissions(this)
        }
        viewmodel.push_setting = { v, nv ->
            PrefUtils(this).save(v, nv)
            sendBroadcast(Intent(ToyService.ACTION_SETTINGS_UPDATE))
        }
    }
}
