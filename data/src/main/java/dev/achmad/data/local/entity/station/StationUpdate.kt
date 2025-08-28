package dev.achmad.data.local.entity.station

import androidx.room.ColumnInfo

data class StationUpdate(
    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "active")
    val active: Boolean?,

    @ColumnInfo(name = "daop")
    val daop: Int?,

    @ColumnInfo(name = "fg_enable")
    val fgEnable: Int?,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)