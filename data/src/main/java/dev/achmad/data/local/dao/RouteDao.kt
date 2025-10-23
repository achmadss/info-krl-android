package dev.achmad.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.achmad.data.local.entity.route.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Query("SELECT * FROM routes WHERE train_id = :trainId")
    suspend fun awaitSingle(trainId: String): RouteEntity?

    @Query("SELECT * FROM routes WHERE train_id = :trainId")
    suspend fun awaitAllByTrainId(trainId: String): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE train_id = :trainId LIMIT 1")
    fun subscribeSingle(trainId: String): Flow<RouteEntity?>

    @Query("SELECT * FROM routes WHERE train_id IN (:trainIds)")
    suspend fun awaitMultiple(trainIds: List<String>): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE train_id IN (:trainIds)")
    fun subscribeMultiple(trainIds: List<String>): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes")
    suspend fun awaitAll(): List<RouteEntity>

    @Query("SELECT * FROM routes")
    fun subscribeAll(): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routes: List<RouteEntity>)

    @Delete
    suspend fun delete(route: RouteEntity)

    @Delete
    suspend fun delete(vararg route: RouteEntity)

    @Delete
    suspend fun delete(routes: List<RouteEntity>)

    @Query("DELETE FROM routes")
    suspend fun deleteAll()

}