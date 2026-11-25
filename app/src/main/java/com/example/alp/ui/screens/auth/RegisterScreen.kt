package com.example.alp.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.alp.ui.navigation.Screen
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // rememberSaveable for non-password fields
    var email by rememberSaveable { mutableStateOf("") }

    // Passwords use remember (NOT saveable) for security
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Default timezone from ZoneId.systemDefault().id
    val defaultTimezone = remember {
        try { ZoneId.systemDefault().id } catch (_: Exception) { "Asia/Jakarta" }
    }
    var timezone by rememberSaveable { mutableStateOf(defaultTimezone) }
    var timezoneExpanded by remember { mutableStateOf(false) }

    // Add device's ZoneId.systemDefault().id to list if missing
    val availableTimezones = remember(defaultTimezone) {
        val baseList = mutableListOf(
            "Asia/Jakarta",
            "Asia/Makassar",
            "Asia/Jayapura",
            "Asia/Singapore",
            "Asia/Tokyo",
            "UTC"
        )
        if (!baseList.contains(defaultTimezone)) {
            baseList.add(0, defaultTimezone)
        }
        baseList
    }

    // Optional Collapsible Profile Section (rememberSaveable)
    var isProfileExpanded by rememberSaveable { mutableStateOf(false) }
    var gender by rememberSaveable { mutableStateOf("") }
    var weightKgStr by rememberSaveable { mutableStateOf("") }
    var heightCmStr by rememberSaveable { mutableStateOf("") }
    var ageStr by rememberSaveable { mutableStateOf("") }
    var activityLevel by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf("") }

    var genderExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Error -> {
                val errorMessage = (uiState as AuthUiState.Error).message
                if (errorMessage.contains("Lengkapi semua kolom profil", ignoreCase = true) ||
                    errorMessage.contains("profil", ignoreCase = true)
                ) {
                    isProfileExpanded = true
                }
                scrollState.animateScrollTo(0)
            }
            is AuthUiState.Success -> {
                navController.navigate(Screen.MealLog.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Buat Akun Baru",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mulai perjalanan nutrisi sehat Anda sekarang",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error Display
                if (uiState is AuthUiState.Error) {
                    val errorMessage = (uiState as AuthUiState.Error).message
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("nama@email.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Kata Sandi (Min. 6 karakter)") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Konfirmasi Kata Sandi") },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Timezone Dropdown
                ExposedDropdownMenuBox(
                    expanded = timezoneExpanded,
                    onExpandedChange = { timezoneExpanded = !timezoneExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = timezone,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Zona Waktu") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timezoneExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = timezoneExpanded,
                        onDismissRequest = { timezoneExpanded = false }
                    ) {
                        availableTimezones.forEach { tz ->
                            DropdownMenuItem(
                                text = { Text(tz) },
                                onClick = {
                                    timezone = tz
                                    timezoneExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Collapsible Optional Body Profile Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isProfileExpanded = !isProfileExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Profil Tubuh Awal (Opsional)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Diisi jika ingin menghitung TDEE secara otomatis",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (isProfileExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Toggle Profil"
                            )
                        }

                        AnimatedVisibility(visible = isProfileExpanded) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Text(
                                    text = "* Catatan: Kirim profil awal jika SEMUA 6 bidang di bawah terisi.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Jenis Kelamin
                                ExposedDropdownMenuBox(
                                    expanded = genderExpanded,
                                    onExpandedChange = { genderExpanded = !genderExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val genderDisplay = when (gender) {
                                        "MALE" -> "Laki-laki"
                                        "FEMALE" -> "Perempuan"
                                        else -> "Pilih Jenis Kelamin"
                                    }
                                    OutlinedTextField(
                                        value = genderDisplay,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Jenis Kelamin") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = genderExpanded,
                                        onDismissRequest = { genderExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Laki-laki") },
                                            onClick = { gender = "MALE"; genderExpanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Perempuan") },
                                            onClick = { gender = "FEMALE"; genderExpanded = false }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Berat Badan (kg) & Tinggi Badan (cm)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = weightKgStr,
                                        onValueChange = { weightKgStr = it },
                                        label = { Text("Berat (20-300 kg)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = heightCmStr,
                                        onValueChange = { heightCmStr = it },
                                        label = { Text("Tinggi (100-250 cm)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Umur (tahun)
                                OutlinedTextField(
                                    value = ageStr,
                                    onValueChange = { ageStr = it },
                                    label = { Text("Umur (10-100 tahun)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Tingkat Aktivitas
                                ExposedDropdownMenuBox(
                                    expanded = activityExpanded,
                                    onExpandedChange = { activityExpanded = !activityExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val activityDisplay = when (activityLevel) {
                                        "SEDENTARY" -> "Sangat Jarang Berolahraga"
                                        "LIGHT" -> "Olahraga Ringan (1-3 hari/minggu)"
                                        "MODERATE" -> "Olahraga Sedang (3-5 hari/minggu)"
                                        "ACTIVE" -> "Olahraga Berat (6-7 hari/minggu)"
                                        "VERY_ACTIVE" -> "Sangat Aktif / Fisik Berat"
                                        else -> "Pilih Tingkat Aktivitas"
                                    }
                                    OutlinedTextField(
                                        value = activityDisplay,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Tingkat Aktivitas") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = activityExpanded,
                                        onDismissRequest = { activityExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Sangat Jarang Berolahraga") },
                                            onClick = { activityLevel = "SEDENTARY"; activityExpanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Olahraga Ringan (1-3 hari/minggu)") },
                                            onClick = { activityLevel = "LIGHT"; activityExpanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Olahraga Sedang (3-5 hari/minggu)") },
                                            onClick = { activityLevel = "MODERATE"; activityExpanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Olahraga Berat (6-7 hari/minggu)") },
                                            onClick = { activityLevel = "ACTIVE"; activityExpanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Sangat Aktif / Fisik Berat") },
                                            onClick = { activityLevel = "VERY_ACTIVE"; activityExpanded = false }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Target Nutrisi (Goal)
                                ExposedDropdownMenuBox(
                                    expanded = goalExpanded,
                                    onExpandedChange = { goalExpanded = !goalExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val goalDisplay = when (goal) {
                                        "LOSE_WEIGHT" -> "Turunkan Berat Badan"
                                        "MAINTAIN" -> "Pertahankan Berat Badan"
                                        "GAIN_WEIGHT" -> "Naikkan Berat Badan"
                                        else -> "Pilih Target Nutrisi"
                                    }
                                    OutlinedTextField(
                                        value = goalDisplay,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Target Nutrisi") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = goalExpanded,
                                        onDismissRequest = { goalExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Turunkan Berat Badan") },
                                            onClick = { goal = "LOSE_WEIGHT"; goalExpanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Pertahankan Berat Badan") },
                                            onClick = { goal = "MAINTAIN"; goalExpanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Naikkan Berat Badan") },
                                            onClick = { goal = "GAIN_WEIGHT"; goalExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Submit Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.register(
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword,
                            timezone = timezone,
                            gender = gender,
                            weightKgStr = weightKgStr,
                            heightCmStr = heightCmStr,
                            ageStr = ageStr,
                            activityLevel = activityLevel,
                            goal = goal
                        )
                    },
                    enabled = uiState !is AuthUiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Daftar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Link (uses popBackStack())
                TextButton(
                    onClick = {
                        viewModel.resetState()
                        navController.popBackStack()
                    }
                ) {
                    Text(text = "Sudah punya akun? Masuk")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
