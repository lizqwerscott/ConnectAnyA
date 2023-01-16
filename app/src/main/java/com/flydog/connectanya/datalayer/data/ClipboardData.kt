package com.flydog.connectanya.datalayer.data

import com.flydog.connectanya.datalayer.model.Clipboard
import com.flydog.connectanya.datalayer.model.Device

data class ClipboardData(
    val device: Device,
    val clipboardData: Clipboard
)