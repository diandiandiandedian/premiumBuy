package com.example.myapplication.utils

import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.SdkException
import com.sunmi.printerx.enums.Align
import com.sunmi.printerx.enums.DividingLine
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.QrStyle
import com.sunmi.printerx.style.TextStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrinterUtils2 {

   private fun getCurrentFormattedTime(): String {
        // 获取当前日期和时间
        val currentDate = Date()
        // 创建指定的格式化模式
        val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
        // 返回格式化后的日期时间字符串
        return dateFormat.format(currentDate)
    }

    private fun generateOrderId(): String {
        // 获取当前日期和时间
        val currentDate = Date()
        // 创建格式化模式，例如：20241104094512（2024年11月4日09点45分12秒）
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        // 格式化当前日期和时间为订单号
        return "ORDER" + dateFormat.format(currentDate)
    }


    // 根据链选择区块浏览器 URL
    private fun getExplorerBaseUrl(chain: String): String {
        return when (chain.lowercase()) {
            "base" -> "https://eth-sepolia.blockscout.com"
            "scroll" -> "https://scroll.blockscout.com" // 示例地址
            "bitkub" -> "https://bitkub.blockscout.com" // 示例地址
            // 其他链的地址
            else -> "https://eth.blockscout.com"
        }
    }

    fun printReceipt(printer: PrinterSdk.Printer, totalPrice: Double, tokenAddress: String, recipientAddress: String,
                     txHash:String,lotteryNumber:String,paymentToken: String,selectedChain: String ) {
        try {
            val api = printer.lineApi()



            // Header Section - ASCII Art
            api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
//            val asciiArt = """
//𝑷𝒓𝒆𝒎𝒊𝒖𝒎𝑩𝒖𝒚
//            """.trimIndent()
            val titleArt = """𝑷𝒓𝒆𝒎𝒊𝒖𝒎𝑩𝒖𝒚 𝑳𝒐𝒕𝒕𝒆𝒓𝒚 𝑺𝒕𝒐𝒓𝒆"""

            api.printText(titleArt, TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(32).enableBold(true))
            api.printDividingLine(DividingLine.EMPTY, 20)

            api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
            val titleArt2 = """𝑻𝒊𝒄𝒌𝒆𝒕"""

            api.printText(titleArt2, TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(32).enableBold(true))
            api.printDividingLine(DividingLine.EMPTY, 40)

            api.autoOut()
        } catch (e: SdkException) {
            e.printStackTrace()
        }
    }
}