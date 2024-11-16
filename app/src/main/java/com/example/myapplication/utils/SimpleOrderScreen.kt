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

data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double
)

object PrinterUtils {

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
            "base" -> "https://base-sepolia.blockscout.com"
            "celo" -> "https://celo-alfajores.blockscout.com/"
            "scroll" -> "https://scroll.blockscout.com" // 示例地址
            "flow" -> "https://evm-testnet.flowscan.io/" // 示例地址
            "morph" -> "https://explorer-holesky.morphl2.io/" // 示例地址
            // 其他链的地址
            else -> "https://eth.blockscout.com"
        }
    }



    fun printReceipt(printer: PrinterSdk.Printer, totalPrice: Double, tokenAddress: String, recipientAddress: String,
                     txHash:String,lotteryNumber:String,paymentToken: String,selectedChain: String,orderItems: List<OrderItem>,
                     fromAddress: String,notaryUrl:String  ) {
        try {
            val api = printer.lineApi()


            // Header Section
//            api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
//            api.printText(
//                "DianDian Lottery Store",
//                TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(40).enableBold(true)
//            )
            api.printDividingLine(DividingLine.EMPTY, 30)
            api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
            val titleArt = """𝑷𝒓𝒆𝒎𝒊𝒖𝒎𝑩𝒖𝒚 𝑳𝒐𝒕𝒕𝒆𝒓𝒚 𝑺𝒕𝒐𝒓𝒆"""
            api.printText(titleArt, TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(33).enableBold(true))

            api.printDividingLine(DividingLine.EMPTY, 30)

            // Order Information
            api.printText("""𝑂𝑟𝑑𝑒𝑟 𝐼𝐷："""+generateOrderId(), TextStyle.getStyle())
            api.printDividingLine(DividingLine.EMPTY, 15)
            api.printText("**************************", TextStyle.getStyle())
            api.printDividingLine(DividingLine.EMPTY, 10)
            api.printText(
                """𝑻𝒊𝒄𝒌𝒆𝒕： $lotteryNumber""",
                TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(40).enableBold(true)
            )
            api.printDividingLine(DividingLine.EMPTY, 10)
            api.printText("**************************", TextStyle.getStyle())
            api.printText(getCurrentFormattedTime(), TextStyle.getStyle())
            api.printDividingLine(DividingLine.EMPTY, 30)


            // 商品信息
            // 打印订单项
            orderItems.forEach { item ->
                api.printTexts(
                    arrayOf("${item.name} x${item.quantity}", "${item.price * item.quantity} $paymentToken"),
                    intArrayOf(1, 2),
                    arrayOf(TextStyle.getStyle().setAlign(Align.LEFT), TextStyle.getStyle().setAlign(Align.RIGHT))
                )
            }

            api.printDividingLine(DividingLine.EMPTY, 10)
            // Transaction Details
            val leftStyle: TextStyle = TextStyle.getStyle().setAlign(Align.LEFT)
            val rightStyle: TextStyle = TextStyle.getStyle().setAlign(Align.RIGHT)
            api.printTexts(arrayOf("""𝑇𝑜𝑡𝑎𝑙""", "$totalPrice $paymentToken"), intArrayOf(1, 2), arrayOf(leftStyle, rightStyle))
            api.printDividingLine(DividingLine.EMPTY, 5)
//            api.printTexts(arrayOf("Subtotal", "$4.00"), intArrayOf(1, 2), arrayOf(leftStyle, rightStyle))
//            api.printDividingLine(DividingLine.EMPTY, 5)
//            api.printTexts(arrayOf("Rate", "$71000"), intArrayOf(1, 2), arrayOf(leftStyle, rightStyle))
//            api.printDividingLine(DividingLine.EMPTY, 5)
            api.printTexts(arrayOf("""𝑃𝑎𝑖𝑑""", "$totalPrice $paymentToken"), intArrayOf(1, 2), arrayOf(leftStyle, rightStyle))
            api.printDividingLine(DividingLine.EMPTY, 5)
            api.printTexts(arrayOf("""𝑇𝑜""", recipientAddress), intArrayOf(1, 2), arrayOf(leftStyle, rightStyle))
            api.printDividingLine(DividingLine.EMPTY, 10)
            api.printTexts(
                arrayOf("""𝑡𝑥𝐻𝑎𝑠ℎ""", txHash),
                intArrayOf(1, 2),
                arrayOf(leftStyle, rightStyle.enableUnderline(true))
            )
            api.printDividingLine(DividingLine.EMPTY, 50)

            // QR Code Section
            api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
            api.printText("""𝐵𝑙𝑜𝑐𝑘𝑠𝑐𝑜𝑢𝑡 𝑇𝑋""", TextStyle.getStyle())
            api.printDividingLine(DividingLine.EMPTY, 10)
            val explorerBaseUrl = getExplorerBaseUrl(selectedChain) // 获取区块浏览器地址
            api.printQrCode(
                "$explorerBaseUrl/tx/$txHash",
                QrStyle.getStyle().setDot(6).setAlign(Align.CENTER)
            )
            api.printDividingLine(DividingLine.EMPTY, 30)

            // QR Code Section
            api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
            api.printText("""𝑦𝑜𝑢 𝑐𝑎𝑛 𝑐ℎ𝑒𝑐𝑘 𝑡𝑖𝑐𝑘𝑒𝑡 𝑟𝑒𝑠𝑢𝑙𝑡""", TextStyle.getStyle())
            api.printDividingLine(DividingLine.EMPTY, 10)
            // 使用 selectedChain 作为 mode，拼接 fromAddress 参数
            val countdownUrl = "https://premium-buy.vercel.app/countdown?mode=$selectedChain&fromAddress=$fromAddress"
            api.printQrCode(
                countdownUrl,
                QrStyle.getStyle().setDot(6).setAlign(Align.CENTER)
            )

            api.printDividingLine(DividingLine.EMPTY, 40)
            api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
            api.printText("""𝑆𝑖𝑔𝑛 𝑃𝑟𝑜𝑡𝑜𝑐𝑜𝑙 𝐴𝑡𝑡𝑒𝑠𝑡𝑎𝑡𝑖𝑜𝑛""", TextStyle.getStyle())
            api.printDividingLine(DividingLine.EMPTY, 10)
            api.printQrCode(notaryUrl, QrStyle.getStyle().setDot(6).setAlign(Align.CENTER))


//            api.printQrCode(
//                "https://premium-buy.vercel.app/countdown",
//                QrStyle.getStyle().setDot(6).setAlign(Align.CENTER)
//            )
            api.printDividingLine(DividingLine.EMPTY, 40)

            // Footer
//            api.printText(
//                "Powered by PremiumBuy",
//                TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(20).enableBold(true)
//            )

            api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
            val asciiArt = """
                𝑷𝒓𝒆𝒎𝒊𝒖𝒎𝑩𝒖𝒚
            """.trimIndent()
            api.printText(asciiArt, TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(26).enableBold(true))


            api.autoOut()
        } catch (e: SdkException) {
            e.printStackTrace()
        }
    }
}