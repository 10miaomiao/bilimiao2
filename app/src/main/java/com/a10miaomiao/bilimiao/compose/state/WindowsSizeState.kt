package com.a10miaomiao.bilimiao.compose.state

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
var windowSize by mutableStateOf(WindowSizeClass.calculateFromSize(DpSize.Zero))