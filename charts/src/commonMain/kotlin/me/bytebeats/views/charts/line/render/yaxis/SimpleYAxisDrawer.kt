package me.bytebeats.views.charts.line.render.yaxis

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.bytebeats.views.charts.LabelFormatter
import kotlin.math.roundToInt

/**
 * Created by bytebeats on 2021/9/24 : 21:08
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

data class SimpleYAxisDrawer(
    val labelTextSize: TextUnit = 12.sp,
    val labelTextColor: Color = Color.Black,
    val drawLabelEvery: Int = 1,
    val labelValueFormatter: LabelFormatter = { value -> "$value" },
    val axisLineThickness: Dp = 1.dp,
    val axisLineColor: Color = Color.Black,
) : IYAxisDrawer {
    private val mAxisLinePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = axisLineColor
            style = PaintingStyle.Stroke
        }
    }

    override fun drawAxisLine(
        drawScope: DrawScope,
        canvas: Canvas,
        drawableArea: Rect
    ) = with(drawScope) {
        val lineThickness = axisLineThickness.toPx()
        val x = drawableArea.right - lineThickness / 2F
        canvas.drawLine(
            p1 = Offset(x = x, y = drawableArea.top),
            p2 = Offset(x = x, y = drawableArea.bottom),
            paint = mAxisLinePaint.apply { strokeWidth = lineThickness })
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
