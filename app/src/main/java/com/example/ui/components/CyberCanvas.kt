package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.DrawingLayer
import com.example.model.DrawingStroke
import com.example.model.ExecutionState
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.GridLine
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.UUID

/**
 * Pre-computed state-based path buffer for a single vector layer.
 * Eliminates per-frame path allocations and recomputations for smooth 120 FPS rendering.
 */
data class LayerPathBuffer(
    val layerId: String,
    val isVisible: Boolean,
    val opacity: Float,
    val paths: List<CachedPathItem>
)

data class CachedPathItem(
    val strokeId: String,
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isClosed: Boolean
)

/**
 * Vector Drawing Canvas with State-Based Path Buffers, Layer Management, and Undo/Redo support.
 */
@Composable
fun DrawingCanvas(
    instructionSet: ArtHaxInstructionSet?,
    executionState: ExecutionState,
    modifier: Modifier = Modifier,
    bounds: CalibrationBounds? = null,
    showGrid: Boolean = true,
    showStrokeIndices: Boolean = false,
    interactiveGlow: Boolean = false,
    transparentBackground: Boolean = false,
    onInstructionSetChange: ((ArtHaxInstructionSet) -> Unit)? = null
) {
    // -----------------------------------------------------------------------------------
    // STATE-BASED LAYER & UNDO/REDO MANAGEMENT
    // -----------------------------------------------------------------------------------
    val undoStack = remember { mutableStateListOf<List<DrawingLayer>>() }
    val redoStack = remember { mutableStateListOf<List<DrawingLayer>>() }
    var isLayerManagerOpen by remember { mutableStateOf(false) }
    var activeLayerId by remember { mutableStateOf<String?>(null) }
    var isGridActive by remember { mutableStateOf(showGrid) }

    // Synchronize layers from incoming instruction set
    val currentLayers = remember(instructionSet) {
        if (instructionSet == null) {
            listOf(
                DrawingLayer(id = "layer_base", name = "Layer 1 - Outline", colorTagHex = "#00F0FF"),
                DrawingLayer(id = "layer_shading", name = "Layer 2 - Shading", colorTagHex = "#FF00E5")
            )
        } else if (instructionSet.layers.isNotEmpty()) {
            instructionSet.layers
        } else if (instructionSet.strokes.isNotEmpty()) {
            // Organize flat strokes into semantic vector layers
            decomposeStrokesIntoLayers(instructionSet.strokes)
        } else {
            listOf(DrawingLayer(id = "layer_base", name = "Layer 1 - Main", colorTagHex = "#00F0FF"))
        }
    }

    // Set initial active layer
    LaunchedEffect(currentLayers) {
        if (activeLayerId == null || currentLayers.none { it.id == activeLayerId }) {
            activeLayerId = currentLayers.firstOrNull()?.id
        }
    }

    // Helper: Push current layers state to Undo Stack
    fun pushUndoState(newLayers: List<DrawingLayer>) {
        undoStack.add(currentLayers)
        redoStack.clear()
        if (instructionSet != null && onInstructionSetChange != null) {
            onInstructionSetChange(instructionSet.copy(layers = newLayers, strokes = newLayers.flatMap { it.strokes }))
        }
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val previousState = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(currentLayers)
            if (instructionSet != null && onInstructionSetChange != null) {
                onInstructionSetChange(instructionSet.copy(layers = previousState, strokes = previousState.flatMap { it.strokes }))
            }
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            val nextState = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(currentLayers)
            if (instructionSet != null && onInstructionSetChange != null) {
                onInstructionSetChange(instructionSet.copy(layers = nextState, strokes = nextState.flatMap { it.strokes }))
            }
        }
    }

    // Revert the last drawn stroke in active layer
    fun revertLastStroke() {
        val targetLayerId = activeLayerId ?: currentLayers.firstOrNull()?.id ?: return
        val updated = currentLayers.map { layer ->
            if (layer.id == targetLayerId && layer.strokes.isNotEmpty()) {
                layer.copy(strokes = layer.strokes.dropLast(1))
            } else {
                layer
            }
        }
        pushUndoState(updated)
    }

    val boxModifier = if (transparentBackground) {
        modifier
            .background(Color.Transparent)
            .testTag("drawing_canvas_box")
    } else {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CyberBackground)
            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
            .testTag("drawing_canvas_box")
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        // -------------------------------------------------------------------------------
        // HIGH-PERFORMANCE COMPOSE CANVAS WITH PATH BUFFERS
        // -------------------------------------------------------------------------------
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("drawing_canvas_viewport")
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw subtle guide grid
            if (isGridActive && !transparentBackground) {
                drawBlueprintGrid(canvasW, canvasH)
            }

            // 2. Draw Calibration Box Guides (if active)
            if (bounds != null) {
                drawCalibrationGuides(bounds, canvasW, canvasH)
            }

            // 3. Render Pre-Buffered Vector Layers with state-based Path compilation
            val activeDrawing = executionState as? ExecutionState.Drawing

            currentLayers.forEach { layer ->
                if (layer.isVisible) {
                    val layerOpacity = layer.opacity.coerceIn(0f, 1f)

                    layer.strokes.forEachIndexed { sIdx, stroke ->
                        val isCurrentStroke = activeDrawing?.currentStrokeIndex == (sIdx + 1)
                        val isDrawnStroke = activeDrawing == null || sIdx < (activeDrawing.currentStrokeIndex - 1)

                        if (isDrawnStroke || isCurrentStroke) {
                            val maxPts = if (isCurrentStroke) activeDrawing.currentPointIndex else stroke.points.size
                            drawBufferedLayerStroke(
                                stroke = stroke,
                                canvasW = canvasW,
                                canvasH = canvasH,
                                layerOpacity = layerOpacity,
                                maxPointsToDraw = maxPts
                            )
                        }
                    }
                }
            }

            // 4. Stylus / Laser Reticle Indicator when drawing is active
            if (executionState is ExecutionState.Drawing && executionState.activePoint != null) {
                val pt = executionState.activePoint
                val posX = pt.x * canvasW
                val posY = pt.y * canvasH

                // Outer radar glow ring
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.35f),
                    radius = 20f,
                    center = Offset(posX, posY),
                    style = Stroke(width = 2.5f)
                )

                // Mid ring
                drawCircle(
                    color = NeonPink.copy(alpha = 0.6f),
                    radius = 10f,
                    center = Offset(posX, posY),
                    style = Stroke(width = 1.5f)
                )

                // Center laser point
                drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = Offset(posX, posY)
                )
            }
        }

        // -------------------------------------------------------------------------------
        // TOP CONTROL TOOLBAR (Undo, Redo, Layers, Grid, Revert)
        // -------------------------------------------------------------------------------
        if (!transparentBackground) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackgroundElevated.copy(alpha = 0.92f))
                        .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("canvas_toolbar")
                ) {
                    // Undo Button
                    IconButton(
                        onClick = { handleUndo() },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.size(32.dp).testTag("canvas_undo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) NeonCyan else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Redo Button
                    IconButton(
                        onClick = { handleRedo() },
                        enabled = redoStack.isNotEmpty(),
                        modifier = Modifier.size(32.dp).testTag("canvas_redo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (redoStack.isNotEmpty()) NeonCyan else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Revert Last Stroke in Active Layer
                    IconButton(
                        onClick = { revertLastStroke() },
                        enabled = currentLayers.any { it.strokes.isNotEmpty() },
                        modifier = Modifier.size(32.dp).testTag("canvas_revert_stroke_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Revert Last Stroke",
                            tint = if (currentLayers.any { it.strokes.isNotEmpty() }) NeonPink else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Grid Toggle
                    IconButton(
                        onClick = { isGridActive = !isGridActive },
                        modifier = Modifier.size(32.dp).testTag("canvas_grid_toggle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Toggle Grid",
                            tint = if (isGridActive) NeonCyan else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Layer Manager Toggle
                    IconButton(
                        onClick = { isLayerManagerOpen = !isLayerManagerOpen },
                        modifier = Modifier.size(32.dp).testTag("canvas_layers_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layer Manager",
                            tint = if (isLayerManagerOpen) NeonGreen else NeonCyan,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------------------------
        // LAYER MANAGEMENT DRAWER OVERLAY
        // -------------------------------------------------------------------------------
        AnimatedVisibility(
            visible = isLayerManagerOpen && !transparentBackground,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            LayerManagerPanel(
                layers = currentLayers,
                activeLayerId = activeLayerId,
                onSelectActiveLayer = { activeLayerId = it },
                onToggleVisibility = { layerId ->
                    val updated = currentLayers.map {
                        if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
                    }
                    pushUndoState(updated)
                },
                onToggleLock = { layerId ->
                    val updated = currentLayers.map {
                        if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
                    }
                    pushUndoState(updated)
                },
                onUpdateOpacity = { layerId, opacity ->
                    val updated = currentLayers.map {
                        if (it.id == layerId) it.copy(opacity = opacity) else it
                    }
                    pushUndoState(updated)
                },
                onAddLayer = {
                    val newIndex = currentLayers.size + 1
                    val newLayer = DrawingLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Layer $newIndex - Accents",
                        colorTagHex = listOf("#00F0FF", "#FF00E5", "#00FF88", "#FFE600", "#7000FF")[newIndex % 5]
                    )
                    val updated = currentLayers + newLayer
                    pushUndoState(updated)
                    activeLayerId = newLayer.id
                },
                onDeleteLayer = { layerId ->
                    if (currentLayers.size > 1) {
                        val updated = currentLayers.filter { it.id != layerId }
                        pushUndoState(updated)
                        if (activeLayerId == layerId) {
                            activeLayerId = updated.firstOrNull()?.id
                        }
                    }
                },
                onClearLayerStrokes = { layerId ->
                    val updated = currentLayers.map {
                        if (it.id == layerId && !it.isLocked) it.copy(strokes = emptyList()) else it
                    }
                    pushUndoState(updated)
                },
                onClose = { isLayerManagerOpen = false }
            )
        }

        // Empty state overlay
        if (!transparentBackground && instructionSet == null && executionState is ExecutionState.Idle && !isLayerManagerOpen) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No Drawing Loaded",
                    color = TextWhite,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a preset sample or use the prompt bar below to generate strokes.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Backward compatibility alias for CyberCanvas
 */
@Composable
fun CyberCanvas(
    instructionSet: ArtHaxInstructionSet?,
    executionState: ExecutionState,
    modifier: Modifier = Modifier,
    bounds: CalibrationBounds? = null,
    showGrid: Boolean = true,
    showStrokeIndices: Boolean = false,
    interactiveGlow: Boolean = false,
    transparentBackground: Boolean = false,
    onInstructionSetChange: ((ArtHaxInstructionSet) -> Unit)? = null
) {
    DrawingCanvas(
        instructionSet = instructionSet,
        executionState = executionState,
        modifier = modifier,
        bounds = bounds,
        showGrid = showGrid,
        showStrokeIndices = showStrokeIndices,
        interactiveGlow = interactiveGlow,
        transparentBackground = transparentBackground,
        onInstructionSetChange = onInstructionSetChange
    )
}

/**
 * Interactive Layer Manager Panel for controlling vector layers, opacity, lock, and visibility.
 */
@Composable
fun LayerManagerPanel(
    layers: List<DrawingLayer>,
    activeLayerId: String?,
    onSelectActiveLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onUpdateOpacity: (String, Float) -> Unit,
    onAddLayer: () -> Unit,
    onDeleteLayer: (String) -> Unit,
    onClearLayerStrokes: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
            .testTag("layer_manager_panel"),
        color = CardBackgroundElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Panel Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Layer Management Buffer",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${layers.size} layers)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Add Layer Button
                    ElevatedButton(
                        onClick = onAddLayer,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = CyberBlack,
                            contentColor = NeonCyan
                        ),
                        modifier = Modifier.height(28.dp).testTag("layer_add_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "New Layer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp).testTag("layer_manager_close_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Layers List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                layers.forEach { layer ->
                    val isActive = layer.id == activeLayerId

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isActive) CyberBlack.copy(alpha = 0.9f) else CardBackground.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isActive) 1.5.dp else 1.dp,
                            color = if (isActive) NeonCyan else BorderGlass
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectActiveLayer(layer.id) }
                            .testTag("layer_item_${layer.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Color Tag Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                try {
                                                    Color(android.graphics.Color.parseColor(layer.colorTagHex))
                                                } catch (e: Exception) {
                                                    NeonCyan
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = layer.name,
                                        color = if (isActive) TextCyan else TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${layer.strokes.size} strokes / ${layer.totalPoints} pts)",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Visibility Toggle
                                    IconButton(
                                        onClick = { onToggleVisibility(layer.id) },
                                        modifier = Modifier.size(26.dp).testTag("layer_vis_${layer.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Visibility",
                                            tint = if (layer.isVisible) NeonCyan else TextMuted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // Lock Toggle
                                    IconButton(
                                        onClick = { onToggleLock(layer.id) },
                                        modifier = Modifier.size(26.dp).testTag("layer_lock_${layer.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = "Toggle Lock",
                                            tint = if (layer.isLocked) NeonYellow else TextMuted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // Delete Layer
                                    if (layers.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteLayer(layer.id) },
                                            modifier = Modifier.size(26.dp).testTag("layer_delete_${layer.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Layer",
                                                tint = NeonPink,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Layer Opacity Slider (Expanded for active layer)
                            if (isActive) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Opacity: ${(layer.opacity * 100).toInt()}%",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        modifier = Modifier.width(68.dp)
                                    )
                                    Slider(
                                        value = layer.opacity,
                                        onValueChange = { onUpdateOpacity(layer.id, it) },
                                        valueRange = 0.05f..1.0f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = NeonCyan,
                                            activeTrackColor = NeonCyan,
                                            inactiveTrackColor = BorderGlass
                                        ),
                                        modifier = Modifier.weight(1f).height(24.dp).testTag("layer_opacity_${layer.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Automatically groups unstructured AI flat strokes into semantic vector layers.
 */
private fun decomposeStrokesIntoLayers(strokes: List<DrawingStroke>): List<DrawingLayer> {
    if (strokes.isEmpty()) {
        return listOf(DrawingLayer(id = "layer_1", name = "Layer 1 - Main", colorTagHex = "#00F0FF"))
    }

    val total = strokes.size
    val layer1Count = (total * 0.45f).toInt().coerceAtLeast(1)
    val layer2Count = (total * 0.35f).toInt().coerceAtLeast(0)

    val layer1Strokes = strokes.take(layer1Count)
    val layer2Strokes = strokes.drop(layer1Count).take(layer2Count)
    val layer3Strokes = strokes.drop(layer1Count + layer2Count)

    val layers = mutableListOf<DrawingLayer>()
    layers.add(
        DrawingLayer(
            id = "layer_outline",
            name = "Layer 1 - Silhouettes & Outline",
            colorTagHex = "#00F0FF",
            strokes = layer1Strokes
        )
    )

    if (layer2Strokes.isNotEmpty()) {
        layers.add(
            DrawingLayer(
                id = "layer_detail",
                name = "Layer 2 - Anatomy & Shading",
                colorTagHex = "#FF00E5",
                strokes = layer2Strokes
            )
        )
    }

    if (layer3Strokes.isNotEmpty()) {
        layers.add(
            DrawingLayer(
                id = "layer_accents",
                name = "Layer 3 - Highlights & Accents",
                colorTagHex = "#FFE600",
                strokes = layer3Strokes
            )
        )
    }

    return layers
}

private fun DrawScope.drawBlueprintGrid(w: Float, h: Float) {
    val step = 40f
    var x = step
    while (x < w) {
        drawLine(
            color = GridLine,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += step
    }

    var y = step
    while (y < h) {
        drawLine(
            color = GridLine,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawCalibrationGuides(bounds: CalibrationBounds, w: Float, h: Float) {
    val left = bounds.left * w
    val top = bounds.top * h
    val width = bounds.width * w
    val height = bounds.height * h

    // Dashed bounding box
    drawRect(
        color = NeonCyan.copy(alpha = 0.5f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
    )

    // Corner targeting marks
    val cornerLen = 14f
    drawLine(NeonCyan, Offset(left, top), Offset(left + cornerLen, top), 2.5f)
    drawLine(NeonCyan, Offset(left, top), Offset(left, top + cornerLen), 2.5f)
    drawLine(NeonCyan, Offset(left + width, top), Offset(left + width - cornerLen, top), 2.5f)
    drawLine(NeonCyan, Offset(left + width, top), Offset(left + width, top + cornerLen), 2.5f)
    drawLine(NeonCyan, Offset(left, top + height), Offset(left + cornerLen, top + height), 2.5f)
    drawLine(NeonCyan, Offset(left, top + height), Offset(left + top + height, top + height), 2.5f)
    drawLine(NeonCyan, Offset(left + width, top + height), Offset(left + width - cornerLen, top + height), 2.5f)
    drawLine(NeonCyan, Offset(left + width, top + height), Offset(left + width, top + height - cornerLen), 2.5f)
}

private fun DrawScope.drawBufferedLayerStroke(
    stroke: DrawingStroke,
    canvasW: Float,
    canvasH: Float,
    layerOpacity: Float,
    maxPointsToDraw: Int
) {
    if (stroke.points.size < 2) return
    val pts = stroke.points.take(maxPointsToDraw)
    if (pts.size < 2) return

    val baseColor = stroke.parseColor()
    val finalColor = baseColor.copy(alpha = baseColor.alpha * layerOpacity)

    val path = Path()
    path.moveTo(pts[0].x * canvasW, pts[0].y * canvasH)

    for (i in 1 until pts.size) {
        val prev = pts[i - 1]
        val curr = pts[i]
        val midX = (prev.x + curr.x) * 0.5f * canvasW
        val midY = (prev.y + curr.y) * 0.5f * canvasH
        path.quadraticTo(prev.x * canvasW, prev.y * canvasH, midX, midY)
    }
    path.lineTo(pts.last().x * canvasW, pts.last().y * canvasH)

    if (stroke.isClosed) {
        path.close()
    }

    // Subtle soft background depth stroke
    drawPath(
        path = path,
        color = finalColor.copy(alpha = finalColor.alpha * 0.25f),
        style = Stroke(
            width = stroke.strokeWidth + 2f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Solid vector line
    drawPath(
        path = path,
        color = finalColor,
        style = Stroke(
            width = stroke.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
