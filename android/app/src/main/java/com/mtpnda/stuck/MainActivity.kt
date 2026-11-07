package com.mtpnda.stuck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mtpnda.stuck.data.ContextRepository
import com.mtpnda.stuck.model.ContextStatus
import com.mtpnda.stuck.ui.screens.*
import com.mtpnda.stuck.ui.theme.ContextColors
import com.mtpnda.stuck.ui.theme.ContextsTheme

class MainActivity : ComponentActivity() {
    private lateinit var repository: ContextRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ContextRepository(applicationContext)

        setContent {
            ContextsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ContextColors.Bg
                ) {
                    val profile by repository.profile.collectAsState()
                    val isLocked by repository.isLocked.collectAsState()

                    var isShowingProfile by remember { mutableStateOf(false) }
                    var sheetContextId by remember { mutableStateOf<String?>(null) }
                    var isShowingNewSheet by remember { mutableStateOf(false) }
                    var newSheetInitialStatus by remember { mutableStateOf(ContextStatus.ACTIVE) }
                    var newSheetInitialDate by remember { mutableStateOf(System.currentTimeMillis()) }

                    if (profile == null) {
                        LoginScreen(repository = repository)
                    } else if (isLocked && profile?.faceId == true) {
                        LockScreen(repository = repository)
                    } else if (isShowingProfile) {
                        ProfileScreen(
                            repository = repository,
                            onDismiss = { isShowingProfile = false }
                        )
                    } else if (isShowingNewSheet) {
                        ContextSheet(
                            repository = repository,
                            contextId = null,
                            initialStatus = newSheetInitialStatus,
                            initialDate = newSheetInitialDate,
                            onDismiss = { isShowingNewSheet = false }
                        )
                    } else if (sheetContextId != null) {
                        ContextSheet(
                            repository = repository,
                            contextId = sheetContextId,
                            onDismiss = { sheetContextId = null }
                        )
                    } else {
                        BoardScreen(
                            repository = repository,
                            onOpenProfile = { isShowingProfile = true },
                            onNewContext = { status, date ->
                                newSheetInitialStatus = status
                                newSheetInitialDate = date
                                isShowingNewSheet = true
                            },
                            onEditContext = { id -> sheetContextId = id }
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        repository.lock()
    }
}
