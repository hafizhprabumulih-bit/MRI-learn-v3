package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "node_progress",
    indices = [
        Index(value = ["nodeCode"]),
        Index(value = ["moduleId"])
    ]
)
data class NodeProgressEntity(
    @PrimaryKey val nodeCode: String,
    val completedAt: Long,
    val moduleId: String = if (nodeCode.contains(".")) nodeCode.substringBefore(".") else ""
)

@Entity(
    tableName = "study_notes",
    indices = [
        Index(value = ["category"]),
        Index(value = ["moduleId"]),
        Index(value = ["nodeCode"]),
        Index(value = ["updatedAt"])
    ]
)
data class StudyNoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long,
    val nodeCode: String? = null,
    val moduleId: String? = null
)

@Entity(
    tableName = "quiz_attempts",
    indices = [
        Index(value = ["quizId"]),
        Index(value = ["nodeCode"]),
        Index(value = ["moduleId"]),
        Index(value = ["answeredAt"])
    ]
)
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val quizId: String,
    val nodeCode: String,
    val moduleId: String = if (nodeCode.contains(".")) nodeCode.substringBefore(".") else "",
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val answeredAt: Long
)

@Entity(
    tableName = "study_days",
    indices = [
        Index(value = ["dateIso"])
    ]
)
data class StudyDayEntity(
    @PrimaryKey val dateIso: String,
    val nodesCompletedCount: Int = 0,
    val lastNodeCode: String? = null,
    val lastModuleId: String? = null,
    @ColumnInfo(defaultValue = "0") val hasFlashcardActivity: Boolean = false
)

@Entity(
    tableName = "mri_modules",
    indices = [
        Index(value = ["id"]),
        Index(value = ["sortOrder"])
    ]
)
data class MriModuleEntity(
    @PrimaryKey val id: String,
    val code: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val levelBadge: String,
    val iconName: String,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "mri_nodes",
    indices = [
        Index(value = ["code"]),
        Index(value = ["moduleId"]),
        Index(value = ["level"]),
        Index(value = ["sortOrder"])
    ]
)
data class MriNodeEntity(
    @PrimaryKey val code: String,
    val moduleId: String,
    val level: Int,
    val title: String,
    val summaryShort: String,
    val detailFull: String,
    val analogi: String,
    val prerequisite: String? = null,
    val learningGoal: String? = null,
    val crossLinks: String? = null,
    val labType: String? = null,
    val sortOrder: Int = 0,
    val htmlSimulationAsset: String? = null
)

@Entity(
    tableName = "quiz_questions",
    indices = [
        Index(value = ["id"]),
        Index(value = ["moduleId"]),
        Index(value = ["nodeCode"]),
        Index(value = ["level"])
    ]
)
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val nodeCode: String,
    val level: Int = 1,
    val levelBadge: String = "Dasar",
    val question: String,
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val optionsJson: String = "",
    val correctIndex: Int,
    val explanation: String
)

@Entity(
    tableName = "curriculum_nodes",
    indices = [
        Index(value = ["code"]),
        Index(value = ["moduleId"]),
        Index(value = ["level"]),
        Index(value = ["sortOrder"])
    ]
)
data class CurriculumNodeEntity(
    @PrimaryKey val code: String,
    val moduleId: String,
    val level: Int, // 0 = Tujuan, 1 = Dasar, 2 = Menengah, 3 = Lanjut
    val title: String,
    val summaryShort: String,
    val detailFull: String,
    val analogi: String,
    val prerequisite: String? = null,
    val learningGoal: String? = null,
    val crossLinks: String? = null,
    val labType: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "curriculum_quizzes",
    indices = [
        Index(value = ["id"]),
        Index(value = ["moduleId"]),
        Index(value = ["nodeCode"]),
        Index(value = ["level"])
    ]
)
data class CurriculumQuizEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val nodeCode: String,
    val level: Int, // 1, 2, atau 3
    val question: String,
    val optionsJson: String,
    val correctIndex: Int,
    val explanation: String
)
