package org.happypeng.roomrecyclerviewexample;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {MyEntry.class}, version = 1, exportSchema = false)
abstract public class MyDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "MyDatabase";

    public abstract MyDao getMyDao();
}
