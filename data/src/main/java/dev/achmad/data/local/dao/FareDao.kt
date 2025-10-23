package dev.achmad.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.achmad.data.local.entity.fare.FareEntity

@Dao
interface FareDao {

    @Query("SELECT * FROM fares WHERE station_from = :originStationId AND station_to = :destinationStationId")
    suspend fun awaitSingle(
        originStationId: String,
        destinationStationId: String,
    ): FareEntity?

    @Query("SELECT * FROM fares")
    suspend fun awaitAll(): List<FareEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fare: FareEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg fare: FareEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fares: List<FareEntity>)

    @Delete
    suspend fun delete(fare: FareEntity)

    @Delete
    suspend fun delete(vararg fare: FareEntity)

    @Delete
    suspend fun delete(fares: List<FareEntity>)

    @Query("DELETE FROM fares")
    suspend fun deleteAll()

}