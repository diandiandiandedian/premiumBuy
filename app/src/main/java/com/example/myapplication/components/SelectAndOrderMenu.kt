package com.example.myapplication.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.network.AssignLotteryRequest
import com.example.myapplication.network.NotaryRequest
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.TokenTransferRequest
import com.example.myapplication.utils.OrderItem
import com.example.myapplication.utils.PrinterUtils
import com.example.myapplication.utils.PrinterUtils2
import com.example.myapplication.utils.generateQRCode
import com.google.gson.Gson
import com.sunmi.printerx.PrinterSdk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random
@Composable
fun SelectAndOrderMenu(
    selectedChain: String,
    onChainSelected: (String) -> Unit,
    chainIcons: Map<String, Int>,
    tokenAddresses: Map<String, Pair<String, String>>,
    recipientAddresses: Map<String, String>,
    productImages: Map<String, Int>,
    prices: Map<String, Double>,
    selectPrinter: PrinterSdk.Printer?,
    qrCodeBitmap: Bitmap?,
    onQrCodeGenerated: (Bitmap?) -> Unit,
    showDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit,
    selectedToken: String,
    onTokenSelected: (String) -> Unit,
    totalPrice: Double
) {
    var burgerCount by remember { mutableStateOf(0) }
    var friesCount by remember { mutableStateOf(0) }
    var colaCount by remember { mutableStateOf(0) }
    var chickenCount by remember { mutableStateOf(0) }
    var skewersCount by remember { mutableStateOf(0) }

    val orderItems = remember {
        mutableStateListOf<OrderItem>()
    }
    val coroutineScope = rememberCoroutineScope()






    // 监听商品数量变化逻辑保持不变...
    LaunchedEffect(burgerCount, friesCount, colaCount, chickenCount, skewersCount) {
        orderItems.clear()
        if (burgerCount > 0) orderItems.add(OrderItem("Burger", burgerCount, prices["Burger"]!!))
        if (friesCount > 0) orderItems.add(OrderItem("Fries", friesCount, prices["Fries"]!!))
        if (colaCount > 0) orderItems.add(OrderItem("Cola", colaCount, prices["Cola"]!!))
        if (chickenCount > 0) orderItems.add(OrderItem("Chicken", chickenCount, prices["Chicken"]!!))
        if (skewersCount > 0) orderItems.add(OrderItem("Skewers", skewersCount, prices["Skewers"]!!))
    }

    val currentTotalPrice = (burgerCount * prices["Burger"]!! +
            friesCount * prices["Fries"]!! +
            colaCount * prices["Cola"]!! +
            chickenCount * prices["Chicken"]!! +
            skewersCount * prices["Skewers"]!!)

    var selectedTokenInternal by remember { mutableStateOf(selectedToken) }
    var selectedChainInternal by remember { mutableStateOf(selectedChain) }






    var continueRequesting by remember { mutableStateOf(true) }

    // Add back the polling logic
    LaunchedEffect(showDialog) {
        if (showDialog) {
            continueRequesting = true
            while (continueRequesting) {
                delay(1000L)  // 仅使用 delay，不启动额外的协程
                val value = BigDecimal(currentTotalPrice).multiply(BigDecimal.TEN.pow(18))
                    .setScale(0, RoundingMode.DOWN).toPlainString()
                val tokenAddress = if (selectedTokenInternal == "USDC") {
                    tokenAddresses[selectedChainInternal]?.first ?: ""
                } else {
                    tokenAddresses[selectedChainInternal]?.second ?: ""
                }
                val response = try {
                    val recipientAddress = recipientAddresses[selectedChainInternal] ?: ""
                    val mode = selectedChainInternal.lowercase()

                    RetrofitClient.apiService.getTokenTransfers(
                        mode = mode,
                        value = value,
                        toAddress = recipientAddress.lowercase(),
                        tokenAddress = tokenAddress
                    )
                } catch (e: Exception) {
                    println(e)
                    null
                }

                response?.let { apiResponse ->
                    if (apiResponse.success) {
                        val dataList = apiResponse.data
                        dataList.firstOrNull()?.let { tokenTransfer ->
                            selectPrinter?.let { printer ->
                                val lotteryNumber = List(3) { Random.nextInt(1, 10) }.joinToString(",")
                                val requestBody = AssignLotteryRequest(
                                    mode = selectedChainInternal.lowercase(),
                                    value = value,
                                    toAddress = tokenTransfer.toAddress.lowercase(),
                                    tokenAddress = tokenAddress,
                                    lotteryNumber = lotteryNumber,
                                    lotteryPeriod = "1",
                                )
                                val lotteryResponse = try {
                                    RetrofitClient.apiService.assignLottery(requestBody)
                                } catch (e: Exception) {
                                    println(e)
                                    null
                                }
                                lotteryResponse?.let { _ ->
                                    // 格式化 orderItems 数据
                                    val itemNames = orderItems.map { it.name }
                                    val itemQuantities = orderItems.map { it.quantity }
                                    val itemPrices = orderItems.map { it.price }
                                    val purchaserAddress = tokenTransfer.fromAddress // 您可以更改为实际的购买者地址
                                    val lotteryNumbers = lotteryNumber.split(",").map { it.trim().toInt() } // 将 lotteryNumber 分割成整数列表

                                    // 创建请求体
                                    val notaryRequest = NotaryRequest(
                                        itemName = itemNames,
                                        itemQuantity = itemQuantities,
                                        itemPrice = itemPrices,
                                        tokenAddress = tokenAddress,
                                        purchaser = purchaserAddress,
                                        lotteryNumber = lotteryNumbers
                                    )
                                    val gson = Gson()
                                    val jsonString = gson.toJson(notaryRequest)
                                    println(jsonString)

                                    // 发送请求
                                    val urlResponse = try {
                                        RetrofitClient.apiService.createNotary(notaryRequest)
                                    } catch (e: Exception) {
                                        println("Error while creating notary: ${e.message}")
                                        null
                                    }

                                    // 检查返回的 URL 并传递到 printReceipt
                                    urlResponse?.data?.url?.let { url ->
                                        PrinterUtils.printReceipt(
                                            printer,
                                            totalPrice = currentTotalPrice,
                                            tokenAddress = selectedChainInternal.lowercase(),
                                            recipientAddress = tokenTransfer.toAddress,
                                            txHash = tokenTransfer.transactionHash,
                                            lotteryNumber = lotteryNumber,
                                            paymentToken = selectedTokenInternal,
                                            selectedChain = selectedChainInternal.lowercase(),
                                            orderItems = orderItems,
                                            fromAddress = tokenTransfer.fromAddress,
                                            notaryUrl = url  // 传递返回的 URL
                                        )
                                    }

//                                    PrinterUtils.printReceipt(
//                                        printer,
//                                        totalPrice = currentTotalPrice,
//                                        tokenAddress = selectedChainInternal.lowercase(),
//                                        recipientAddress = tokenTransfer.toAddress,
//                                        txHash = tokenTransfer.transactionHash,
//                                        lotteryNumber = lotteryNumber,
//                                        paymentToken = selectedTokenInternal,
//                                        selectedChain = selectedChainInternal.lowercase(),
//                                        orderItems = orderItems,
//                                        fromAddress = tokenTransfer.fromAddress
//                                    )
                                }
                            }
                            continueRequesting = false  // 停止循环
                        }
                    } else {
                        println("请求失败，消息: ${apiResponse.message}")
                    }
                }
            }
        }
    }


    // 添加这个函数
    // 更新链选择器的回调逻辑
    fun updateSelectedChain(chain: String) {
        selectedChainInternal = chain
        onChainSelected(chain) // 确保调用外部的链选择回调
    }

    // 添加这个函数
    fun updateSelectedToken(token: String) {
        selectedTokenInternal = token
        onTokenSelected(token)
    }

    // 打印功能实现
    fun handlePrint() {
        coroutineScope.launch {
            try {
                selectPrinter?.let { printer ->
                    PrinterUtils.printReceipt(
                        printer = printer,
                        totalPrice = currentTotalPrice,
                        tokenAddress = selectedChainInternal.lowercase(),
                        recipientAddress = "0xbebaf2a9ad714feb9dd151d81dd6d61ae0535646",
                        txHash = "0x1ff20cd9792f9a61f2b70ec6a8d6367a6856544d7c7cfbe0eb36a4cee17cea94",
                        lotteryNumber = "5,3,8",
                        paymentToken = selectedTokenInternal,
                        selectedChain = selectedChainInternal.lowercase(),
                        orderItems = orderItems,
                        fromAddress = "0xB4F205238b7556790dACef577D371Cb8f6C87215",
                        notaryUrl="https://testnet-scan.sign.global/attestation/onchain_evm_84532_0xd4d"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Composable
    fun MenuItemCard(
        name: String,
        count: Int,
        price: Double,
        imageResource: Int,
        onIncrement: () -> Unit,
        onDecrement: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .height(80.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Food Image
                Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = "$name image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Food Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${price}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Counter Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_minus),
                            contentDescription = "Decrease",
                            tint = when {
                                count == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onPrimary  // 改为和加号一样的文字颜色
                            },
                            modifier = Modifier.size(10.dp)  // 统一图标大小
//                            modifier = Modifier.scale(0.8f)
                        )
                    }

                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 24.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),  // 使用新的加号图标
                            contentDescription = "Increase",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(10.dp)  // 统一图标大小
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Chain Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(0.dp)) {
//                Text(
//                    "Select Chain",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold
//                )
//                Spacer(modifier = Modifier.height(8.dp))
                SelectChain(
                    selectedChain = selectedChainInternal,
                    onChainSelected = { updateSelectedChain(it) },  // 修改这里
                    chainIcons = chainIcons,
                    tokenAddresses = tokenAddresses
                )
            }
        }

        // Menu Items
        Text(
            "Menu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        MenuItemCard(
            name = "Burger",
            count = burgerCount,
            price = prices["Burger"]!!,
            imageResource = productImages["Burger"]!!,
            onIncrement = { burgerCount++ },
            onDecrement = { if (burgerCount > 0) burgerCount-- }
        )
        MenuItemCard(
            name = "Fries",
            count = friesCount,
            price = prices["Fries"]!!,
            imageResource = productImages["Fries"]!!,
            onIncrement = { friesCount++ },
            onDecrement = { if (friesCount > 0) friesCount-- }
        )
        MenuItemCard(
            name = "Cola",
            count = colaCount,
            price = prices["Cola"]!!,
            imageResource = productImages["Cola"]!!,
            onIncrement = { colaCount++ },
            onDecrement = { if (colaCount > 0) colaCount-- }
        )
        MenuItemCard(
            name = "Chicken",
            count = chickenCount,
            price = prices["Chicken"]!!,
            imageResource = productImages["Chicken"]!!,
            onIncrement = { chickenCount++ },
            onDecrement = { if (chickenCount > 0) chickenCount-- }
        )
        MenuItemCard(
            name = "Skewers",
            count = skewersCount,
            price = prices["Skewers"]!!,
            imageResource = productImages["Skewers"]!!,
            onIncrement = { skewersCount++ },
            onDecrement = { if (skewersCount > 0) skewersCount-- }
        )

        // Payment Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Payment Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Amount:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "$${String.format("%.2f", currentTotalPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("USDC" to R.drawable.usdc, "USDT" to R.drawable.usdt).forEach { (token, icon) ->
                        OutlinedButton(
                            onClick = { updateSelectedToken(token) },  // 修改这里
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTokenInternal == token)  // 使用 selectedTokenInternal
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Image(
                                painter = painterResource(id = icon),
                                contentDescription = "$token Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(token)
                        }
                    }
                }

                Button(
                    onClick = {
                        val tokenDecimals = 18
                        val amountInWei = BigDecimal(currentTotalPrice)
                            .multiply(BigDecimal.TEN.pow(tokenDecimals))
                            .setScale(0, RoundingMode.DOWN).toPlainString()

                        val (usdcAddress, customTokenAddress) = tokenAddresses[selectedChainInternal] ?: Pair("", "")
                        val recipientAddress = recipientAddresses[selectedChainInternal] ?: ""
                        val tokenAddress = if (selectedTokenInternal == "USDC") usdcAddress else customTokenAddress
                        val uri = "ethereum:$tokenAddress/transfer?address=$recipientAddress&uint256=$amountInWei"

                        onQrCodeGenerated(generateQRCode(uri))
                        onShowDialogChange(true)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = currentTotalPrice > 0
                ) {
                    Text(
                        "Place Order",
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    // QR Code Dialog
    if (showDialog && qrCodeBitmap != null) {
        AlertDialog(
            onDismissRequest = { onShowDialogChange(false) },
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Scan to Pay",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onShowDialogChange(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Image(
                            bitmap = qrCodeBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(280.dp)
                                .padding(16.dp)
                        )
                    }
                    Text(
                        "Amount: $${String.format("%.2f", currentTotalPrice)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        handlePrint()
//                        onShowDialogChange(false)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Print Receipt",
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        )
    }
}

@Composable
fun OrderSummarySection(
    orderItems: List<OrderItem>,
    currentTotalPrice: Double,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = orderItems.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Order Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                orderItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${item.name} × ${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "$${String.format("%.2f", item.price * item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$${String.format("%.2f", currentTotalPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}