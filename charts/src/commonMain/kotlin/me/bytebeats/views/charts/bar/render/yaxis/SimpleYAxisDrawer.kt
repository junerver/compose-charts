package me.bytebeats.views.charts.bar.render.yaxis

/**
 * Created by bytebeats on 2021/9/25 : 14:27
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.bytebeats.views.charts.LabelFormatter
import kotlin.math.roundToInt

typealias LabelFormatter = (Float) -> String

data class SimpleYAxisDrawer(
    val labelTextSize: TextUnit = 12.sp,
    val labelTextColor: Color = Color.Black,
    val drawLabelEvery: Int = 3,
    val labelValueFormatter: LabelFormatter = { value -> "$value" },
    val axisLineThickness: Dp = 1.dp,
    val axisLineColor: Color = Color.Black
) : IYAxisDrawer {

    override fun drawAxisLine(
        drawScope: DrawScope,
        canvas: Canvas,
        drawableArea: Rect
    ) {
        with(drawScope) {
            val lineThickness = axisLineThickness.toPx()
            val x = drawableArea.right - lineThickness / 2F
            canvas.drawLine(
                p1 = Offset(x = x, y = drawableArea.top),
                p2 = Offset(x = x, y = drawableArea.bottom),
                paint = Paint().apply {
                    color = axisLineColor
                    strokeWidth = lineThickness
                }
            )
        }
    }

    override fun drawAxisLabels(
        drawScope: DrawScope,
        canvas: Canvas,
        drawableArea: Rect,
        minValue: Float,
        maxValue: Float,
        textMeasurer: TextMeasurer
    ) {
        with(drawScope) {
            val minLabelHeight = labelTextSize.toPx() * drawLabelEvery.toFloat()
            val totalHeight = drawableArea.height
            val labelCount = (drawableArea.height / minLabelHeight).roundToInt().coerceAtLeast(2)

            for (i in 0..labelCount) {
                val value = minValue + i * (maxValue - minValue) / labelCount
                val label = labelValueFormatter(value)

                val x = drawableArea.right - axisLineThickness.toPx() - labelTextSize.toPx() / 2F
                val y = drawableArea.bottom - i * (totalHeight / labelCount)

                val textLayoutResult = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = labelTextColor,
                        fontSize = labelTextSize
                    )
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x - textLayoutResult.size.width,
                        y - textLayoutResult.size.height / 2F
                    )
                )
            }
        }
    }
}
