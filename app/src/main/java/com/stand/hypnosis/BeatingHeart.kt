package com.stand.hypnosis

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource

@Composable
fun BeatingHeart(
    beating: Boolean,
    modifier: Modifier = Modifier
) {
    // 1. 无限循环动画，只在 beating==true 时运行
    val infinite = rememberInfiniteTransition(label = "infinite")

    // 2. 当 beating 为 true 才计算动画值，否则固定 1f
    val scale by if (beating) {
        infinite.animateFloat(
            initialValue = 1f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    0.8f at 500
                    1f at 1000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "scale"
        )
    } else {
        // 停止时直接返回 1f，不启动无限动画
        remember { mutableFloatStateOf(1f) }
    }

    Image(
        painter = painterResource(id = R.drawable.heart),
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth(0.6f)
            .aspectRatio(1f)
            .scale(scale)
    )
}