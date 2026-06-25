package com.hanenashi.chirpie2.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.hanenashi.chirpie2.data.model.Bird
import com.hanenashi.chirpie2.data.model.BirdTextMetadata
import com.hanenashi.chirpie2.data.model.CustomBirdImport
import com.hanenashi.chirpie2.data.model.textMetadata
import com.hanenashi.chirpie2.data.preferences.BirdList
import com.hanenashi.chirpie2.data.preferences.DisplayMode
import com.hanenashi.chirpie2.data.preferences.SortOrder
import com.hanenashi.chirpie2.viewmodel.CustomBirdImportStatus
import kotlinx.coroutines.launch

@Composable
fun BirdListScreen(
    birds: List<Bird>,
    displayMode: DisplayMode,
    sortOrder: SortOrder,
    activeList: BirdList,
    gridColumns: Int,
    membershipsByBird: Map<Long, Set<BirdList>>,
    importStatus: CustomBirdImportStatus,
    isLoading: Boolean,
    playingAudioAsset: String?,
    onToggleAudio: (String) -> Unit,
    onScreenPress: () -> Unit,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onActiveListChange: (BirdList) -> Unit,
    onGridColumnsChange: (Int) -> Unit,
    onListMembershipChange: (Long, BirdList, Boolean) -> Unit,
    onUpdateTextMetadata: (BirdTextMetadata) -> Unit,
    onResetTextMetadata: (Long) -> Unit,
    onImportCustomBird: (CustomBirdImport) -> Unit,
    onDeleteCustomBird: (Bird) -> Unit,
    onDismissImportMessage: () -> Unit,
    onResetOrder: () -> Unit,
    onSaveOrder: (List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBirdId by remember { mutableStateOf<Long?>(null) }
    var editingBirdId by remember { mutableStateOf<Long?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showCustomBirdImport by remember { mutableStateOf(false) }
    var birdPendingDeletion by remember { mutableStateOf<Bird?>(null) }
    var arrangedBirds by remember { mutableStateOf<List<Bird>?>(null) }

    if (isLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Text(text = "Loading birds...", modifier = Modifier.padding(top = 12.dp))
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(playingAudioAsset) {
                if (playingAudioAsset != null) {
                    awaitPointerEventScope {
                        awaitPointerEvent(pass = PointerEventPass.Initial)
                        onScreenPress()
                    }
                }
            }
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        DisplayModeSelector(
            displayMode = displayMode,
            gridColumns = gridColumns,
            onDisplayModeChange = onDisplayModeChange,
            onAddCustomBird = { showCustomBirdImport = true },
            onOpenSettings = { showSettings = true }
        )

        when (displayMode) {
            DisplayMode.Grid -> BirdGrid(
                birds = arrangedBirds ?: birds,
                gridColumns = gridColumns,
                canArrange = activeList == BirdList.All && sortOrder == SortOrder.Custom,
                onBirdClick = { selectedBirdId = it.id },
                onGridColumnsChange = onGridColumnsChange,
                onStartArrange = {
                    if (arrangedBirds == null) arrangedBirds = birds
                },
                onMoveBird = { fromIndex, toIndex ->
                    arrangedBirds = arrangedBirds?.move(fromIndex, toIndex)
                },
                onFinishArrange = {
                    arrangedBirds?.let { onSaveOrder(it.map(Bird::id)) }
                    arrangedBirds = null
                },
                onCancelArrange = {
                    arrangedBirds = null
                }
            )

            DisplayMode.List -> BirdCompactList(
                birds = arrangedBirds ?: birds,
                canArrange = activeList == BirdList.All && sortOrder == SortOrder.Custom,
                onBirdClick = { selectedBirdId = it.id },
                playingAudioAsset = playingAudioAsset,
                onToggleAudio = onToggleAudio,
                onStartArrange = {
                    if (arrangedBirds == null) arrangedBirds = birds
                },
                onMoveBird = { fromIndex, toIndex ->
                    arrangedBirds = arrangedBirds?.move(fromIndex, toIndex)
                },
                onFinishArrange = {
                    arrangedBirds?.let { onSaveOrder(it.map(Bird::id)) }
                    arrangedBirds = null
                },
                onCancelArrange = {
                    arrangedBirds = null
                }
            )
        }
    }

    val selectedBird = birds.firstOrNull { it.id == selectedBirdId }
    selectedBird?.let { bird ->
        BirdDetailsDialog(
            bird = bird,
            memberships = membershipsByBird[bird.id].orEmpty(),
            onDismiss = { selectedBirdId = null },
            playingAudioAsset = playingAudioAsset,
            onToggleAudio = onToggleAudio,
            onScreenPress = onScreenPress,
            onEdit = { editingBirdId = bird.id },
            onResetText = { onResetTextMetadata(bird.id) },
            canResetText = bird.imageUrl.startsWith("file:///android_asset/"),
            canDelete = !bird.imageUrl.startsWith("file:///android_asset/"),
            onDelete = { birdPendingDeletion = bird },
            onListMembershipChange = { list, isMember ->
                onListMembershipChange(bird.id, list, isMember)
            }
        )
    }

    val editingBird = birds.firstOrNull { it.id == editingBirdId }
    editingBird?.let { bird ->
        BirdTextEditorDialog(
            bird = bird,
            onSave = {
                onUpdateTextMetadata(it)
                editingBirdId = null
            },
            onDismiss = { editingBirdId = null }
        )
    }

    if (showSettings) {
        BirdSettingsDialog(
            displayMode = displayMode,
            sortOrder = sortOrder,
            activeList = activeList,
            onDisplayModeChange = onDisplayModeChange,
            onSortOrderChange = onSortOrderChange,
            onActiveListChange = onActiveListChange,
            onResetOrder = onResetOrder,
            onDismiss = { showSettings = false }
        )
    }

    if (showCustomBirdImport) {
        CustomBirdImportDialog(
            onImport = {
                onImportCustomBird(it)
                showCustomBirdImport = false
            },
            onDismiss = { showCustomBirdImport = false }
        )
    }

    birdPendingDeletion?.let { bird ->
        DeleteBirdConfirmationDialog(
            bird = bird,
            onConfirm = {
                onScreenPress()
                onDeleteCustomBird(bird)
                if (selectedBirdId == bird.id) selectedBirdId = null
                birdPendingDeletion = null
            },
            onDismiss = { birdPendingDeletion = null }
        )
    }

    importStatus.message?.let { message ->
        ImportResultDialog(
            message = message,
            isError = importStatus.isError,
            onDismiss = onDismissImportMessage
        )
    }

    if (importStatus.isImporting) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Text("Importing bird files…")
                }
            }
        }
    }
}

@Composable
private fun DisplayModeSelector(
    displayMode: DisplayMode,
    gridColumns: Int,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onAddCustomBird: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.End
    ) {
        if (displayMode == DisplayMode.Grid) {
            Text(
                text = "$gridColumns columns",
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        DisplayMode.entries.forEach { mode ->
            TextButton(
                onClick = { onDisplayModeChange(mode) },
                enabled = displayMode != mode
            ) {
                Text(mode.name)
            }
        }
        TextButton(onClick = onAddCustomBird) {
            Text("Add bird")
        }
        TextButton(onClick = onOpenSettings) {
            Text("Settings")
        }
    }
}

@Composable
private fun BirdSettingsDialog(
    displayMode: DisplayMode,
    sortOrder: SortOrder,
    activeList: BirdList,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onActiveListChange: (BirdList) -> Unit,
    onResetOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .widthIn(max = 520.dp)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Settings", style = MaterialTheme.typography.headlineSmall)

                SettingsSection(title = "Display mode") {
                    DisplayMode.entries.forEach { mode ->
                        SettingsRadioRow(
                            label = mode.label,
                            selected = displayMode == mode,
                            onClick = { onDisplayModeChange(mode) }
                        )
                    }
                }

                HorizontalDivider()

                SettingsSection(title = "Active list") {
                    BirdList.entries.forEach { list ->
                        SettingsRadioRow(
                            label = list.label,
                            selected = activeList == list,
                            onClick = { onActiveListChange(list) }
                        )
                    }
                }

                HorizontalDivider()

                SettingsSection(title = "Sort order") {
                    SortOrder.entries.forEach { order ->
                        SettingsRadioRow(
                            label = order.label,
                            selected = sortOrder == order,
                            onClick = { onSortOrderChange(order) }
                        )
                    }
                    TextButton(
                        onClick = onResetOrder,
                        enabled = sortOrder != SortOrder.Custom
                    ) {
                        Text("Reset order")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun SettingsRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun BirdGrid(
    birds: List<Bird>,
    gridColumns: Int,
    canArrange: Boolean,
    onBirdClick: (Bird) -> Unit,
    onGridColumnsChange: (Int) -> Unit,
    onStartArrange: () -> Unit,
    onMoveBird: (Int, Int) -> Unit,
    onFinishArrange: () -> Unit,
    onCancelArrange: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var draggedBirdId by remember { mutableStateOf<Long?>(null) }
    var draggedOffset by remember { mutableStateOf(Offset.Zero) }
    var pointerPosition by remember { mutableStateOf(Offset.Zero) }
    var zoomAccumulator by remember { mutableFloatStateOf(1f) }
    val currentBirds by rememberUpdatedState(birds)
    val currentCanArrange by rememberUpdatedState(canArrange)
    val currentGridColumns by rememberUpdatedState(gridColumns)
    val currentOnGridColumnsChange by rememberUpdatedState(onGridColumnsChange)
    val currentOnStartArrange by rememberUpdatedState(onStartArrange)
    val currentOnMoveBird by rememberUpdatedState(onMoveBird)
    val currentOnFinishArrange by rememberUpdatedState(onFinishArrange)
    val currentOnCancelArrange by rememberUpdatedState(onCancelArrange)

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(gridState) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.count { it.pressed } >= 2) {
                            zoomAccumulator *= event.calculateZoom()
                            val nextColumns = when {
                                zoomAccumulator > 1.12f -> currentGridColumns - 1
                                zoomAccumulator < 0.89f -> currentGridColumns + 1
                                else -> currentGridColumns
                            }.coerceIn(2, 6)

                            if (nextColumns != currentGridColumns) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentOnGridColumnsChange(nextColumns)
                                zoomAccumulator = 1f
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(gridState) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        if (!currentCanArrange) {
                            draggedBirdId = null
                            return@detectDragGesturesAfterLongPress
                        }
                        val item = gridState.layoutInfo.visibleItemsInfo
                            .firstOrNull { info ->
                                offset.x >= info.offset.x &&
                                    offset.x <= info.offset.x + info.size.width &&
                                    offset.y >= info.offset.y &&
                                    offset.y <= info.offset.y + info.size.height
                            }
                        draggedBirdId = item?.key as? Long
                        draggedOffset = Offset.Zero
                        pointerPosition = offset
                        if (draggedBirdId != null) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnStartArrange()
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        draggedOffset += dragAmount
                        pointerPosition += dragAmount

                        val draggedId = draggedBirdId ?: return@detectDragGesturesAfterLongPress
                        val visibleItems = gridState.layoutInfo.visibleItemsInfo
                        val draggedItem = visibleItems.firstOrNull { it.key == draggedId }
                        val target = visibleItems
                            .firstOrNull { info ->
                                pointerPosition.x >= info.offset.x &&
                                    pointerPosition.x <= info.offset.x + info.size.width &&
                                    pointerPosition.y >= info.offset.y &&
                                    pointerPosition.y <= info.offset.y + info.size.height
                            }
                        val targetId = target?.key as? Long
                        if (targetId != null && targetId != draggedId) {
                            val fromIndex = currentBirds.indexOfFirst { it.id == draggedId }
                            val toIndex = currentBirds.indexOfFirst { it.id == targetId }
                            if (fromIndex >= 0 && toIndex >= 0 && draggedItem != null) {
                                draggedOffset += Offset(
                                    x = (draggedItem.offset.x - target.offset.x).toFloat(),
                                    y = (draggedItem.offset.y - target.offset.y).toFloat()
                                )
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentOnMoveBird(fromIndex, toIndex)
                            }
                        }

                        val edgeSize = 72.dp.toPx()
                        val scrollAmount = when {
                            pointerPosition.y < edgeSize -> -20.dp.toPx()
                            pointerPosition.y > gridState.layoutInfo.viewportSize.height - edgeSize ->
                                20.dp.toPx()
                            else -> 0f
                        }
                        if (scrollAmount != 0f) {
                            coroutineScope.launch { gridState.scrollBy(scrollAmount) }
                        }
                    },
                    onDragEnd = {
                        if (draggedBirdId != null) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnFinishArrange()
                        }
                        draggedBirdId = null
                        draggedOffset = Offset.Zero
                    },
                    onDragCancel = {
                        draggedBirdId = null
                        draggedOffset = Offset.Zero
                        currentOnCancelArrange()
                    }
                )
            },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 10.dp,
            end = 10.dp,
            bottom = 10.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        gridItems(items = birds, key = { it.id }) { bird ->
            BirdTile(
                bird = bird,
                modifier = Modifier.animateItem(),
                isDragged = bird.id == draggedBirdId,
                dragOffset = if (bird.id == draggedBirdId) draggedOffset else Offset.Zero,
                onClick = { onBirdClick(bird) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BirdCompactList(
    birds: List<Bird>,
    canArrange: Boolean,
    onBirdClick: (Bird) -> Unit,
    playingAudioAsset: String?,
    onToggleAudio: (String) -> Unit,
    onStartArrange: () -> Unit,
    onMoveBird: (Int, Int) -> Unit,
    onFinishArrange: () -> Unit,
    onCancelArrange: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var draggedBirdId by remember { mutableStateOf<Long?>(null) }
    var draggedOffset by remember { mutableFloatStateOf(0f) }
    var pointerY by remember { mutableFloatStateOf(0f) }
    val currentBirds by rememberUpdatedState(birds)
    val currentCanArrange by rememberUpdatedState(canArrange)
    val currentOnStartArrange by rememberUpdatedState(onStartArrange)
    val currentOnMoveBird by rememberUpdatedState(onMoveBird)
    val currentOnFinishArrange by rememberUpdatedState(onFinishArrange)
    val currentOnCancelArrange by rememberUpdatedState(onCancelArrange)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(listState) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        if (!currentCanArrange) {
                            draggedBirdId = null
                            return@detectDragGesturesAfterLongPress
                        }
                        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                            offset.y >= info.offset && offset.y <= info.offset + info.size
                        }
                        draggedBirdId = item?.key as? Long
                        draggedOffset = 0f
                        pointerY = offset.y
                        if (draggedBirdId != null) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnStartArrange()
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        draggedOffset += dragAmount.y
                        pointerY += dragAmount.y

                        val draggedId = draggedBirdId ?: return@detectDragGesturesAfterLongPress
                        val visibleItems = listState.layoutInfo.visibleItemsInfo
                        val draggedItem = visibleItems.firstOrNull { it.key == draggedId }
                        val target = visibleItems.firstOrNull { info ->
                            pointerY >= info.offset && pointerY <= info.offset + info.size
                        }
                        val targetId = target?.key as? Long
                        if (targetId != null && targetId != draggedId && draggedItem != null) {
                            val fromIndex = currentBirds.indexOfFirst { it.id == draggedId }
                            val toIndex = currentBirds.indexOfFirst { it.id == targetId }
                            if (fromIndex >= 0 && toIndex >= 0) {
                                draggedOffset += (draggedItem.offset - target.offset).toFloat()
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentOnMoveBird(fromIndex, toIndex)
                            }
                        }

                        val edgeSize = 72.dp.toPx()
                        val scrollAmount = when {
                            pointerY < edgeSize -> -20.dp.toPx()
                            pointerY > listState.layoutInfo.viewportSize.height - edgeSize ->
                                20.dp.toPx()
                            else -> 0f
                        }
                        if (scrollAmount != 0f) {
                            coroutineScope.launch { listState.scrollBy(scrollAmount) }
                        }
                    },
                    onDragEnd = {
                        if (draggedBirdId != null) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnFinishArrange()
                        }
                        draggedBirdId = null
                        draggedOffset = 0f
                    },
                    onDragCancel = {
                        draggedBirdId = null
                        draggedOffset = 0f
                        currentOnCancelArrange()
                    }
                )
            },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 10.dp,
            end = 10.dp,
            bottom = 10.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listItems(items = birds, key = { it.id }) { bird ->
            BirdListRow(
                bird = bird,
                modifier = Modifier.animateItem(),
                isDragged = bird.id == draggedBirdId,
                dragOffsetY = if (bird.id == draggedBirdId) draggedOffset else 0f,
                onClick = { onBirdClick(bird) },
                playingAudioAsset = playingAudioAsset,
                onToggleAudio = onToggleAudio
            )
        }
    }
}

@Composable
private fun BirdTile(
    bird: Bird,
    modifier: Modifier = Modifier,
    isDragged: Boolean,
    dragOffset: Offset,
    onClick: () -> Unit
) {
    val lift by animateFloatAsState(
        targetValue = if (isDragged) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bird-card-lift"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .zIndex(lift)
            .graphicsLayer {
                translationX = dragOffset.x
                translationY = dragOffset.y
                scaleX = 1f + (0.05f * lift)
                scaleY = 1f + (0.05f * lift)
                alpha = 1f - (0.12f * lift)
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !isDragged, onClick = onClick),
        color = Color.White,
        shadowElevation = (12f * lift).dp
    ) {
        AsyncImage(
            model = bird.imageUrl,
            contentDescription = bird.englishName,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BirdListRow(
    bird: Bird,
    modifier: Modifier = Modifier,
    isDragged: Boolean,
    dragOffsetY: Float,
    onClick: () -> Unit,
    playingAudioAsset: String?,
    onToggleAudio: (String) -> Unit
) {
    val lift by animateFloatAsState(
        targetValue = if (isDragged) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bird-row-lift"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp)
            .zIndex(lift)
            .graphicsLayer {
                translationY = dragOffsetY
                scaleX = 1f + (0.025f * lift)
                scaleY = 1f + (0.025f * lift)
                alpha = 1f - (0.1f * lift)
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !isDragged, onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = (12f * lift).dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .padding(6.dp)
            ) {
                AsyncImage(
                    model = bird.imageUrl,
                    contentDescription = bird.englishName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = bird.kanjiJapaneseName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = bird.englishName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = bird.scientificName,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.size(2.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    bird.audioAssetPaths().forEachIndexed { index, assetPath ->
                        AudioPlaybackButton(
                            assetPath = assetPath,
                            callNumber = index + 1,
                            isPlaying = playingAudioAsset == assetPath,
                            onToggleAudio = onToggleAudio
                        )
                    }
                }
            }
        }
    }
}

private fun <T> List<T>.move(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex) return this
    return toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}

@Composable
private fun AudioPlaybackButton(
    assetPath: String,
    callNumber: Int,
    isPlaying: Boolean,
    onToggleAudio: (String) -> Unit
) {
    IconButton(onClick = { onToggleAudio(assetPath) }) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) {
                    "Stop bird call $callNumber"
                } else {
                    "Play bird call $callNumber"
                },
                tint = if (isPlaying) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (callNumber > 1) {
                Text(
                    text = callNumber.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun DeleteBirdConfirmationDialog(
    bird: Bird,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Delete custom bird?", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "This permanently deletes ${bird.englishName.ifBlank { "this bird" }} " +
                        "and its copied image and audio files."
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = onConfirm) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BirdDetailsDialog(
    bird: Bird,
    memberships: Set<BirdList>,
    onDismiss: () -> Unit,
    playingAudioAsset: String?,
    onToggleAudio: (String) -> Unit,
    onScreenPress: () -> Unit,
    onEdit: () -> Unit,
    onResetText: () -> Unit,
    canResetText: Boolean,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onListMembershipChange: (BirdList, Boolean) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(playingAudioAsset) {
                    if (playingAudioAsset != null) {
                        awaitPointerEventScope {
                            awaitPointerEvent(pass = PointerEventPass.Initial)
                            onScreenPress()
                        }
                    }
                }
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .widthIn(max = 520.dp)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    AsyncImage(
                        model = bird.imageUrl,
                        contentDescription = bird.englishName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = bird.kanjiJapaneseName, style = MaterialTheme.typography.headlineSmall)
                    Text(text = bird.romanizedJapaneseName, style = MaterialTheme.typography.titleMedium)
                    Text(text = bird.englishName, style = MaterialTheme.typography.bodyLarge)
                    Text(text = bird.scientificName, style = MaterialTheme.typography.bodyMedium)
                    Text(text = bird.czechName, style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onEdit) {
                        Text("Edit text")
                    }
                    TextButton(
                        onClick = onResetText,
                        enabled = canResetText
                    ) {
                        Text("Reset text")
                    }
                    if (canDelete) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete custom bird"
                            )
                        }
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bird.audioAssetPaths().forEachIndexed { index, assetPath ->
                        AudioPlaybackButton(
                            assetPath = assetPath,
                            callNumber = index + 1,
                            isPlaying = playingAudioAsset == assetPath,
                            onToggleAudio = onToggleAudio
                        )
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Saved lists", style = MaterialTheme.typography.titleMedium)
                    BirdList.entries.filter(BirdList::isEditable).forEach { list ->
                        val isMember = list in memberships
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onListMembershipChange(list, !isMember)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isMember,
                                onCheckedChange = { checked ->
                                    onListMembershipChange(list, checked)
                                }
                            )
                            Text(list.label)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportResultDialog(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isError) "Import failed" else "Import complete",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(message)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun BirdTextEditorDialog(
    bird: Bird,
    onSave: (BirdTextMetadata) -> Unit,
    onDismiss: () -> Unit
) {
    var metadata by remember(bird.id) { mutableStateOf(bird.textMetadata()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .widthIn(max = 520.dp)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit bird text", style = MaterialTheme.typography.headlineSmall)

                BirdTextField(
                    label = "Japanese",
                    value = metadata.kanjiJapaneseName,
                    onValueChange = { metadata = metadata.copy(kanjiJapaneseName = it) }
                )
                BirdTextField(
                    label = "Romanized Japanese",
                    value = metadata.romanizedJapaneseName,
                    onValueChange = { metadata = metadata.copy(romanizedJapaneseName = it) }
                )
                BirdTextField(
                    label = "English",
                    value = metadata.englishName,
                    onValueChange = { metadata = metadata.copy(englishName = it) }
                )
                BirdTextField(
                    label = "Scientific",
                    value = metadata.scientificName,
                    onValueChange = { metadata = metadata.copy(scientificName = it) }
                )
                BirdTextField(
                    label = "Czech",
                    value = metadata.czechName,
                    onValueChange = { metadata = metadata.copy(czechName = it) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { onSave(metadata) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun BirdTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true
    )
}

@Composable
private fun CustomBirdImportDialog(
    onImport: (CustomBirdImport) -> Unit,
    onDismiss: () -> Unit
) {
    var kanjiJapaneseName by remember { mutableStateOf("") }
    var romanizedJapaneseName by remember { mutableStateOf("") }
    var englishName by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var czechName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var firstAudioUri by remember { mutableStateOf<String?>(null) }
    var secondAudioUri by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        imageUri = uri?.toString()
    }
    val firstAudioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        firstAudioUri = uri?.toString()
    }
    val secondAudioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        secondAudioUri = uri?.toString()
    }

    val hasName = listOf(
        kanjiJapaneseName,
        romanizedJapaneseName,
        englishName,
        scientificName,
        czechName
    ).any { it.isNotBlank() }
    val canImport = imageUri != null && firstAudioUri != null && hasName

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .widthIn(max = 520.dp)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add custom bird", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Choose an image, one or two MP3 files, and enter the bird names.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ImportFileRow(
                    label = "Image",
                    selectedUri = imageUri,
                    buttonLabel = "Choose JPG/PNG",
                    onChoose = { imagePicker.launch(arrayOf("image/jpeg", "image/png")) }
                )
                ImportFileRow(
                    label = "Bird call 1",
                    selectedUri = firstAudioUri,
                    buttonLabel = "Choose MP3",
                    onChoose = { firstAudioPicker.launch(arrayOf("audio/mpeg", "audio/mp3")) }
                )
                ImportFileRow(
                    label = "Bird call 2 (optional)",
                    selectedUri = secondAudioUri,
                    buttonLabel = "Choose MP3",
                    onChoose = { secondAudioPicker.launch(arrayOf("audio/mpeg", "audio/mp3")) }
                )

                HorizontalDivider()

                BirdTextField(
                    label = "Japanese",
                    value = kanjiJapaneseName,
                    onValueChange = { kanjiJapaneseName = it }
                )
                BirdTextField(
                    label = "Romanized Japanese",
                    value = romanizedJapaneseName,
                    onValueChange = { romanizedJapaneseName = it }
                )
                BirdTextField(
                    label = "English",
                    value = englishName,
                    onValueChange = { englishName = it }
                )
                BirdTextField(
                    label = "Scientific",
                    value = scientificName,
                    onValueChange = { scientificName = it }
                )
                BirdTextField(
                    label = "Czech",
                    value = czechName,
                    onValueChange = { czechName = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(
                        enabled = canImport,
                        onClick = {
                            onImport(
                                CustomBirdImport(
                                    kanjiJapaneseName = kanjiJapaneseName,
                                    romanizedJapaneseName = romanizedJapaneseName,
                                    englishName = englishName,
                                    scientificName = scientificName,
                                    czechName = czechName,
                                    imageUri = requireNotNull(imageUri),
                                    audioUris = listOfNotNull(
                                        firstAudioUri,
                                        secondAudioUri
                                    )
                                )
                            )
                        }
                    ) {
                        Text("Import")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportFileRow(
    label: String,
    selectedUri: String?,
    buttonLabel: String,
    onChoose: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        TextButton(onClick = onChoose) {
            Text(buttonLabel)
        }
        Text(
            text = selectedUri?.substringAfterLast('/') ?: "No file selected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
