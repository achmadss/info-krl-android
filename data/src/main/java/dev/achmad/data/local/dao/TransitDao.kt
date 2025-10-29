package dev.achmad.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.achmad.data.local.entity.transit.TransitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransitDao {

    @Query("SELECT * FROM transits WHERE origin_station_id = :originStationId AND destination_station_id = :destinationStationId LIMIT 1")
    suspend fun awaitSingle(
        originStationId: String,
        destinationStationId: String
    ): TransitEntity?

    @Query("SELECT * FROM transits WHERE origin_station_id = :originStationId AND destination_station_id = :destinationStationId LIMIT 1")
    fun subscribeSingle(
        originStationId: String,
        destinationStationId: String
    ): Flow<TransitEntity?>

    @Query("SELECT * FROM transits")
    suspend fun awaitAll(): List<TransitEntity>

    @Query("SELECT * FROM transits")
    fun subscribeAll(): Flow<List<TransitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transit: TransitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg transit: TransitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transits: List<TransitEntity>)

    @Delete
    suspend fun delete(transit: TransitEntity)

    @Delete
    suspend fun delete(vararg transit: TransitEntity)

    @Delete
    suspend fun delete(transits: List<TransitEntity>)

    @Query("DELETE FROM transits")
    suspend fun deleteAll()
}