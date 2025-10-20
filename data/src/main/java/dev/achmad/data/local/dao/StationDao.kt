package dev.achmad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import dev.achmad.data.local.entity.station.StationEntity
import dev.achmad.data.local.entity.station.StationUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {

    @Query("SELECT * FROM stations WHERE id = :id")
    suspend fun awaitSingle(id: String): StationEntity?

    @Query("SELECT * FROM stations WHERE id = :id LIMIT 1")
    fun subscribeSingle(id: String): Flow<StationEntity?>

    @Query("""
        SELECT * FROM stations
        WHERE (:applyFavorite = 0 OR favorite = :favorite)
        AND id IN (:ids)
    """)
    suspend fun awaitMultiple(
        ids: List<String>,
        favorite: Boolean? = null,
        applyFavorite: Int = if (favorite == null) 0 else 1
    ): List<StationEntity>

    @Query("""
        SELECT * FROM stations
        WHERE (:applyFavorite = 0 OR favorite = :favorite)
        AND id IN (:ids)
    """)
    fun subscribeMultiple(
        ids: List<String>,
        favorite: Boolean? = null,
        applyFavorite: Int = if (favorite == null) 0 else 1
    ): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE (:applyFavorite = 0 OR favorite = :favorite)")
    suspend fun awaitAll(
        favorite: Boolean? = null,
        applyFavorite: Int = if (favorite == null) 0 else 1
    ): List<StationEntity>

    @Query("SELECT * FROM stations WHERE (:applyFavorite = 0 OR favorite = :favorite)")
    fun subscribeAll(
        favorite: Boolean? = null,
        applyFavorite: Int = if (favorite == null) 0 else 1
    ): Flow<List<StationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: StationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg stations: StationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stations: List<StationEntity>)

    @Update
    suspend fun update(station: StationEntity)

    @Update
    suspend fun update(vararg stations: StationEntity)

    @Update
    suspend fun update(stations: List<StationEntity>)

    @Upsert(entity = StationEntity::class)
    suspend fun upsert(stationUpdate: StationUpdate)

    @Upsert(entity = StationEntity::class)
    suspend fun upsert(vararg stationUpdates: StationUpdate)

    @Upsert(entity = StationEntity::class)
    suspend fun upsert(stationUpdates: List<StationUpdate>)

    @Query("UPDATE stations SET favorite = 0, favorite_position = NULL WHERE id = :stationId")
    suspend fun unfavorite(stationId: String)

}