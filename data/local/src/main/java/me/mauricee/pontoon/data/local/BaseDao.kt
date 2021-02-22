package me.mauricee.pontoon.data.local

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

abstract class BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entity: T): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(entity: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entities: List<T>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(entities: List<T>)

    @Transaction
    inline fun <reified A : T> upsert(entity: A) {
        if (insert(entity) == -1L)
            update(entity)
    }

    @Transaction
    inline fun <reified A : T> upsert(entities: List<A>) {
        val existingEntites = insert(entities).mapIndexed { index, l -> l to entities[index] }
                .filter { it.first == -1L }
                .map { it.second }
        if (existingEntites.isNotEmpty())
            update(existingEntites)
    }
}