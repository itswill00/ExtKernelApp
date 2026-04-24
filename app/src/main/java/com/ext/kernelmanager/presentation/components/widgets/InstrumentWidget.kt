package com.ext.kernelmanager.presentation.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * InstrumentWidget: The base interface for all hardware instruments.
 * Ensures every UI component is modular, efficient, and standardized.
 */
interface InstrumentWidget {
    val title: String
    val priority: Int // Determines position in the grid
    
    @Composable
    fun Render(modifier: Modifier)
}

/**
 * WidgetCategory: Defines the grouping for the dynamic dashboard.
 */
enum class WidgetCategory {
    IDENTITY,
    PERFORMANCE,
    RESOURCES,
    THERMAL,
    ANALYTICS
}
