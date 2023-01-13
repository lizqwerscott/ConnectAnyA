package com.flydog.connectanya.utils

import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern


object ClipboardUtil {

    private fun getLog(aboutEvent: String, filter: String = "TriggerManager"): List<String> {
        val results: MutableList<String> = mutableListOf()
        try {
            val cmdLine = ArrayList<String>() //设置命令   logcat -d 读取日志
            cmdLine.add("logcat")
            cmdLine.add("-s")
            cmdLine.add(filter)
            cmdLine.add("-d")

            val clearLog = ArrayList<String>() //设置命令  logcat -c 清除日志
            clearLog.add("logcat")
            clearLog.add("-c")

            val process = Runtime.getRuntime().exec(cmdLine.toTypedArray()) //捕获日志
            val bufferedReader =
                BufferedReader(InputStreamReader(process.inputStream)) //将捕获内容转换为BufferedReader

            var str: String = ""
            var isMultiLine = false
            while (bufferedReader.readLine().also { str = it } != null) //开始读取日志，每次读取一行
            {
                Runtime.getRuntime()
                    .exec(clearLog.toTypedArray()) //清理日志....这里至关重要，不清理的话，任何操作都将产生新的日志，代码进入死循环，直到bufferreader满
                Log.i("Log", "add log: $str")
                str.let {
                    if (isMultiLine) {
                        results.add(it)
                        if (it.contains("label")) {
                            isMultiLine = false
                        }
                    } else {
                        if (it.contains(aboutEvent)) {
                            results.add(it)
                            isMultiLine = !it.contains("label")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    private fun removeLogHeader(texts: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (item in texts) {
            val s = item.split("TriggerManager: ")
            if (s.isEmpty()) {
                continue
            }
            if (s.size == 1) {
                result.add("")
            } else {
                result.add(s[1])
            }
        }
        return result
    }

    private fun handleLog(texts: List<String>): String {
        var res = ""
        if (texts.size == 1) {
            val pattern = Pattern.compile("\\{([^}]*)\\}")
            val matcher = pattern.matcher(texts[0])
            if (matcher.find()) {
                val key_values =
                    matcher.group().substring(1, matcher.group().length - 1).split(", ")
                for (key_value in key_values) {
                    val key_temp = key_value.split("=")
                    if (key_temp[0] == "text") {
                        res = key_temp[1].substring(1, key_temp[1].length - 1)
                        break
                    }
                }
            }
        } else if (texts.size > 1) {
            val centerTexts = mutableListOf<String>()
            val header = texts.first().substring(texts.first().indexOf("text=") + 6)
            centerTexts.add(header)
            for (i in 1 until texts.size - 1) {
                centerTexts.add(texts[i])
            }
            val end = texts.last().substring(0, texts.last().indexOfLast { it == ',' } - 1)
            centerTexts.add(end)
            res = centerTexts.joinToString("\n")
        }
        return res
    }

    fun getClipboardText(): String {
        return handleLog(removeLogHeader(getLog("notifyEvent:EVENT_CLIPBOARD")))
    }

    fun getClipboardTextFront(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClip!!.itemCount > 0) {
            return clipboard.primaryClip?.getItemAt(0)?.text.toString()
        }
        return ""
    }

}