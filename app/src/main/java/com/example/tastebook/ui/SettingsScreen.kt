package com.example.tastebook.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tastebook.viewmodel.DriveViewModel

@Composable
fun SettingsScreen(
    driveViewModel: DriveViewModel,
    navController: NavController,
    onConnectDriveClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    // Observe state from ViewModel
    val isSignedIn by driveViewModel.isSignedIn.collectAsState()
    val syncStatus by driveViewModel.syncStatus.collectAsState()
    val userEmail by driveViewModel.userEmail.collectAsState()
    val isLoading by driveViewModel.isLoading.collectAsState()

    // Create the ActivityResultLauncher for Drive permission
    val drivePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // Pass the result to ViewModel to handle the Drive authorization
        driveViewModel.handleAuthorizationResult(result.data)
    }

    Scaffold(
        topBar = { TopBar(title = "Settings") },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (!isSignedIn) {
                Button(
                    onClick = {
                        // Start Google Sign-in
                        driveViewModel.beginGoogleSignIn(activity, drivePermissionLauncher)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Connect to Google Drive")
                }
            } else {
                Text(
                    text = "Signed in as: ${userEmail ?: "Unknown"}",
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { driveViewModel.syncRecipesFromDrive() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Sync From Drive")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { driveViewModel.uploadRecipesToDrive(drivePermissionLauncher) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Sync With Drive")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { driveViewModel.signOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Sign Out")
                }
            }

            syncStatus?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Status: $it")
            }
        }

        // -------------------------
        // Show loading spinner
        // -------------------------
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}