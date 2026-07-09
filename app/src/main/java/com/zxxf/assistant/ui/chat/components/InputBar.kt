package com.zxxf.assistant.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zxxf.assistant.ui.theme.Subtext0

@Composable
fun InputBar(
    isThinking: Boolean,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    "输入入职、制度、流程或岗位培训问题...",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Subtext0
                )
            },
            modifier = Modifier.weight(1f),
            enabled = !isThinking,
            minLines = 1,
            maxLines = 5,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                val msg = text.trim()
                if (msg.isNotEmpty()) {
                    onSend(msg)
                    text = ""
                }
            },
            enabled = text.isNotBlank() && !isThinking,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "发送",
                tint = if (text.isNotBlank() && !isThinking)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}
