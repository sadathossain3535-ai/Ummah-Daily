package com.example.ui.screens.tasbih

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.config.AppConfigRegistry
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedSoft
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkGray
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SoftMintSurface
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import com.example.util.BengaliFormatter

@Composable
fun TasbihOnboardingModalOverlay(
    isLoading: Boolean,
    feedbackMessage: String?,
    onSubmit: (name: String, phone: String, area: String, district: String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var districtDropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f))
            .clickable(enabled = true, onClick = {}) // Trap backdrop taps like modal
            .padding(horizontal = 18.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .testTag("tasbih_onboarding_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CleanWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Modal Teal Top Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IslamicTeal)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = WarmAmberSoft
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudDone,
                                        contentDescription = null,
                                        tint = IslamicTealDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "ধাপ ${BengaliFormatter.toBengaliNumber(step)} / ২",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = IslamicTealDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Two-step progress dots
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(width = if (step == 1) 20.dp else 8.dp, height = 8.dp)
                                        .background(if (step == 1) WarmAmber else SoftMint, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = if (step == 2) 20.dp else 8.dp, height = 8.dp)
                                        .background(if (step == 2) WarmAmber else SoftMint, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (step == 1) "অ্যাকাউন্ট সেটআপ" else "আপনার লোকেশন",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = CleanWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (step == 1) {
                                "মোবাইল নম্বর দিলে পূর্বের সকল তাসবিহ ও আমলনামা স্বয়ংক্রিয়ভাবে পুনরুদ্ধার হবে।"
                            } else {
                                "আপনার এলাকা ও জেলা উল্লেখ করুন (র‍্যাংকিংয়ে প্রদর্শনের জন্য)।"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedContent(targetState = step, label = "onboarding_step") { currentStep ->
                        if (currentStep == 1) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        localError = null
                                    },
                                    label = { Text("নাম") },
                                    placeholder = { Text("আপনার নাম লিখুন...") },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Person, contentDescription = null, tint = IslamicTeal)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IslamicTeal,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = DarkGray,
                                        unfocusedTextColor = DarkGray
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_name_input")
                                )

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = {
                                        phone = it
                                        localError = null
                                    },
                                    label = { Text("মোবাইল নম্বর") },
                                    placeholder = { Text("মোবাইল নম্বর (যেমন: 017xxxxxxxx)") },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Phone, contentDescription = null, tint = IslamicTeal)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IslamicTeal,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = DarkGray,
                                        unfocusedTextColor = DarkGray
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_phone_input")
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = area,
                                    onValueChange = {
                                        area = it
                                        localError = null
                                    },
                                    label = { Text("এরিয়া / এলাকা") },
                                    placeholder = { Text("এরিয়া (যেমন: মিরপুর-১২)") },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = IslamicTeal)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IslamicTeal,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = DarkGray,
                                        unfocusedTextColor = DarkGray
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_area_input")
                                )

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = district,
                                        onValueChange = {
                                            district = it
                                            localError = null
                                        },
                                        label = { Text("জেলা") },
                                        placeholder = { Text("জেলা (যেমন: ঢাকা)") },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.LocationCity, contentDescription = null, tint = IslamicTeal)
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.ExpandMore,
                                                contentDescription = "জেলা তালিকা",
                                                tint = IslamicTeal,
                                                modifier = Modifier
                                                    .minimumInteractiveComponentSize()
                                                    .clickable { districtDropdownOpen = true }
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = IslamicTeal,
                                            unfocusedBorderColor = CardBorder,
                                            focusedTextColor = DarkGray,
                                            unfocusedTextColor = DarkGray
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("onboarding_district_input")
                                    )

                                    DropdownMenu(
                                        expanded = districtDropdownOpen,
                                        onDismissRequest = { districtDropdownOpen = false }
                                    ) {
                                        AppConfigRegistry.bangladeshCities.forEach { city ->
                                            DropdownMenuItem(
                                                text = { Text("${city.nameBn} (${city.divisionBn})") },
                                                onClick = {
                                                    district = city.nameBn
                                                    districtDropdownOpen = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val activeError = localError ?: feedbackMessage
                    if (!activeError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AlertRedSoft,
                            border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = activeError,
                                style = MaterialTheme.typography.bodySmall.copy(color = AlertRed),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (step == 1) {
                        Button(
                            onClick = {
                                val cleaned = phone.replace(Regex("[^0-9]"), "")
                                when {
                                    name.isBlank() -> localError = "অনুগ্রহ করে আপনার নাম লিখুন।"
                                    cleaned.length < 10 -> localError = "সঠিক মোবাইল নম্বর দিন (যেমন: 017xxxxxxxx)।"
                                    else -> {
                                        localError = null
                                        step = 2
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicTeal,
                                contentColor = CleanWhite
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .height(50.dp)
                                .testTag("onboarding_next_button")
                        ) {
                            Text(
                                text = "পরবর্তী ❯",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    localError = null
                                    step = 1
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .minimumInteractiveComponentSize()
                                    .height(50.dp)
                                    .testTag("onboarding_back_button")
                            ) {
                                Text("❮ পিছনে", color = IslamicTeal, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    when {
                                        area.isBlank() -> localError = "অনুগ্রহ করে আপনার এরিয়া লিখুন।"
                                        district.isBlank() -> localError = "অনুগ্রহ করে আপনার জেলা লিখুন।"
                                        else -> {
                                            localError = null
                                            onSubmit(name, phone, area, district)
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = IslamicTeal,
                                    contentColor = CleanWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .minimumInteractiveComponentSize()
                                    .height(50.dp)
                                    .testTag("onboarding_submit_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = CleanWhite,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("যাচাই হচ্ছে...")
                                } else {
                                    Text(
                                        text = "শুরু করুন ✔",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftMintTint, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(SoftMintSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDone,
                                contentDescription = null,
                                tint = IslamicTeal,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "আগে নিবন্ধিত থাকলে এই মোবাইল নম্বরে পূর্বের সকল তাসবিহ ও আমলনামা রিকভার হবে।",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
