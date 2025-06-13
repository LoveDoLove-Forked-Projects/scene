package com.bytedance.scene.navigation.configuration

import android.content.res.Configuration

/**
 * Created by jiangqi on 2024/11/5
 * @author jiangqi@bytedance.com
 */
fun createConfigurationForDayNight(
    isNight: Boolean
): Configuration {
//    val newNightMode: Int = when (isNight) {
//        true -> Configuration.UI_MODE_NIGHT_YES
//        false -> Configuration.UI_MODE_NIGHT_NO
//    }
//    val configuration = Configuration()
//    configuration.uiMode = newNightMode or (0 and Configuration.UI_MODE_NIGHT_MASK.inv())
    val newNightMode: Int = when (isNight) {
        true -> 33
        false -> 17
    }
    val configuration = Configuration()
    configuration.uiMode = newNightMode
    return configuration
}

fun createConfigurationForOrientation(
    isPortrait: Boolean
): Configuration {
    val newOrientation: Int = when (isPortrait) {
        true -> Configuration.ORIENTATION_PORTRAIT
        false -> Configuration.ORIENTATION_LANDSCAPE
    }
    val configuration = Configuration()
    configuration.orientation = newOrientation
    return configuration
}

fun getNightMode(configuration: Configuration): Int {
    return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
}
