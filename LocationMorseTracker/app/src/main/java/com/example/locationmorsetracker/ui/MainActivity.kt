package com.example.locationmorsetracker.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val smsGranted = permissions[Manifest.permission.SEND_SMS] == true
        val backgroundLocationGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true

        if (locationGranted && smsGranted && backgroundLocationGranted) {
            viewModel.startLocationService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LocationMorseTrackerApp(viewModel)
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.SEND_SMS
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMorseTrackerApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Location Morse Tracker") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Contact Number Input
                OutlinedTextField(
                    value = uiState.contactNumber,
                    onValueChange = { viewModel.updateContactNumber(it) },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                // Home Location
                Text("Home Location", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.homeLatitude,
                        onValueChange = { 
                            viewModel.updateHomeLocation(it, uiState.homeLongitude)
                        },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = uiState.homeLongitude,
                        onValueChange = { 
                            viewModel.updateHomeLocation(uiState.homeLatitude, it)
                        },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // College Location
                Text("College Location", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.collegeLatitude,
                        onValueChange = { 
                            viewModel.updateCollegeLocation(it, uiState.collegeLongitude)
                        },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = uiState.collegeLongitude,
                        onValueChange = { 
                            viewModel.updateCollegeLocation(uiState.collegeLatitude, it)
                        },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // Periodic Sharing Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Enable Periodic Sharing",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = uiState.isPeriodicSharingEnabled,
                        onCheckedChange = { viewModel.togglePeriodicSharing(it) }
                    )
                }

                // Service Control Button
                Button(
                    onClick = {
                        if (uiState.isLocationServiceRunning) {
                            viewModel.stopLocationService()
                        } else {
                            viewModel.startLocationService()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (uiState.isLocationServiceRunning) "Stop Tracking"
                        else "Start Tracking"
                    )
                }

                // Error Message
                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}