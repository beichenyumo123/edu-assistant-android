package com.zxxf.assistant.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val presetQuestions = listOf(
    "入职第一周需要完成哪些事项？",
    "试用期转正评估主要看什么？",
    "请假和异常打卡应该怎么处理？",
    "差旅报销需要注意哪些要求？",
    "哪些公司数据不能外发或上传？",
    "帮我整理新人必修培训清单"
)

/**
 * Welcome screen shown when there are no messages in the current conversation.
 * Displays a greeting and 6 preset questions in a 2-column AssistChip grid.
 */
@Composable
fun WelcomeCards(
    onPresetClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "你好，我是入职培训助手",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "基于已上传的企业培训资料，回答入职、制度、流程、安全等问题",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preset question chips in a 2-column grid
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) { col ->
                    val index = row * 2 + col
                    if (index < presetQuestions.size) {
                        AssistChip(
                            onClick = { onPresetClick(presetQuestions[index]) },
                            label = {
                                Text(
                                    text = presetQuestions[index],
                                    maxLines = 2
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
