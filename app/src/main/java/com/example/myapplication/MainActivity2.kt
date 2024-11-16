//package com.example.myapplication
//
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import com.example.myapplication.ui.theme.MyApplicationTheme
//import com.google.zxing.BarcodeFormat
//import com.google.zxing.qrcode.QRCodeWriter
//import com.sunmi.printerx.PrinterSdk
//import com.sunmi.printerx.PrinterSdk.PrinterListen
//import com.sunmi.printerx.SdkException
//import com.sunmi.printerx.enums.Align
//import com.sunmi.printerx.enums.DividingLine
//import com.sunmi.printerx.enums.ErrorLevel
//import com.sunmi.printerx.style.BaseStyle
//import com.sunmi.printerx.style.QrStyle
//import com.sunmi.printerx.style.TextStyle
//import java.math.BigDecimal
//import java.math.RoundingMode
//
//class MainActivity : ComponentActivity() {
//
//    var selectPrinter by mutableStateOf<PrinterSdk.Printer?>(null)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            MyApplicationTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    SimpleOrderScreen(selectPrinter)
//                }
//            }
//        }
//        initPrinter()
//    }
//
//    private val mainHandler = Handler(Looper.getMainLooper())
//
//    private fun initPrinter() {
//        try {
//            PrinterSdk.getInstance().getPrinter(this, object : PrinterListen {
//                override fun onDefPrinter(printer: PrinterSdk.Printer) {
//                    selectPrinter = printer
//                    println("打印机已连接")
//                    mainHandler.post {
//                        Toast.makeText(this@MainActivity, "打印机已连接", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onPrinters(printers: List<PrinterSdk.Printer>) {
//                    // 可以选择特定打印机
//                }
//            })
//        } catch (e: Exception) {
//            e.printStackTrace()
//            mainHandler.post {
//                Toast.makeText(this, "打印机初始化失败", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//}
//
//@Composable
//fun SimpleOrderScreen(selectPrinter: PrinterSdk.Printer?) {
//    val prices = mapOf(
//        "ticket: $1" to 1.0,
//        "ticket: $5" to 5.0,
//        "ticket: $10" to 10.0,
//        "ticket: $50" to 50.0
//    )
//    val ticketImages = listOf(
//        R.drawable.ticket1, // Replace with your actual image resources
//        R.drawable.ticket2,
//        R.drawable.ticket3,
//        R.drawable.ticket4
//    )
//    val ticketNames = listOf("ticket: $1", "ticket: $5", "ticket: $10", "ticket: $50")
//
//    var selectedLottery by remember { mutableStateOf("ticket: $1") }
//    var numberInput by remember { mutableStateOf("") }
//    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
//    var selectedToken by remember { mutableStateOf("USDC") }
//    var showConfirmation by remember { mutableStateOf(false) } // 控制提示图片的显示
//
//    val selectedPrice = prices[selectedLottery] ?: 0.0
//    val scrollState = rememberScrollState()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(scrollState),
//        verticalArrangement = Arrangement.Top,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // 输入框在顶部，用于输入彩票号码
//        TextField(
//            value = numberInput,
//            onValueChange = { input ->
//                if (input.toIntOrNull() in 1..99 || input.isEmpty()) {
//                    numberInput = input
//                }
//            },
//            label = { Text("请输入1~99的数字作为彩票号码") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 彩票金额水平排列，每行显示两个选项
//        Text("选择彩票金额")
//        Column {
//            ticketNames.chunked(2).forEachIndexed { rowIndex, rowTickets ->
//                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                    rowTickets.forEachIndexed { index, name ->
//                        val imageResource = ticketImages[rowIndex * 2 + index]
//                        Box(contentAlignment = Alignment.TopEnd) {
//                            Button(
//                                onClick = {
//                                    selectedLottery = name
//                                    showConfirmation = true // 显示提示图片
//                                },
//                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
//                                contentPadding = PaddingValues(0.dp),
//                                modifier = Modifier.size(150.dp) // 放大图片尺寸
//                            ) {
//                                Image(
//                                    painter = painterResource(id = imageResource),
//                                    contentDescription = name
//                                )
//                            }
//                            if (selectedLottery == name && showConfirmation) {
//                                Image(
//                                    painter = painterResource(id = R.drawable.yes),
//                                    contentDescription = "Selected",
//                                    modifier = Modifier.size(25.dp).padding(0.dp).offset(y = 30.dp) // 向下移动 2dp // 控制内边距
//                                )
//                            }
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(0.dp))
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = "总价: $$selectedPrice")
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = "选择支付方式:")
//        Row {
//            listOf("USDT", "USDC", "ETH").forEach { token ->
//                Button(
//                    onClick = { selectedToken = token },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (selectedToken == token) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
//                    ),
//                    modifier = Modifier.padding(4.dp)
//                ) {
//                    Text(token)
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = {
//            val ethAddress = when (selectedToken) {
//                "USDT" -> "0xc2132D05D31c914a87C6611C10748AEb04B58e8F"
//                "USDC" -> "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
//                else -> ""
//            }
//            val recipientAddress = "0xE3a463d743F762D538031BAD3f1E748BB41f96ec"
//            val tokenDecimals = if (selectedToken == "ETH") 18 else if (selectedToken == "USDT") 18 else 18
//            val amountInWei = BigDecimal(selectedPrice).multiply(BigDecimal.TEN.pow(tokenDecimals))
//                .setScale(0, RoundingMode.DOWN).toPlainString()
//            val uri = if (selectedToken == "ETH") {
//                "ethereum:$recipientAddress?value=$amountInWei"
//            } else {
//                "ethereum:$ethAddress/transfer?address=$recipientAddress&uint256=$amountInWei"
//            }
//            qrCodeBitmap = generateQRCode(uri)
//        }) {
//            Text("提交")
//        }
//
//        Button(onClick = {
//            selectPrinter?.let {
//                try {
//                    val api = it.lineApi()
//                    api.initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
//                    api.printText("点点点彩票店", TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(40).enableBold(true))
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 30)
//                    api.printText("Order ID: aaaaaaaaaaaaaa", TextStyle.getStyle())
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 15)
//                    api.printText("**************************", TextStyle.getStyle())
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 10)
//                    api.printText("Ticket:  $numberInput", TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(40).enableBold(true))
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 10)
//                    api.printText("**************************", TextStyle.getStyle())
//                    api.printText("03/01/2024 09:03", TextStyle.getStyle())
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 30)
//                    val leftStyle: TextStyle = TextStyle.getStyle().setAlign(Align.LEFT)
//                    val rightStyle: TextStyle = TextStyle.getStyle().setAlign(Align.RIGHT)
//                    it.lineApi().printTexts(arrayOf("Total", "$selectedPrice"), intArrayOf(1, 2), arrayOf(leftStyle, rightStyle))
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 5)
//                    it.lineApi().printTexts(
//                        arrayOf("Subtotal", "$4.00"),
//                        intArrayOf(1, 2),
//                        arrayOf(leftStyle, rightStyle)
//                    )
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 5)
//                    it.lineApi().printTexts(
//                        arrayOf("Rate", "$71000"),
//                        intArrayOf(1, 2),
//                        arrayOf(leftStyle, rightStyle)
//                    )
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 5)
//                    it.lineApi().printTexts(
//                        arrayOf("Paid", "0.004 ETH"),
//                        intArrayOf(1, 2),
//                        arrayOf(leftStyle, rightStyle)
//                    )
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 5)
//                    it.lineApi().printTexts(
//                        arrayOf("Destination", "0xE3a463d743F762D538031BAD3f1E748BB41f96ec"),
//                        intArrayOf(1, 2),
//                        arrayOf(leftStyle, rightStyle)
//                    )
//
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 10)
//                    it.lineApi().printTexts(
//                        arrayOf("txHash", "0x74ec0a784dfa8c26d329576ceb09237a8f21a87e7b6bb60fc51d8d8f4e26e3a8"),
//                        intArrayOf(1, 2),
//                        arrayOf(leftStyle, rightStyle.enableUnderline(true))
//                    )
//
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 50)
//                    it.lineApi().printQrCode("https://eth.blockscout.com/tx/0x74ec0a784dfa8c26d329576ceb09237a8f21a87e7b6bb60fc51d8d8f4e26e3a8",
//                        QrStyle.getStyle().setDot(6).setAlign(Align.CENTER).setErrorLevel(ErrorLevel.M))
//                    it.lineApi().printDividingLine(DividingLine.EMPTY, 30)
//                    api.printText("Powered by dian", TextStyle.getStyle().setAlign(Align.CENTER).setTextSize(20).enableBold(true))
//                    api.autoOut()
//                } catch (e: SdkException) {
//                    e.printStackTrace()
//                }
//            } ?: println("打印机未连接")
//        }) {
//            Text("打印")
//        }
//
//        qrCodeBitmap?.let { bitmap ->
//            Image(
//                bitmap = bitmap.asImageBitmap(),
//                contentDescription = "QR Code",
//                modifier = Modifier.size(300.dp).padding(16.dp)
//            )
//        }
//    }
//}
//
//fun generateQRCode(text: String): Bitmap? {
//    return try {
//        val writer = QRCodeWriter()
//        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 1024, 1024)
//        val width = bitMatrix.width
//        val height = bitMatrix.height
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
//        for (x in 0 until width) {
//            for (y in 0 until height) {
//                bitmap.setPixel(
//                    x,
//                    y,
//                    if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
//                )
//            }
//        }
//        bitmap
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}