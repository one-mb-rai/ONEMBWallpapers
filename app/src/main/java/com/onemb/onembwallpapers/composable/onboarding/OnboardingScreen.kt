package com.onemb.onembwallpapers.composable.onboarding

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(viewModel: WallpaperViewModel) {
    val scope = rememberCoroutineScope()
    val pageState = rememberPagerState(0, pageCount = { 4 })
    val context = LocalContext.current
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HorizontalPager(
            state = pageState,
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
        ) { page ->
            when(page) {
                0 -> FirstPage()
                1 -> SecondPage()
                2 -> ThirdPage()
                3 -> FourthPage()
            }
        }

        BottomSection(size = 4, index = pageState.currentPage) {
            if (pageState.currentPage + 1 < 4) scope.launch {
                pageState.scrollToPage(pageState.currentPage + 1)
            } else {
                viewModel.getSharedPreferences(context).edit().putBoolean("onboardingDone", true).apply()
                viewModel.setOnboarding(true)
            }
        }
    }
}

@Composable
fun BottomSection(size: Int, index: Int, onButtonClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Indicators(size, index)

        ExtendedFloatingActionButton(
            onClick = { onButtonClick() },
            containerColor = Color.Black,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
        ) {
            if(index + 1 != 4) {
                Text(text = "Next", color = Color.White)
            } else {
                Text(text = "Finish", color = Color.White)
            }
            Icon(
                Icons.Outlined.KeyboardArrowRight,
                tint = Color.White,
                contentDescription = "Move to next screen")
        }
    }
}

@Composable
fun BoxScope.Indicators(size: Int, index: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.align(Alignment.CenterStart)
    ) {
        repeat(size) {
            Indicator(isSelected = it == index)
        }
    }
}

@Composable
fun Indicator(isSelected: Boolean) {
    val width = animateDpAsState(
        targetValue = if (isSelected) 25.dp else 10.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )

    Box(
        modifier = Modifier
            .height(10.dp)
            .width(width.value)
            .clip(CircleShape)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0XFFF8E2E7)
            )
    )
}


@Composable
fun headerForPages() {
    Text(
        text = "Welcome to ONEMB Wallpapers",
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.W900,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
fun FirstPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        headerForPages()
        Spacer(modifier = Modifier.height(30.dp))
        Image(
            painter = painterResource(id = R.drawable.list_of_categories),
            contentDescription = "List of Categories image",
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Choose from a variety of wallpaper categories",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W300,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.save_cat_btn),
            contentDescription = "Save button image",
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Clicking on this button will save you choices",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W300,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun SecondPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper_select),
            contentDescription = "Wallpaper selection screen",
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )
        Text(
            text = "This screen shows you a wide variety of fresh wallpapers each time you open app",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W300,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.refresh_btn),
            contentDescription = "Save button image",
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Clicking the refresh button will load a fresh set of wallpapers",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W300,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.back_cat_btn),
            contentDescription = "Save button image",
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        )
        Text(
            text = "You can always go back and update your categories selection",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W300,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun ThirdPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.change_wall),
            contentDescription = "Change Wall every 30 mins",
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
        )
        Text(
            text = "This option requires special notification permission.",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W300,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.permission_screen),
            contentDescription = "Save button image",
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Notification permission allows the app to always be in foreground. This will help app to change wallpaper every 30 minutes",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W300,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.notification),
            contentDescription = "Save button image",
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You can stop the service anytime you want. This will stop the auto wallpaper change",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W300,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun FourthPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Disclaimer!",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = TextUnit(6f, type = TextUnitType.Em),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app uses foreground service to change wallpaper every 30 minutes",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Foreground service will consume battery and phone resources.",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "User is allowed to stop the service from the notification itself any time they want",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "If you don't use the service then you don't need to worry about this disclaimer ",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Thank you",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Justify,
            letterSpacing = TextUnit(value = 4f, TextUnitType.Sp),
            fontSize = TextUnit(6f, type = TextUnitType.Em),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "for using my app.",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Justify,
            letterSpacing = TextUnit(value = 4f, TextUnitType.Sp),
            fontSize = TextUnit(6f, type = TextUnitType.Em),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "It takes a lot of",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Justify,
            letterSpacing = TextUnit(value = 4f, TextUnitType.Sp),
            fontSize = TextUnit(6f, type = TextUnitType.Em),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "effort in",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Justify,
            letterSpacing = TextUnit(value = 4f, TextUnitType.Sp),
            fontSize = TextUnit(6f, type = TextUnitType.Em),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "development",
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Justify,
            letterSpacing = TextUnit(value = 4f, TextUnitType.Sp),
            fontSize = TextUnit(6f, type = TextUnitType.Em),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}