package com.example.myapplication

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.PrinterSdk.PrinterListen
import androidx.compose.ui.Modifier
import com.example.myapplication.components.SelectAndOrderMenu

class MainActivity : ComponentActivity() {

    var selectPrinter by mutableStateOf<PrinterSdk.Printer?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SimpleOrderScreen(selectPrinter)
                }
            }
        }
        initPrinter()
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun initPrinter() {
        try {
            PrinterSdk.getInstance().getPrinter(this, object : PrinterListen {
                override fun onDefPrinter(printer: PrinterSdk.Printer) {
                    selectPrinter = printer
                    mainHandler.post {
                        Toast.makeText(this@MainActivity, "Printer connected", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPrinters(printers: List<PrinterSdk.Printer>) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
            mainHandler.post {
                Toast.makeText(this, "Failed to initialize printer", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SimpleOrderScreen(selectPrinter: PrinterSdk.Printer?) {
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val tokenAddresses = mapOf(
        "Base" to Pair("0xA7ab21686D40Aa35Cb51137A795D84A57352F593", "0xA7ab21686D40Aa35Cb51137A795D84A57352F593"),
        "Scroll" to Pair("0xscrollUSDCAddress", "0xscrollCustomTokenAddress"),
        "Flow" to Pair("0x7BAF75d206CA49B3454E1E54D9d563ff80f7492D", "0x7BAF75d206CA49B3454E1E54D9d563ff80f7492D"),
        "Mantle" to Pair("0xmantleUSDCAddress", "0xmantleCustomTokenAddress"),
        "Zircuit" to Pair("0xzircuitUSDCAddress", "0xzircuitCustomTokenAddress"),
        "Celo" to Pair("0x2EC5CfDE6F37029aa8cc018ED71CF4Ef67C704AE", "0x2EC5CfDE6F37029aa8cc018ED71CF4Ef67C704AE"),
        "Linea" to Pair("0xlineaUSDCAddress", "0xlineaCustomTokenAddress"),
        "Morph" to Pair("0x7BAF75d206CA49B3454E1E54D9d563ff80f7492D", "0x7BAF75d206CA49B3454E1E54D9d563ff80f7492D"),
        "Fhenix" to Pair("0xfhenixUSDCAddress", "0xfhenixCustomTokenAddress")
    )

    val recipientAddresses = mapOf(
        "Base" to "0xbebaf2a9ad714feb9dd151d81dd6d61ae0535646",
        "Scroll" to "0xscrollSpecificAddress",
        "Flow" to "0xBEbAF2a9ad714fEb9Dd151d81Dd6d61Ae0535646",
        "Mantle" to "0xmantleSpecificAddress",
        "Zircuit" to "0xzircuitSpecificAddress",
        "Celo" to "0xBEbAF2a9ad714fEb9Dd151d81Dd6d61Ae0535646",
        "Linea" to "0xlineaSpecificAddress",
        "Morph" to "0xBEbAF2a9ad714fEb9Dd151d81Dd6d61Ae0535646",
        "Fhenix" to "0xfhenixSpecificAddress"
    )
    val prices = mapOf(
        "Burger" to 1.0,
        "Fries" to 0.5,
        "Cola" to 0.5,
        "Chicken" to 2.0,
        "Skewers" to 10.0
    )

    val chainIcons = mapOf(
        "Base" to R.drawable.base,
        "Scroll" to R.drawable.scroll,
        "Bitkub" to R.drawable.bitkub,
        "Mantle" to R.drawable.mantle,
        "Zircuit" to R.drawable.zircuit,
        "Celo" to R.drawable.celo,
        "Linea" to R.drawable.linea,
        "Morph" to R.drawable.morph,
        "Fhenix" to R.drawable.fhenix,
        "Flow" to R.drawable.flow,
    )

    val productImages = mapOf(
        "Burger" to R.drawable.hamburger,
        "Fries" to R.drawable.fries,
        "Cola" to R.drawable.cola,
        "Chicken" to R.drawable.friedchicken,
        "Skewers" to R.drawable.kebabs
    )

    SelectAndOrderMenu(
        selectedChain = "Base",
        onChainSelected = { /* handle chain change */ },
        chainIcons = chainIcons,
        tokenAddresses = tokenAddresses,
        recipientAddresses = recipientAddresses,
        productImages = productImages,
        prices = prices,
        selectPrinter = selectPrinter,
        qrCodeBitmap = qrCodeBitmap,
        onQrCodeGenerated = { qrCodeBitmap = it },
        showDialog = showDialog,
        onShowDialogChange = { showDialog = it },
        selectedToken = "USDC",
        onTokenSelected = { /* handle token selection */ },
        totalPrice = 0.0
    )
}