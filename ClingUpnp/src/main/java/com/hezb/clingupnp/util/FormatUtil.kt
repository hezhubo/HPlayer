package com.hezb.clingupnp.util

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * Project Name: HPlayer
 * File Name:    FormatUtil
 *
 * Description: 格式化工具类.
 *
 * @author  hezhubo
 * @date    2022年03月03日 09:24
 */
object FormatUtil {

    /**
     * 格式化文件大小
     *
     * @param size
     * @return
     */
    @JvmStatic
    fun formatFileSize(size: Long): String {
        if (size <= 0) {
            return "0 B"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        val digitString =
            DecimalFormat("#,##0.00").format(size / 1024.0.pow(digitGroups.toDouble()))
        return "$digitString ${units[digitGroups]}"
    }

    /**
     * 格式化时间
     *
     * @param timeMs
     * @param alwaysShowHour
     * @return
     */
    @JvmStatic
    @JvmOverloads
    fun formatTime(timeMs: Long, alwaysShowHour: Boolean = false): String {
        val totalSeconds = (timeMs / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0 || alwaysShowHour) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * 转换时间为毫秒
     *
     * @param formatTime 格式化的时间，DLNA传输的数据中有的秒可能是小数
     */
    @JvmStatic
    fun transformTime(formatTime: String?): Long {
        if (formatTime.isNullOrEmpty()) {
            return 0
        }
        val splitArray = formatTime.split(":")
        return when (splitArray.size) {
            1 -> {
                (splitArray[0].toDouble() * 1000).toLong()
            }
            2 -> {
                ((splitArray[0].toInt() * 60 + splitArray[1].toDouble()) * 1000).toLong()
            }
            3 -> {
                ((splitArray[0].toInt() * 3600 + splitArray[1].toInt() * 60 + splitArray[2].toDouble()) * 1000).toLong()
            }
            else -> {
                0
            }
        }
    }

}