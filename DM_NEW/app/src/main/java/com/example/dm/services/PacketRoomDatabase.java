package com.example.dm.services;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Packet.class}, version = 1, exportSchema = false)
public abstract class PacketRoomDatabase extends RoomDatabase {

    public abstract PacketDAO packetDAO();

    private static volatile PacketRoomDatabase INSTANCE;

    /**
     * Singleton instance for the db object
     * @param context
     * @return
     */
    public static PacketRoomDatabase getInstance(Context context){
        if(INSTANCE == null) {
            synchronized (PacketRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context,
                            PacketRoomDatabase.class,
                            "PacketDB"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
