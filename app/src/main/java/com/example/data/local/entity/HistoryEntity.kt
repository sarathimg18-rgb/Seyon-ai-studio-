package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generation_history")
data class HistoryEntity(
  @PrimaryKey
  val id: String,
  val prompt: String,
  val referenceImageUri: String?,
  val localImagePath: String,
  val timestamp: Long = System.currentTimeMillis(),
  val preserveIdentity: Boolean = true,
  val stylePreset: String = "Realistic",
  val aspectRatio: String = "1:1",
  val seed: Long? = null,
  val guidanceScale: Float = 7.5f,
  val numInferenceSteps: Int = 30,
  val numImages: Int = 1
)
