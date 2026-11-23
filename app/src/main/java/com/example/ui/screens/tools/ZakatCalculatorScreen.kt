package com.example.ui.screens.tools

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AnekBanglaFontFamily
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkGray
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import com.example.util.BengaliFormatter

@Composable
fun ZakatCalculatorScreen(
    onBack: () -> Unit,
    contentBottomPadding: PaddingValues
) {
    BackHandler(onBack = onBack)

    var goldInput by remember { mutableStateOf("") }
    var silverInput by remember { mutableStateOf("") }
    var cashBankInput by remember { mutableStateOf("") }
    var businessAssetsInput by remember { mutableStateOf("") }
    var debtInput by remember { mutableStateOf("") }

    val gold = goldInput.toDoubleOrNull() ?: 0.0
    val silver = silverInput.toDoubleOrNull() ?: 0.0
    val cashBank = cashBankInput.toDoubleOrNull() ?: 0.0
    val businessAssets = businessAssetsInput.toDoubleOrNull() ?: 0.0
    val debt = debtInput.toDoubleOrNull() ?: 0.0

    val grossAssets = gold + silver + cashBank + businessAssets
    val totalZakatableWealth = (grossAssets - debt).coerceAtLeast(0.0)
    val payableZakat = totalZakatableWealth * 0.025

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                    .background(IslamicTeal)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("zakat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = CleanWhite
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "যাকাত ক্যালকুলেটর",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "সহজ ও নির্ভুল ২.৫% যাকাত হিসাব",
                        style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = contentBottomPadding.calculateBottomPadding() + 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Result Summary Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = IslamicTeal),
                    border = BorderStroke(1.5.dp, WarmAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মোট যাকাতযোগ্য সম্পদ",
                                style = MaterialTheme.typography.bodyMedium.copy(color = SoftMint)
                            )
                            Text(
                                text = BengaliFormatter.formatCurrencyBn(totalZakatableWealth),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = AnekBanglaFontFamily,
                                    color = CleanWhite,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.testTag("zakat_total_wealth_text")
                            )
                        }

                        HorizontalDivider(color = SoftMint.copy(alpha = 0.25f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "প্রদেয় যাকাত (২.৫%)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = WarmAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "নিসাব পূর্ণ ও ১ বছর অতিক্রান্ত হলে",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SoftMint)
                                )
                            }
                            Text(
                                text = BengaliFormatter.formatCurrencyBn(payableZakat),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = AnekBanglaFontFamily,
                                    color = WarmAmber,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.testTag("zakat_payable_text")
                            )
                        }
                    }
                }

                // Inputs Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ZakatAmountField(
                            label = "স্বর্ণের মূল্য (টাকা)",
                            value = goldInput,
                            onValueChange = { goldInput = sanitizeDecimalInput(it) },
                            tag = "zakat_gold_input"
                        )
                        ZakatAmountField(
                            label = "রৌপ্যের মূল্য (টাকা)",
                            value = silverInput,
                            onValueChange = { silverInput = sanitizeDecimalInput(it) },
                            tag = "zakat_silver_input"
                        )
                        ZakatAmountField(
                            label = "নগদ ক্যাশ টাকা ও ব্যাংক জমা",
                            value = cashBankInput,
                            onValueChange = { cashBankInput = sanitizeDecimalInput(it) },
                            tag = "zakat_cash_input"
                        )
                        ZakatAmountField(
                            label = "ব্যবসায়িক পণ্যের বর্তমান মূল্য",
                            value = businessAssetsInput,
                            onValueChange = { businessAssetsInput = sanitizeDecimalInput(it) },
                            tag = "zakat_business_input"
                        )
                        ZakatAmountField(
                            label = "প্রদেয় ঋণ বা দেনা (মাইনাস হবে)",
                            value = debtInput,
                            onValueChange = { debtInput = sanitizeDecimalInput(it) },
                            isNegative = true,
                            tag = "zakat_debt_input"
                        )

                        OutlinedButton(
                            onClick = {
                                goldInput = ""
                                silverInput = ""
                                cashBankInput = ""
                                businessAssetsInput = ""
                                debtInput = ""
                            },
                            border = BorderStroke(1.dp, CardBorder),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("zakat_reset_button")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, tint = IslamicTeal)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সব রিসেট করুন", color = IslamicTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Nisab Info Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = WarmAmberSoft.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, WarmAmber.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💡 নিসাব সম্পর্কিত তথ্য",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = IslamicTealDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "৭.৫ ভরি (৮৭.৪৮ গ্রাম) স্বর্ণ অথবা ৫২.৫ তোলা (৬১২.৩৬ গ্রাম) রৌপ্যের সমমূল্য সম্পদ ঋণ বাদে এক চন্দ্রবছর স্থায়ী থাকলে মোট সম্পদের ২.৫% (৪০ ভাগের ১ ভাগ) যাকাত ফরজ হয়।",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZakatAmountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isNegative: Boolean = false,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("০") },
        prefix = {
            Text(
                text = if (isNegative) "- ৳ " else "৳ ",
                color = if (isNegative) AlertRed else IslamicTeal,
                fontWeight = FontWeight.Bold
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isNegative) AlertRed else IslamicTeal,
            unfocusedBorderColor = CardBorder,
            focusedContainerColor = SoftMintTint.copy(alpha = 0.4f),
            unfocusedContainerColor = SoftMintTint.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    )
}

private fun sanitizeDecimalInput(raw: String): String {
    val sb = StringBuilder()
    var hasDot = false
    for (ch in raw) {
        if (ch in '0'..'9') {
            sb.append(ch)
        } else if (ch == '.' && !hasDot) {
            hasDot = true
            sb.append(ch)
        }
    }
    return sb.toString()
}
