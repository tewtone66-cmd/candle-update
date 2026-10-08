package com.candle.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

private val Bg = Color(0xFF07090F)
private val Panel = Color(0xFF0F1421)
private val Orange = Color(0xFFFF8A1F)
private val Amber = Color(0xFFFFC15A)
private val Muted = Color(0xFF8A93A8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        setContent { CandleApp() }
    }
}

/* ---------- helpers ---------- */

@Composable
private fun Modifier.enter(i: Int): Modifier {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { delay(i * 90L); a.animateTo(1f, tween(550, easing = FastOutSlowInEasing)) }
    return this.graphicsLayer { alpha = a.value; translationY = (1f - a.value) * 70f }
}

@Composable
private fun Modifier.pressable(onClick: () -> Unit): Modifier {
    val src = remember { MutableInteractionSource() }
    val p by src.collectIsPressedAsState()
    val s by animateFloatAsState(if (p) 0.93f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "press")
    return this.graphicsLayer { scaleX = s; scaleY = s }.clickable(src, null, onClick = onClick)
}

private fun Modifier.panel(): Modifier =
    this.background(Panel, RoundedCornerShape(18.dp)).border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))

@Composable
private fun Chip(text: String, color: Color, textColor: Color = Color.Black) {
    Text(
        text, color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.background(color, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

/* ---------- root ---------- */

@Composable
fun CandleApp() {
    var tab by remember { mutableIntStateOf(0) }
    val t = rememberInfiniteTransition(label = "bg")
    val drift = t.animateFloat(0.15f, 0.85f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), label = "d")

    Box(Modifier.fillMaxSize().background(Bg).drawBehind {
        drawRect(Brush.radialGradient(listOf(Orange.copy(alpha = 0.16f), Color.Transparent),
            center = Offset(size.width * drift.value, size.height * 0.15f), radius = size.width * 0.5f))
    }) {
        Row(Modifier.fillMaxSize().systemBarsPadding().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Sidebar(tab) { tab = it }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.enter(0)) { TopBar() }
                AnimatedContent(
                    targetState = tab, modifier = Modifier.weight(1f).fillMaxWidth(),
                    transitionSpec = { (fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 14 }) togetherWith fadeOut(tween(120)) },
                    label = "tabs"
                ) { t2 -> if (t2 == 0) Home { tab = it } else Placeholder(tabs[t2].first) }
            }
        }
    }
}

private val tabs: List<Pair<String, ImageVector>> = listOf(
    "Home" to Icons.Default.Home, "Play" to Icons.Default.PlayArrow, "Mods" to Icons.Default.Build,
    "Cosmetics" to Icons.Default.Star, "Resource Packs" to Icons.Default.Favorite, "Settings" to Icons.Default.Settings
)

/* ---------- sidebar ---------- */

@Composable
private fun Sidebar(sel: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.width(170.dp).fillMaxHeight().enter(0).panel().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Flame(34)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("CANDLE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
                Text("LAUNCHER", color = Orange, fontSize = 8.sp, letterSpacing = 3.sp)
            }
        }
        tabs.forEachIndexed { i, (name, icon) -> NavItem(name, icon, i == sel) { onSelect(i) } }
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, sel: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (sel) Orange.copy(alpha = 0.18f) else Color.Transparent, tween(300), label = "bg")
    val c by animateColorAsState(if (sel) Orange else Color(0xFFD5D9E4), tween(300), label = "c")
    val bar by animateDpAsState(if (sel) 20.dp else 0.dp, spring(Spring.DampingRatioMediumBouncy), label = "bar")
    val nudge by animateDpAsState(if (sel) 4.dp else 0.dp, spring(Spring.DampingRatioMediumBouncy), label = "n")
    Row(
        Modifier.fillMaxWidth().height(38.dp).pressable(onClick).background(bg, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(bar).background(Orange, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Icon(icon, null, tint = c, modifier = Modifier.size(18.dp).offset(x = nudge))
        Spacer(Modifier.width(10.dp))
        Text(label, color = c, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun Flame(dp: Int) {
    val t = rememberInfiniteTransition(label = "flame")
    val s = t.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "s")
    Canvas(Modifier.size(dp.dp)) {
        val w = size.width; val h = size.height
        scale(s.value) {
            drawCircle(Orange.copy(alpha = 0.22f), radius = w * 0.55f)
            val path = Path().apply {
                moveTo(w * .5f, h * .08f)
                cubicTo(w * .92f, h * .42f, w * .85f, h * .92f, w * .5f, h * .92f)
                cubicTo(w * .15f, h * .92f, w * .08f, h * .45f, w * .5f, h * .08f)
                close()
            }
            drawPath(path, Brush.verticalGradient(listOf(Amber, Orange, Color(0xFFE5400A))))
        }
    }
}

/* ---------- top bar ---------- */

@Composable
private fun TopBar() {
    Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.weight(1f).fillMaxHeight().panel().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⚡", color = Orange, fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Text("Ready for your next adventure?", color = Color(0xFFD5D9E4), fontSize = 12.sp)
        }
        Row(Modifier.fillMaxHeight().panel().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).background(Color(0xFFB5703C), RoundedCornerShape(5.dp)))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Tewtone66", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).background(Color(0xFF3DDC84), CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("Online", color = Muted, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Muted, modifier = Modifier.size(16.dp))
        }
    }
}

/* ---------- home ---------- */

@Composable
private fun Home(go: (Int) -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1.7f).fillMaxHeight().enter(1)) { Hero { go(1) } }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.enter(2)) { VersionCard() }
            Column(Modifier.weight(1f).enter(3).panel().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⚡ Quick Access", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tile("Cosmetics", "Customize", Icons.Default.Star, Color(0xFFFF7A1A)) { go(3) }
                    Tile("Mods", "Manage", Icons.Default.Build, Color(0xFF2F8CFF)) { go(2) }
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tile("Packs", "Your world", Icons.Default.Favorite, Color(0xFFA855F7)) { go(4) }
                    Tile("Settings", "Preferences", Icons.Default.Settings, Color(0xFF22D3EE)) { go(5) }
                }
            }
        }
    }
}

@Composable
private fun RowScope.Tile(title: String, sub: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        Modifier.weight(1f).fillMaxHeight().pressable(onClick).background(Color(0x14FFFFFF), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(26.dp).background(color.copy(alpha = 0.22f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(6.dp))
        Column {
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(sub, color = Muted, fontSize = 8.sp, maxLines = 1)
        }
    }
}

@Composable
private fun VersionCard() {
    Row(Modifier.fillMaxWidth().panel().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).background(Brush.verticalGradient(listOf(Color(0xFF6BBF3A), Color(0xFF7A4A24))), RoundedCornerShape(6.dp)))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Game Version", color = Muted, fontSize = 10.sp)
            Text("1.21.11 (Fabric)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Fabric Loader 0.16.10", color = Muted, fontSize = 9.sp)
        }
        Chip("Latest", Color(0xFF1F7A45), Color(0xFFB8FFD6))
    }
}

@Composable
private fun Hero(onLaunch: () -> Unit) {
    val ctx = LocalContext.current
    val imgId = remember { ctx.resources.getIdentifier("hero_bg", "drawable", ctx.packageName) }
    val t = rememberInfiniteTransition(label = "hero")
    val shine = t.animateFloat(-200f, 600f, infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart), label = "shine")
    Box(Modifier.fillMaxSize().panel().clip(RoundedCornerShape(18.dp))) {
        if (imgId != 0) Image(painterResource(imgId), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xCC07090F), Color.Transparent))))
        Column(Modifier.align(Alignment.CenterStart).padding(20.dp)) {
            Chip("BETA", Orange)
            Spacer(Modifier.height(8.dp))
            Text(
                "CANDLE CLIENT", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic,
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(Color.White, Amber, Color.White),
                        start = Offset(shine.value, 0f), end = Offset(shine.value + 260f, 80f)),
                    fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic
                )
            )
            Text("More Than Just a Client", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text("Custom UI, Cosmetics, Mods, and more.", color = Color(0xFFC3C9D8), fontSize = 11.sp)
            Spacer(Modifier.height(14.dp))
            LaunchButton(onLaunch)
        }
    }
}

@Composable
private fun LaunchButton(onClick: () -> Unit) {
    val glow = rememberInfiniteTransition(label = "glow").animateFloat(
        0.15f, 0.55f, infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "g")
    Row(
        Modifier
            .drawBehind {
                val g = 8.dp.toPx()
                drawRoundRect(Orange.copy(alpha = glow.value * 0.5f), Offset(-g, -g),
                    Size(size.width + 2 * g, size.height + 2 * g), CornerRadius(18.dp.toPx()))
                drawRoundRect(Orange.copy(alpha = glow.value), Offset(-g / 2, -g / 2),
                    Size(size.width + g, size.height + g), CornerRadius(15.dp.toPx()))
            }
            .pressable(onClick)
            .background(Brush.horizontalGradient(listOf(Orange, Color(0xFFFFA83D))), RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF1A0E00), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Launch Game  →", color = Color(0xFF1A0E00), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun Placeholder(name: String) {
    Box(Modifier.fillMaxSize().panel(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Flame(48)
            Spacer(Modifier.height(8.dp))
            Text(name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Coming soon", color = Muted, fontSize = 11.sp)
        }
    }
}
