package com.example.alp.ui.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.alp.data.local.TokenManager

@Composable
fun RequestNotificationPermissionOnce(tokenManager: TokenManager) {
    if (tokenManager.hasAskedNotificationPermission()) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        tokenManager.setAskedNotificationPermission(true)
        return
    }

    val context = LocalContext.current
    var showExplanationDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        tokenManager.setAskedNotificationPermission(true)
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            showExplanationDialog = true
        } else {
            tokenManager.setAskedNotificationPermission(true)
        }
    }

    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = {
                showExplanationDialog = false
                tokenManager.setAskedNotificationPermission(true)
            },
            title = { Text("Izin Notifikasi") },
            text = { Text("Izinkan notifikasi agar kami bisa mengingatkan jadwal makan Anda") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExplanationDialog = false
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text("Izinkan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExplanationDialog = false
                        tokenManager.setAskedNotificationPermission(true)
                    }
                ) {
                    Text("Nanti")
                }
            }
        )
    }
}
