package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeProgressDao {
    @Query("SELECT * FROM node_progress")
    fun observeCompletedNodes(): Flow<List<NodeProgressEntity>>

    @Query("SELECT * FROM node_progress WHERE moduleId = :moduleId")
    fun observeCompletedNodesByModule(moduleId: String): Flow<List<NodeProgressEntity>>

    @Query("SELECT * FROM node_progress WHERE nodeCode LIKE :prefix || '%'")
    fun observeCompletedNodesByPrefix(prefix: String): Flow<List<NodeProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(node: NodeProgressEntity)

    @Query("DELETE FROM node_progress WHERE nodeCode = :nodeCode")
    suspend fun delete(nodeCode: String)

    @Query("SELECT * FROM node_progress")
    suspend fun getAll(): List<NodeProgressEntity>

    @Query("SELECT * FROM node_progress WHERE nodeCode = :nodeCode LIMIT 1")
    suspend fun getByNodeCode(nodeCode: String): NodeProgressEntity?

    @Query("SELECT COUNT(*) FROM node_progress WHERE moduleId = :moduleId")
    suspend fun getCountByModule(moduleId: String): Int

    @Query("SELECT COUNT(*) FROM node_progress WHERE nodeCode LIKE :prefix || '%'")
    suspend fun getCountByPrefix(prefix: String): Int

    @Query("SELECT COUNT(*) FROM node_progress WHERE completedAt >= :startOfDayMillis AND completedAt < :endOfDayMillis")
    suspend fun getCountCompletedBetween(startOfDayMillis: Long, endOfDayMillis: Long): Int

    @Query("SELECT nodeCode FROM node_progress WHERE completedAt >= :startOfDayMillis AND completedAt < :endOfDayMillis ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLatestCompletedNodeCodeBetween(startOfDayMillis: Long, endOfDayMillis: Long): String?
}

@Dao
interface StudyNoteDao {
    @Query("SELECT * FROM study_notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE moduleId = :moduleId ORDER BY updatedAt DESC")
    fun observeNotesByModule(moduleId: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE nodeCode = :nodeCode ORDER BY updatedAt DESC")
    fun observeNotesByNode(nodeCode: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE nodeCode LIKE :prefix || '%' ORDER BY updatedAt DESC")
    fun observeNotesByPrefix(prefix: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE category = :category ORDER BY updatedAt DESC")
    fun observeNotesByCategory(category: String): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: StudyNoteEntity)

    @Query("DELETE FROM study_notes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM study_notes")
    suspend fun getCount(): Int

    @Query("SELECT * FROM study_notes")
    suspend fun getAll(): List<StudyNoteEntity>

    @Query("SELECT * FROM study_notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): StudyNoteEntity?

    @Query("UPDATE study_notes SET nodeCode = :newCode, moduleId = :newModule WHERE nodeCode = :oldCode")
    suspend fun renameNode(oldCode: String, newCode: String, newModule: String)
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts ORDER BY answeredAt DESC")
    fun observeQuizAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE moduleId = :moduleId ORDER BY answeredAt DESC")
    fun observeQuizAttemptsByModule(moduleId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE nodeCode = :nodeCode ORDER BY answeredAt DESC")
    fun observeQuizAttemptsByNode(nodeCode: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE nodeCode LIKE :prefix || '%' ORDER BY answeredAt DESC")
    fun observeQuizAttemptsByPrefix(prefix: String): Flow<List<QuizAttemptEntity>>

    @Insert
    suspend fun insert(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts")
    suspend fun getAll(): List<QuizAttemptEntity>

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE moduleId = :moduleId AND isCorrect = 1")
    suspend fun getCorrectCountByModule(moduleId: String): Int

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE moduleId = :moduleId")
    suspend fun getTotalCountByModule(moduleId: String): Int

    @Query("UPDATE quiz_attempts SET nodeCode = :newCode, moduleId = :newModule WHERE nodeCode = :oldCode")
    suspend fun renameNode(oldCode: String, newCode: String, newModule: String)

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE answeredAt >= :startOfDayMillis AND answeredAt < :endOfDayMillis")
    suspend fun getCountAnsweredBetween(startOfDayMillis: Long, endOfDayMillis: Long): Int
}

@Dao
interface StudyDayDao {
    @Query("SELECT * FROM study_days")
    fun observeStudyDays(): Flow<List<StudyDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(day: StudyDayEntity)

    @Query("SELECT * FROM study_days")
    suspend fun getAll(): List<StudyDayEntity>

    @Query("SELECT COUNT(*) FROM study_days")
    suspend fun getCount(): Int

    @Query("SELECT * FROM study_days WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getByDate(dateIso: String): StudyDayEntity?

    @Query("DELETE FROM study_days WHERE dateIso = :dateIso")
    suspend fun deleteByDate(dateIso: String)

    @Query("UPDATE study_days SET lastNodeCode = :newCode, lastModuleId = :newModule WHERE lastNodeCode = :oldCode")
    suspend fun renameLastNode(oldCode: String, newCode: String, newModule: String)
}

@Dao
interface MriCurriculumDao {
    @Query("SELECT * FROM mri_modules ORDER BY sortOrder ASC, id ASC")
    fun observeAllModules(): Flow<List<MriModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<MriModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: MriModuleEntity)

    @Query("SELECT COUNT(*) FROM mri_modules")
    suspend fun getModulesCount(): Int

    @Query("DELETE FROM mri_modules")
    suspend fun clearModules()
}

@Dao
interface CurriculumDao {
    @Query("SELECT * FROM mri_modules ORDER BY sortOrder ASC, id ASC")
    fun observeAllModules(): Flow<List<MriModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<MriModuleEntity>)

    @Query("SELECT * FROM curriculum_nodes ORDER BY moduleId ASC, sortOrder ASC, level ASC, code ASC")
    fun observeAllNodes(): Flow<List<CurriculumNodeEntity>>

    @Query("SELECT * FROM curriculum_nodes WHERE moduleId = :moduleId ORDER BY sortOrder ASC, level ASC, code ASC")
    fun observeNodesByModule(moduleId: String): Flow<List<CurriculumNodeEntity>>

    @Query("SELECT * FROM curriculum_nodes WHERE moduleId = :moduleId AND level = :level ORDER BY sortOrder ASC, code ASC")
    fun observeNodesByModuleAndLevel(moduleId: String, level: Int): Flow<List<CurriculumNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<CurriculumNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: CurriculumNodeEntity)

    @Query("SELECT COUNT(*) FROM curriculum_nodes")
    suspend fun getNodesCount(): Int

    @Query("SELECT * FROM curriculum_quizzes ORDER BY moduleId ASC, level ASC, id ASC")
    fun observeAllQuizzes(): Flow<List<CurriculumQuizEntity>>

    @Query("SELECT * FROM curriculum_quizzes WHERE moduleId = :moduleId ORDER BY level ASC, id ASC")
    fun observeQuizzesByModule(moduleId: String): Flow<List<CurriculumQuizEntity>>

    @Query("SELECT * FROM curriculum_quizzes WHERE moduleId = :moduleId AND level = :level ORDER BY id ASC")
    fun observeQuizzesByModuleAndLevel(moduleId: String, level: Int): Flow<List<CurriculumQuizEntity>>

    @Query("SELECT * FROM curriculum_quizzes WHERE level = :level ORDER BY moduleId ASC, id ASC")
    fun observeQuizzesByLevel(level: Int): Flow<List<CurriculumQuizEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<CurriculumQuizEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: CurriculumQuizEntity)

    @Query("SELECT COUNT(*) FROM curriculum_quizzes")
    suspend fun getQuizzesCount(): Int

    @Query("DELETE FROM curriculum_nodes")
    suspend fun clearNodes()

    @Query("DELETE FROM curriculum_quizzes")
    suspend fun clearQuizzes()
}
