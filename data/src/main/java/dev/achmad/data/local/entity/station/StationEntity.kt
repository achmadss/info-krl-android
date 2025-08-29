package dev.achmad.data.local.entity.station

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey
    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "favorite")
    val favorite: Boolean? = false,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "active")
    val active: Boolean? = null,

    @ColumnInfo(name = "daop")
    val daop: Int? = null,

    @ColumnInfo(name = "fg_enable")
    val fgEnable: Int? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)