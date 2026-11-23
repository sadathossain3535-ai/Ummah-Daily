package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.AnekBanglaFontFamily
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.IslamicTealDark
import com.example.ui.theme.ShareGreen
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SoftMintSurface
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.util.BengaliFormatter
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val cityCoordinatesMap = mapOf(
    "Dhaka" to (23.8103 to 90.4125),
    "Chittagong" to (22.3569 to 91.7832),
    "Sylhet" to (24.8949 to 91.8687),
    "Rajshahi" to (24.3745 to 88.6042),
    "Khulna" to (22.8456 to 89.5403),
    "Barishal" to (22.7010 to 90.3535),
    "Rangpur" to (25.7439 to 89.2752),
    "Mymensingh" to (24.7471 to 90.4203)
)

@Composable
fun QiblaCompassScreen(
    selectedCityApi: String,
    selectedCityNameBn: String,
    onBack: () -> Unit,
    contentBottomPadding: PaddingValues
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val fallbackCoords = cityCoordinatesMap[selectedCityApi] ?: (23.8103 to 90.4125)

    var userLat by remember { mutableStateOf(fallbackCoords.first) }
    var userLon by remember { mutableStateOf(fallbackCoords.second) }
    var usingGps by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Continuous unwrapped angles prevent 360->0 backward jump spins
    var smoothAzimuthDeg by remember { mutableFloatStateOf(0f) }
    var continuousDialAngle by remember { mutableFloatStateOf(0f) }
    var continuousNeedleAngle by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(true) }

    val qiblaBearing = remember(userLat, userLon) { computeQiblaBearing(userLat, userLon) }

    val applyLocation: (Location?) -> Unit = { loc ->
        if (loc != null) {
            userLat = loc.latitude
            userLon = loc.longitude
            usingGps = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            applyLocation(readBestLastLocation(context))
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            applyLocation(readBestLastLocation(context))
        }
    }

    // Active LocationListener when permission is granted so GPS updates live
    DisposableEffect(hasLocationPermission, context) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val listener = LocationListener { location ->
                applyLocation(location)
            }
            @SuppressLint("MissingPermission")
            fun startUpdates() {
                applyLocation(readBestLastLocation(context))
                listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { provider ->
                    runCatching {
                        if (lm.isProviderEnabled(provider)) {
                            lm.requestLocationUpdates(provider, 3000L, 10f, listener, Looper.getMainLooper())
                        }
                    }
                }
            }
            startUpdates()
            onDispose {
                runCatching { lm.removeUpdates(listener) }
            }
        }
    }

    // Native Hardware SensorManager (TYPE_ROTATION_VECTOR primary, Accelerometer + Magnetometer fallback)
    DisposableEffect(context, qiblaBearing) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magnetSensor == null)) {
            sensorAvailable = false
            continuousNeedleAngle = qiblaBearing
            onDispose { }
        } else {
            val accelData = FloatArray(3)
            val magnetData = FloatArray(3)
            var hasAccel = false
            var hasMagnet = false
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            var initialized = false

            fun pushAzimuth(rawDeg: Float) {
                val normalized = (rawDeg + 360f) % 360f
                if (!initialized) {
                    smoothAzimuthDeg = normalized
                    continuousDialAngle = -normalized
                    continuousNeedleAngle = ((qiblaBearing - normalized) + 360f) % 360f
                    initialized = true
                } else {
                    val delta = shortestSignedAngleDelta(smoothAzimuthDeg, normalized)
                    // Low-pass filter (alpha = 0.22) eliminates jitter while remaining responsive
                    smoothAzimuthDeg = ((smoothAzimuthDeg + 0.22f * delta) + 360f) % 360f
                    val targetDial = -smoothAzimuthDeg
                    continuousDialAngle += shortestSignedAngleDelta(continuousDialAngle, targetDial)

                    val targetNeedle = qiblaBearing - smoothAzimuthDeg
                    continuousNeedleAngle += shortestSignedAngleDelta(continuousNeedleAngle, targetNeedle)
                }
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            pushAzimuth(deg)
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            lowPassVector(event.values, accelData)
                            hasAccel = true
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            lowPassVector(event.values, magnetData)
                            hasMagnet = true
                        }
                    }
                    if (rotationSensor == null && hasAccel && hasMagnet) {
                        if (SensorManager.getRotationMatrix(rotMatrix, null, accelData, magnetData)) {
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            pushAzimuth(deg)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            if (rotationSensor != null) {
                sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
            } else {
                sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_GAME)
            }

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val relativeAngle = ((qiblaBearing - smoothAzimuthDeg) + 360f) % 360f
    val diffFromTop = minOf(relativeAngle, 360f - relativeAngle)
    val isAligned = diffFromTop <= 6f

    val animatedDialRotation by animateFloatAsState(
        targetValue = continuousDialAngle,
        animationSpec = spring(stiffness = 220f, dampingRatio = 0.85f),
        label = "qibla_dial_rotation"
    )
    val animatedQiblaRotation by animateFloatAsState(
        targetValue = continuousNeedleAngle,
        animationSpec = spring(stiffness = 220f, dampingRatio = 0.85f),
        label = "qibla_needle_rotation"
    )

    val textMeasurer = rememberTextMeasurer()

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
            // Teal Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(IslamicTeal)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("qibla_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = CleanWhite
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "🕋 কিবলা কম্পাস",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (usingGps) {
                            "জিপিএস সক্রিয় (${String.format(java.util.Locale.US, "%.2f, %.2f", userLat, userLon)})"
                        } else {
                            "$selectedCityNameBn, বাংলাদেশ (ডিফল্ট অবস্থান)"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = contentBottomPadding.calculateBottomPadding() + 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = BorderStroke(1.dp, if (isAligned) ShareGreen else CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isAligned) ShareGreen.copy(alpha = 0.16f) else SoftMintSurface
                        ) {
                            Text(
                                text = if (isAligned) {
                                    "✅ পবিত্র কাবার দিকেই মুখ রয়েছে!"
                                } else {
                                    "কিবলা ${BengaliFormatter.toBengaliNumber(qiblaBearing.roundToInt())}° • ফোন সমতলে ঘোরান"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isAligned) ShareGreen else IslamicTeal,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier.size(268.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val r = size.minDimension / 2f - 10.dp.toPx()

                                // Outer Soft Mint dial ring
                                drawCircle(
                                    color = SoftMint,
                                    radius = r,
                                    style = Stroke(width = 14.dp.toPx())
                                )

                                // Rotating N, S, E, W Dial Markings & Ticks
                                rotate(degrees = animatedDialRotation, pivot = Offset(cx, cy)) {
                                    for (deg in 0 until 360 step 15) {
                                        val isMajor = deg % 90 == 0
                                        val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                                        rotate(degrees = deg.toFloat(), pivot = Offset(cx, cy)) {
                                            drawLine(
                                                color = if (deg == 0) WarmAmber else IslamicTeal.copy(alpha = 0.5f),
                                                start = Offset(cx, cy - r + 8.dp.toPx()),
                                                end = Offset(cx, cy - r + 8.dp.toPx() + tickLen),
                                                strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx(),
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }

                                    // Cardinal letters N, E, S, W on outer ring
                                    val cardinals = listOf(
                                        0f to "N",
                                        90f to "E",
                                        180f to "S",
                                        270f to "W"
                                    )
                                    val labelDist = r - 24.dp.toPx()
                                    cardinals.forEach { (angle, letter) ->
                                        val rad = Math.toRadians((angle - 90.0))
                                        val lx = cx + (labelDist * cos(rad)).toFloat()
                                        val ly = cy + (labelDist * sin(rad)).toFloat()
                                        val measured = textMeasurer.measure(
                                            text = letter,
                                            style = TextStyle(
                                                color = if (letter == "N") WarmAmber else IslamicTealDark,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                        drawText(
                                            textLayoutResult = measured,
                                            topLeft = Offset(
                                                lx - measured.size.width / 2f,
                                                ly - measured.size.height / 2f
                                            )
                                        )
                                    }
                                }

                                // Inner Deep Teal (#04484A) Compass Face
                                val innerR = r - 36.dp.toPx()
                                drawCircle(
                                    color = IslamicTeal,
                                    radius = innerR
                                )
                                drawCircle(
                                    color = WarmAmber.copy(alpha = 0.35f),
                                    radius = innerR,
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Rotating #04484A to #f9a826 Gradient Qibla Needle
                                rotate(degrees = animatedQiblaRotation, pivot = Offset(cx, cy)) {
                                    val tipY = cy - innerR + 10.dp.toPx()
                                    val tailY = cy + innerR * 0.42f
                                    val needlePath = Path().apply {
                                        moveTo(cx, tipY)
                                        lineTo(cx - 15.dp.toPx(), cy + 6.dp.toPx())
                                        lineTo(cx, cy - 5.dp.toPx())
                                        lineTo(cx + 15.dp.toPx(), cy + 6.dp.toPx())
                                        close()
                                    }
                                    drawPath(
                                        path = needlePath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(WarmAmber, IslamicTealDark),
                                            startY = tipY,
                                            endY = tailY
                                        )
                                    )

                                    // Pointer stem & Kaaba tip highlight
                                    drawCircle(
                                        color = if (isAligned) ShareGreen else WarmAmber,
                                        radius = 6.dp.toPx(),
                                        center = Offset(cx, tipY + 4.dp.toPx())
                                    )
                                }

                                drawCircle(
                                    color = CleanWhite,
                                    radius = 8.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                            }

                            Text(
                                text = "🕋",
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "কিবলা ${BengaliFormatter.toBengaliNumber(qiblaBearing.roundToInt())}°",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = AnekBanglaFontFamily,
                                color = IslamicTeal,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "পবিত্র কাবা শরীফ (২১.৪২২৫° উত্তর, ৩৯.৮২৬২° পূর্ব)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        if (!sensorAvailable) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "এই ডিভাইসে ম্যাগনেটোমিটার সেন্সর নেই; গণনাকৃত দিক দেখানো হচ্ছে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (hasLocationPermission) {
                                applyLocation(readBestLastLocation(context))
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        border = BorderStroke(1.dp, IslamicTeal),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("qibla_gps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = null,
                            tint = IslamicTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("জিপিএস আপডেট", color = IslamicTeal)
                    }

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicTeal),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("বন্ধ করুন", color = CleanWhite)
                    }
                }
            }
        }
    }
}

private fun lowPassVector(input: FloatArray, output: FloatArray) {
    val alpha = 0.2f
    for (i in 0 until minOf(input.size, output.size)) {
        output[i] = output[i] + alpha * (input[i] - output[i])
    }
}

private fun shortestSignedAngleDelta(fromDeg: Float, toDeg: Float): Float {
    var diff = ((toDeg - fromDeg) % 360f + 540f) % 360f - 180f
    if (abs(diff) > 180f) diff = 0f
    return diff
}

@SuppressLint("MissingPermission")
private fun readBestLastLocation(context: Context): Location? {
    return runCatching {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var best: Location? = null
        for (p in providers) {
            val loc = lm.getLastKnownLocation(p) ?: continue
            if (best == null || loc.accuracy < best.accuracy) {
                best = loc
            }
        }
        best
    }.getOrNull()
}

private fun computeQiblaBearing(latDeg: Double, lonDeg: Double): Float {
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLon = Math.toRadians(39.8262)
    val userLat = Math.toRadians(latDeg)
    val userLon = Math.toRadians(lonDeg)
    val dLon = kaabaLon - userLon
    val y = sin(dLon) * cos(kaabaLat)
    val x = cos(userLat) * sin(kaabaLat) - sin(userLat) * cos(kaabaLat) * cos(dLon)
    val bearing = Math.toDegrees(atan2(y, x)).toFloat()
    return (bearing + 360f) % 360f
}
