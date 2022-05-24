package com.example.darren.roomexample

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    @Insert
    suspend fun insert(employeeEntity: EmployeeEntity)

    @Update
    suspend fun update(employeeEntity: EmployeeEntity)

    @Delete
    suspend fun delete(employeeEntity: EmployeeEntity)

    @Query(value = "SELECT * FROM `employee-table`")
    fun fetchAllEmployees(): Flow<List<EmployeeEntity>>

    @Query(value = "SELECT * FROM `employee-table` WHERE id=:id")
    fun fetchEmployeeById(id: Int): Flow<EmployeeEntity>

}