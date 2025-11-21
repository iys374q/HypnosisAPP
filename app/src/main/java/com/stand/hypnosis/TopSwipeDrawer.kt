package com.stand.hypnosis

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


/**
 * 下滑呼出 / 上滑收起 抽屉
 */
@Composable
fun TopSwipeDrawerLayer(
    modifier: Modifier = Modifier,
    configManager: ConfigManager,
    onConfigUpdate: (() -> Unit)? = null  // 添加这个参数
) {
    // 使用 mutableStateOf 来管理配置状态
    var isBeating by remember { mutableStateOf(configManager.isBeating) }
    var isFullScreen by remember { mutableStateOf(configManager.isFullScreen) }
    var ringColor by remember { mutableIntStateOf(configManager.ringColor) }
    var bgColor by remember { mutableIntStateOf(configManager.bgColor) }

    // 当配置改变时，直接更新 ConfigManager
    fun updateIsBeating(value: Boolean) {
        isBeating = value
        configManager.isBeating = value
        onConfigUpdate?.invoke()  // 通知外部配置已更新
    }

    fun updateIsFullScreen(value: Boolean) {
        isFullScreen = value
        configManager.isFullScreen = value
        onConfigUpdate?.invoke()  // 通知外部配置已更新
    }

    fun updateRingColor(value: Int) {
        ringColor = value
        configManager.ringColor = value
        onConfigUpdate?.invoke()
    }

    fun updateBgColor(value: Int) {
        bgColor = value
        configManager.bgColor = value
        onConfigUpdate?.invoke()
    }

    // -------------------- 内部状态 --------------------

    var isVisible by remember { mutableStateOf(false) }
    val start = 200

    // 使用像素偏移而不是百分比，更精确控制
    var drawerHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    // 添加滑动相关状态
    //var isDragging by remember { mutableStateOf(false) }

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else -drawerHeight.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    // 使用两个动画分别控制拉伸和恢复
    var stretchTarget by remember { mutableFloatStateOf(0f) }
    val stretchFactor by animateFloatAsState(
        targetValue = stretchTarget,
        animationSpec = tween(
            durationMillis = 300,
            easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 1.5f)
        )
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // 先快速拉伸
            stretchTarget = 0.3f
            delay(200) // 保持拉伸状态一段时间
            // 然后弹性恢复
            stretchTarget = 0f
        } else {
            stretchTarget = 0f
        }
    }

    // 颜色选择器状态
    var showRingColorPicker by remember { mutableStateOf(false) }
    var showBgColorPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        //isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        // 如果下滑超过阈值，打开抽屉
                        if (dragAmount.y > 20f) {
                            isVisible = true
                        }
                        // 如果上滑超过阈值，关闭抽屉
                        else if (dragAmount.y < -20f && isVisible) {
                            isVisible = false
                        }
                    },
                    onDragEnd = {
                        //isDragging = false
                    }
                )
            }
    ) {
        // 抽屉 - 关键修改：先测量高度，再应用偏移
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onSizeChanged { size ->
                    drawerHeight = size.height
                }
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .background(Color.Transparent) // 容器透明
        ) {
            // 内容区域使用半透明黑色，底部添加圆弧和拉伸效果
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // 只在Y轴底部拉伸
                        scaleY = 1f + stretchFactor
                        transformOrigin = TransformOrigin(0.5f, 0f) // 从底部中心拉伸
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x99000000), Color(0xBB000000)),
                            startY = 0f,
                            endY = 100f
                        ),
                        shape = object : Shape {
                            override fun createOutline(
                                size: Size,
                                layoutDirection: LayoutDirection,
                                density: Density
                            ): Outline {
                                val path = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    lineTo(size.width, size.height - 30f)
                                    // 使用二次贝塞尔曲线创建圆弧，考虑拉伸因子
                                    quadraticTo(
                                        size.width * 0.5f, size.height + 30f * (1f + stretchFactor),
                                        0f, size.height - 30f
                                    )
                                    close()
                                }
                                return Outline.Generic(path)
                            }
                        }
                    )
            ){
                Column{
                    Text("设置面板", style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(top = 35.dp, start = 16.dp))
                    Text("往下滑动打开，往上滑动关闭",
                        color = Color.White,
                        modifier = Modifier.padding(top = 10.dp, start = 16.dp, bottom = 20.dp))

                    // 为每个配置项添加依次进入动画
                    val settingItems = listOf<@Composable () -> Unit>(
                        // 心跳动画开关
                        {
                            SwitchSettingItem(
                                title = "心跳动画",
                                checked = isBeating,
                                onCheckedChange = { newValue ->
                                    updateIsBeating(newValue)
                                }
                            )
                        },
                        {
                            SwitchSettingItem(
                                title = "全屏",
                                checked = isFullScreen,
                                onCheckedChange = { newValue ->
                                    updateIsFullScreen(newValue)
                                }
                            )
                        },
                        // 圆环颜色选择
                        {
                            ColorSettingItem(
                                title = "动画颜色",
                                color = ringColor,
                                onColorClick = { showRingColorPicker = true  }
                            )
                        },
                        // 背景颜色选择
                        {
                            ColorSettingItem(
                                title = "背景颜色",
                                color = bgColor,
                                onColorClick = { showBgColorPicker = true }
                            )
                        }
                        // 添加更多配置项...
                    )
                    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
                    settingItems.forEachIndexed { index, itemContent ->
                        AnimatedSettingItem(
                            index = index,
                            isVisible = isVisible,
                            screenWidthPx = screenWidthPx,
                            start = start
                        ) {
                            itemContent()  // 渲染具体的配置组件
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val uriHandler = LocalUriHandler.current

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 第一行：版权信息
                            Row {
                                Text(
                                    text = "Copyright © 2025 ",
                                    color = Color.White.copy(0.7f),
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "Stand",
                                    color = Color(0xFF29C6FF),
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri("https://space.bilibili.com/382365750")
                                    }
                                )
                            }

                            // 第二行：GitHub 链接
                            Text(
                                text = "GitHub开源地址",
                                color = Color(0xFF6CC644), // GitHub 绿色
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clickable {
                                        uriHandler.openUri("https://github.com/Stand404/HypnosisAPP")
                                    }
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // 颜色选择器对话框
            if (showRingColorPicker) {
                ColorPickerDialog(
                    currentColor = ringColor,
                    onColorSelected = { newColor:Int ->
                        updateRingColor(newColor)
                        showRingColorPicker = false
                    },
                    onDismiss = { showRingColorPicker = false }
                )
            }

            if (showBgColorPicker) {
                ColorPickerDialog(
                    currentColor = bgColor,
                    onColorSelected = { newColor:Int ->
                        updateBgColor(newColor)
                        showBgColorPicker = false
                    },
                    onDismiss = { showBgColorPicker = false }
                )
            }
        }
    }
}

@Composable
fun AnimatedSettingItem(
    index: Int,
    isVisible: Boolean,
    screenWidthPx: Float,
    start: Int,
    content: @Composable () -> Unit  // 改为接收Composable内容
) {
    val targetOffsetX by animateFloatAsState(
        targetValue = if (isVisible) 0f else screenWidthPx,
        animationSpec = if (isVisible) {
            tween(
                durationMillis = 200,
                delayMillis = start + index * 150,
                easing = FastOutSlowInEasing
            )
        } else {
            tween(durationMillis = 150, easing = FastOutSlowInEasing)
        },
        label = "slide_in_animation_$index"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = targetOffsetX
            }
    ) {
        content()  // 在这里渲染具体的配置组件

    }
}

// 开关配置项
@Composable
fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f) )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// 颜色预览配置项
@Composable
fun ColorSettingItem(
    title: String,
    color: Int,
    onColorClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onColorClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f) )
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Color(color), CircleShape)
        )
    }
}

@Composable
fun ColorPickerDialog(
    currentColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember {
        mutableFloatStateOf((currentColor shr 16 and 0xff) / 255f)
    }
    var green by remember {
        mutableFloatStateOf((currentColor shr 8 and 0xff) / 255f)
    }
    var blue by remember {
        mutableFloatStateOf((currentColor and 0xff) / 255f)
    }

    val selectedColor = remember(red, green, blue) {
        Color(red, green, blue)
    }

    val hexValue = remember(red, green, blue) {
        val r = (red * 255).toInt()
        val g = (green * 255).toInt()
        val b = (blue * 255).toInt()
        String.format("#%02X%02X%02X", r, g, b)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色")},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // 当前颜色预览
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(selectedColor)
                ) {
                    Text(
                        text = hexValue,
                        modifier = Modifier.align(Alignment.Center),
                        color = if (selectedColor.luminance() > 0.5f) Color.Black else Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 红色通道
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "红",
                        modifier = Modifier.width(24.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Slider(
                        value = red,
                        onValueChange = { red = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(red * 255).toInt()}",
                        modifier = Modifier.width(30.dp)
                    )
                }

                // 绿色通道
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "绿",
                        modifier = Modifier.width(24.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(green * 255).toInt()}",
                        modifier = Modifier.width(30.dp)
                    )
                }

                // 蓝色通道
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "蓝",
                        modifier = Modifier.width(24.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(blue * 255).toInt()}",
                        modifier = Modifier.width(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 快速预设颜色
                val presetColors = listOf(
                    0xFFFF0000 to "红", 0xFF00FF00 to "绿", 0xFF0000FF to "蓝",
                    0xFFFFFF00 to "黄", 0xFFFF00FF to "紫", 0xFF00FFFF to "青",
                    0xFFFFFFFF to "白", 0xFF000000 to "黑", 0xFFFFA500 to "橙",
                    0xFF808080 to "灰", 0xFFA52A2A to "棕", 0xFFFFC0CB to "粉"
                )

                Text("快速选择:", modifier = Modifier.padding(vertical = 8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetColors) { (color, name) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(color))
                                    .border(
                                        2.dp,
                                        if (color == selectedColor.toArgb()
                                                .toLong()
                                        ) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable {
                                        red = ((color shr 16) and 0xff) / 255f
                                        green = ((color shr 8) and 0xff) / 255f
                                        blue = (color and 0xff) / 255f
                                    }
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss
                onColorSelected(selectedColor.toArgb())}) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        titleContentColor = MaterialTheme.colorScheme.secondary,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
