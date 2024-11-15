package me.bytebeats.views.charts.bar.render.label

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import me.bytebeats.views.charts.AxisLabelFormatter
import me.bytebeats.views.charts.util.FLOAT_1_5

/**
 * Created by bytebeats on 2021/9/25 : 14:01
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
data class SimpleLabelDrawer(
    val drawLocation: DrawLocation = DrawLocation.Inside,
    val labelTextSize: TextUnit = 12.sp,
    val labelTextColor: Color = Color.Black,
    val axisLabelFormatter: AxisLabelFormatter = { value -> "$value" }
) : ILabelDrawer {
    private val mLabelTextArea: Float? = null
    override fun requiredAboveBarHeight(drawScope: DrawScope): Float = when (drawLocation) {
        DrawLocation.Outside -> FLOAT_1_5 * labelTextHeight(drawScope)
        else -> 0F
    }

    override fun requiredXAxisHeight(drawScope: DrawScope): Float = when (drawLocation) {
        DrawLocation.XAxis -> labelTextHeight(drawScope)
        else -> 0F
    }

    override fun drawLabel(
        drawScope: DrawScope,
        canvas: Canvas,
        label: Any?,
        barArea: Rect,
        xAxisArea: Rect,
        textMeasurer: TextMeasurer
    ) {
        with(drawScope) {
            val xCenter = barArea.left + barArea.width / 2
            val yCenter = when (drawLocation) {
                DrawLocation.Inside -> (barArea.top + barArea.bottom) / 2
                DrawLocation.Outside -> barArea.top - labelTextSize.toPx() / 2
                DrawLocation.XAxis -> barArea.bottom + labelTextHeight(drawScope)
            }

            val labelValue = axisLabelFormatter(label)

            // 使用 TextMeasurer 测量和绘制文本
            val textLayoutResult = textMeasurer.measure(
                text = labelValue,
                style = TextStyle(
                    color = labelTextColor,
                    fontSize = labelTextSize
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    xCenter - textLayoutResult.size.width / 2,
                    yCenter - textLayoutResult.size.height / 2
                )
            )
        }
    }

    private fun labelTextHeight(drawScope: DrawScope): Float = with(drawScope) {
        mLabelTextArea ?: (FLOAT_1_5 * labelTextSize.toPx())
    }


    enum class DrawLocation {
        Inside,
        Outside,
        XAxis;
    }
}
