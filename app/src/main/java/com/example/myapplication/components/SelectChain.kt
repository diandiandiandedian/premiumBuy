package com.example.myapplication.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun SelectChain(
    selectedChain: String,
    onChainSelected: (String) -> Unit,
    chainIcons: Map<String, Int>,
    tokenAddresses: Map<String, Pair<String, String>>
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
//        Text("Select Chain:")
//        Spacer(modifier = Modifier.width(8.dp))
        Box {
            Button(onClick = { expanded = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = chainIcons[selectedChain] ?: R.drawable.base),
                        contentDescription = "$selectedChain Icon",
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(text = selectedChain)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                tokenAddresses.keys.forEach { chain ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = chainIcons[chain] ?: R.drawable.base),
                                    contentDescription = "$chain Icon",
                                    modifier = Modifier.size(24.dp).padding(end = 8.dp)
                                )
                                Text(chain)
                            }
                        },
                        onClick = {
                            onChainSelected(chain)  // 调用更新方法，传递新链
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}