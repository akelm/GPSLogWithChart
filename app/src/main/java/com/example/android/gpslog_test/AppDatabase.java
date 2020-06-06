package com.example.android.gpslog_test;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Database(entities = {TypeEntity.class, ExerciseEntity.class, TrackEntity.class}, version = 1)
abstract public class AppDatabase extends RoomDatabase {

    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile AppDatabase INSTANCE;
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            AtomicReference<List<TypeEntity>> types = new AtomicReference<>(Collections.emptyList());
            List<String> exerTypes = Arrays.asList("WALK", "RUN", "BIKE");
            AppDatabaseDao dao = INSTANCE.appDatabaseDao();
            
            Runnable runnable = () -> {
                types.set(dao.getTypes());
            };
            AppDatabase.databaseWriteExecutor.execute(runnable);

            List<String> typesString = types.get().stream().map(TypeEntity::getTypeName).collect(Collectors.toList());

            for (String s : exerTypes) {
                if (!typesString.contains(s)) {
                    databaseWriteExecutor.execute(() -> {
                        dao.insertType(new TypeEntity(s));
                    });

                }
            }

        }
    };

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    abstract AppDatabaseDao appDatabaseDao();

}
