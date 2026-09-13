package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HistoryEntity
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderLight
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletLight
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  historyList: List<HistoryEntity>,
  onBack: () -> Unit,
  onOpenItem: (HistoryEntity) -> Unit,
  onRegenerateItem: (HistoryEntity) -> Unit,
  onDeleteItem: (HistoryEntity) -> Unit,
  onSaveItem: (HistoryEntity) -> Unit,
  onShareItem: (HistoryEntity) -> Unit,
  onClearAll: () -> Unit,
  onToggleOrientation: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showClearConfirmDialog by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = DarkBackground,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Image History",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Text(
              text = "${historyList.size} ${if (historyList.size == 1) "creation" else "creations"} saved locally",
              style = MaterialTheme.typography.bodySmall,
              color = CyanLight
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("history_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Studio",
              tint = TextPrimary
            )
          }
        },
        actions = {
          IconButton(
            onClick = onToggleOrientation,
            modifier = Modifier.testTag("history_rotate_button")
          ) {
            Icon(
              imageVector = Icons.Default.ScreenRotation,
              contentDescription = "Toggle Screen Orientation",
              tint = TextSecondary
            )
          }
          if (historyList.isNotEmpty()) {
            IconButton(
              onClick = { showClearConfirmDialog = true },
              modifier = Modifier.testTag("clear_history_button")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Clear All History",
                tint = TextSecondary
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = DarkCardBackground
        )
      )
    }
  ) { innerPadding ->
    if (historyList.isEmpty()) {
      EmptyHistoryView(
        onBack = onBack,
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      )
    } else {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 340.dp),
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(
          items = historyList,
          key = { it.id }
        ) { item ->
          HistoryItemCard(
            item = item,
            onOpen = { onOpenItem(item) },
            onRegenerate = { onRegenerateItem(item) },
            onDelete = { onDeleteItem(item) },
            onSave = { onSaveItem(item) },
            onShare = { onShareItem(item) }
          )
        }
      }
    }
  }

  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      containerColor = DarkCardBackground,
      title = {
        Text(
          text = "Clear All History?",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
      },
      text = {
        Text(
          text = "This will permanently delete all saved images and prompt records from local storage.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onClearAll()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Clear All", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
fun EmptyHistoryView(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
        .background(DarkSurfaceVariant)
        .border(1.dp, DarkBorder, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.History,
        contentDescription = null,
        tint = CyanLight,
        modifier = Modifier.size(40.dp)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    Text(
      text = "No Creations in History Yet",
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
      color = TextPrimary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Every portrait and image generated with Seyon AI will be securely saved here along with prompt prompts, styles, and settings.",
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onBack,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = Color.Black,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Start Creating",
        fontWeight = FontWeight.Bold,
        color = Color.Black
      )
    }
  }
}

@Composable
fun HistoryItemCard(
  item: HistoryEntity,
  onOpen: () -> Unit,
  onRegenerate: () -> Unit,
  onDelete: () -> Unit,
  onSave: () -> Unit,
  onShare: () -> Unit,
  modifier: Modifier = Modifier
) {
  val file = remember(item.localImagePath) { File(item.localImagePath) }
  val bitmap = remember(item.localImagePath) {
    if (file.exists()) {
      try {
        BitmapFactory.decodeFile(file.absolutePath)
      } catch (e: Exception) {
        null
      }
    } else null
  }

  val dateStr = remember(item.timestamp) {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    sdf.format(Date(item.timestamp))
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("history_item_${item.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight))
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Top info bar: Date + Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = dateStr,
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Style badge
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = CyanPrimary.copy(alpha = 0.15f),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(CyanLight.copy(alpha = 0.5f), CyanPrimary.copy(alpha = 0.5f)))
            )
          ) {
            Text(
              text = item.stylePreset,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = CyanLight,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          // Aspect ratio badge
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = DarkSurfaceElevated,
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight))
            )
          ) {
            Text(
              text = item.aspectRatio,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = TextSecondary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          if (item.preserveIdentity) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = VioletLight.copy(alpha = 0.15f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Face,
                  contentDescription = null,
                  tint = VioletLight,
                  modifier = Modifier.size(11.dp)
                )
                Text(
                  text = "Identity",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = VioletLight
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Middle: Image preview + prompt
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
      ) {
        // Thumbnail
        Box(
          modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .clickable { onOpen() },
          contentAlignment = Alignment.Center
        ) {
          if (bitmap != null) {
            Image(
              bitmap = bitmap.asImageBitmap(),
              contentDescription = "History Item Thumbnail",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // Details Column
        Column(
          modifier = Modifier
            .weight(1f)
            .height(100.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = item.prompt,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = TextPrimary,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Metadata line: Seed & steps
          Text(
            text = "Seed: ${item.seed ?: "Random"} • Steps: ${item.numInferenceSteps} • Scale: ${item.guidanceScale}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Action Bar: Open, Regenerate, Save, Share, Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Open in Studio
          OutlinedButton(
            onClick = onOpen,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(CyanLight, CyanPrimary))
            )
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = null,
              tint = CyanLight,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Open",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = CyanLight
            )
          }

          // Regenerate
          OutlinedButton(
            onClick = onRegenerate,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight))
            )
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              tint = TextPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Regenerate",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = TextPrimary
            )
          }
        }

        // Secondary quick actions
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          IconButton(
            onClick = onSave,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Download,
              contentDescription = "Save to Phone",
              tint = TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = onShare,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Image",
              tint = TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete from History",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
