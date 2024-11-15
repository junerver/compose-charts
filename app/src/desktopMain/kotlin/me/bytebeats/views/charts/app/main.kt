package me.bytebeats.views.charts.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.bytebeats.views.charts.app.ui.ComposeCharts

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ComposeHooks"
    ) {
        ComposeCharts()
    }
}
