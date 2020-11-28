package com.github.yandroidua.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.mappers.mapToUiResult
import com.github.yandroidua.ui.utils.PathCalculationResult
import com.github.yandroidua.ui.utils.PathResultElements
import com.github.yandroidua.ui.utils.StartEndOffset
import kotlin.math.sqrt

@Composable
fun ResultScreen(
        modifier: Modifier = Modifier,
        elements: List<Element>,
        result: PathCalculationResult?,
        onSendClicked: (PathResultElements) -> Unit
) {
    if (result == null) {
        EmptyResultScreen(modifier)
        return
    }
    ResultsPathScreen(modifier, elements, result, onSendClicked)
}

@Composable
private fun ResultsPathScreen(
        modifier: Modifier,
        elements: List<Element>,
        result: PathCalculationResult,
        onSendClicked: (PathResultElements) -> Unit
) = Row(modifier) {
    val scrollState = rememberScrollState(0f)
    ScrollableColumn(
            modifier = Modifier.weight(1f),
            scrollState = scrollState
    ) {
        for ((index, path) in result.paths.withIndex()) {
            Path(
                    path = path.mapToUiResult(elements),
                    steps =  index % sqrt(result.paths.size.toDouble()).toInt() + 1,
                    onSendClicked = onSendClicked
            )
        }
    }
    VerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
}

@Composable
fun Path(
        modifier: Modifier = Modifier,
        path: PathResultElements,
        steps: Int,
        isSendEnabled: Boolean = true,
        onSendClicked: (PathResultElements) -> Unit = {}
) = Column(modifier =
modifier
        .padding(all = 4.dp)
        .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(size = 5.dp))
        .padding(all = 4.dp)
) {
    Row(modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
        Text("From workstation ", modifier = Modifier.align(alignment = Alignment.CenterVertically))
        Text(
                text = path.from.id.toString(),
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
        Text(text = " to workstation ", modifier = Modifier.align(alignment = Alignment.CenterVertically))
        Text(
                text = path.to.id.toString(),
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
    }
    Row {
        Text(
                text = "Weight:",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.height(1.dp).width(5.dp))
        Text(
                text = if (path.weight == Int.MAX_VALUE) "Inf" else path.weight.toString(),
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
    }
    Spacer(modifier = Modifier.height(5.dp))
    Row {
        Text(
                text = "Steps:",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.height(1.dp).width(5.dp))
        Text(
                text = steps.toString(),
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
    }
    Spacer(modifier = Modifier.height(5.dp))
    Row {
        Text(
                text = "Path:",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.height(1.dp).width(5.dp))
        val scrollState = rememberScrollState(0f)
        ScrollableRow(modifier = Modifier
                .weight(1f)
                .draggable(
                        orientation = Orientation.Horizontal,
                        enabled = true,
                        reverseDirection = true,
                        canDrag = { true },
                        startDragImmediately = true,
                        onDrag = { scrollState.scrollBy(it) }
                ),
                scrollState = scrollState
        ) {
            Box {
                Image(imageFromResource("workstation.png"),
                        modifier = Modifier
                                .width(32.dp)
                                .height(32.dp)
                )
                Text(
                        text = path.from.id.toString(),
                        color = Color.Red,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(alignment = Alignment.Center).padding(bottom = 5.dp)
                )
            }
            for (node in path.path) {
                Canvas(modifier = Modifier.wrapContentHeight().width(width = 50.dp)) {
                    node.first.copy(startEndOffset = StartEndOffset(
                            startPoint = Offset(0f, 32/2f),
                            endPoint = Offset(50f, 32/2f)
                    )).onDraw(this)
                }
                Box {
                    Image(imageFromResource("workstation.png"),
                            modifier = Modifier
                                    .width(32.dp)
                                    .height(32.dp)
                    )
                    Text(
                            text = node.second.id.toString(),
                            color = Color.Red,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(alignment = Alignment.Center).padding(bottom = 5.dp)
                    )
                }
            }
        }
    }
    if (isSendEnabled) {
        Spacer(modifier = Modifier.height(5.dp))
        Button(onClick = { onSendClicked(path) }) { Text(text = "Send") }
    }
}


@Composable
private fun EmptyResultScreen(modifier: Modifier) = Column(modifier.then(
        Modifier.fillMaxSize()
)) {
    Spacer(modifier = Modifier.weight(weight = 1/4f))
    Column(modifier = Modifier.weight(weight = 1/2f)
            .align(alignment = Alignment.CenterHorizontally)
            .background(Color.White)
    ) {
        Image(
                bitmap = imageFromResource("no_results.png"),
                modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(height = 10.dp).width(width = 1.dp))
        Text("No results",  modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
    }
    Spacer(modifier = Modifier.weight(weight = 1/4f))
}
