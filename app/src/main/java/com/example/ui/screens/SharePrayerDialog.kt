package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.PrayerScheduleState
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.ShareGreen
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareCardGenerator

private enum class ShareFormatMode {
    IMAGE,
    TEXT
}

@Composable
fun SharePrayerDialog(
    schedule: PrayerScheduleState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var shareMode by remember { mutableStateOf(ShareFormatMode.IMAGE) }

    val previewBitmap = remember(schedule) {
        ShareCardGenerator.generateScheduleBitmap(context, schedule).asImageBitmap()
    }
    val formattedText = remember(schedule) {
        ShareCardGenerator.buildFormattedBengaliText(schedule)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = CleanWhite),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp)
                .testTag("share_prayer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নামাজের সময়সূচী শেয়ার",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("share_dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = TextSecondary
                        )
                    }
                }

                // Radio toggle: 🖼️ ছবি আকারে vs 📝 টেক্সট আকারে
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShareFormatRadioChip(
                        label = "🖼️ ছবি আকারে",
                        selected = shareMode == ShareFormatMode.IMAGE,
                        onClick = { shareMode = ShareFormatMode.IMAGE },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_mode_image_radio")
                    )
                    ShareFormatRadioChip(
                        label = "📝 টেক্সট আকারে",
                        selected = shareMode == ShareFormatMode.TEXT,
                        onClick = { shareMode = ShareFormatMode.TEXT },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_mode_text_radio")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (shareMode == ShareFormatMode.IMAGE) {
                    Image(
                        bitmap = previewBitmap,
                        contentDescription = "আজকের নামাজের সময়সূচী প্রিন্টেবল কার্ড",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SoftMintTint,
                        border = BorderStroke(1.dp, SoftMint),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = formattedText,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("Ummah Daily Schedule", formattedText)
                                    )
                                    Toast.makeText(context, "সময়সূচী কপি হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("copy_schedule_text_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("টেক্সট কপি করুন")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Share Action: 📤 শেয়ার করুন
                Button(
                    onClick = {
                        if (shareMode == ShareFormatMode.IMAGE) {
                            ShareCardGenerator.shareSchedulePng(context, schedule)
                        } else {
                            ShareCardGenerator.shareFormattedText(
                                context = context,
                                text = formattedText,
                                chooserTitle = context.getString(R.string.share_schedule_chooser)
                            )
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShareGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("share_confirm_button")
                ) {
                    Text(
                        text = "📤 শেয়ার করুন",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save PNG to Gallery: 📥 ছবি সেভ করুন
                OutlinedButton(
                    onClick = {
                        val res = ShareCardGenerator.saveSchedulePngToGallery(context, schedule)
                        res.fold(
                            onSuccess = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            },
                            onFailure = {
                                Toast.makeText(context, "ছবি সেভ করা যায়নি", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    border = BorderStroke(1.dp, IslamicTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("save_schedule_png_button")
                ) {
                    Text(
                        text = "📥 ছবি সেভ করুন",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = IslamicTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareFormatRadioChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) SoftMint else SoftMintTint,
        border = BorderStroke(1.dp, if (selected) IslamicTeal else CardBorder),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = IslamicTeal)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = IslamicTeal,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}
