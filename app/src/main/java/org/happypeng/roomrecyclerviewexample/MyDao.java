package org.happypeng.roomrecyclerviewexample;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import androidx.paging.DataSource;
import androidx.room.Update;

@Dao
public interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEntry(MyEntry aEntry);

    @Query("SELECT * FROM MyEntry")
    List<MyEntry> getAll();

    @Query("SELECT * FROM MyEntry")
    DataSource.Factory<Integer, MyEntry> getAllPaged();

    @Delete
    void deleteMany(List<MyEntry> aList);

    @Update
    void update(MyEntry aEntry);
}
