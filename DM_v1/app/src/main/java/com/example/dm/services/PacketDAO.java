package com.example.dm.services;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PacketDAO {

    @Insert
    void insertPacket(Packet packet);

    @Insert
    void insertMultiplePackets(List<Packet> packets);

    @Query("select count(*) from packet_table")
    int getPacketCount();

    @Query("select count(*) from packet_table where commitStatus='1'")
    int getCommittedPacketCount();

    @Query("select distinct commitStatus from packet_table")
    List<String> getDistinctVal();

    @Query("select * from packet_table where commitStatus like '0' order by timestamp, metaSeqNumber limit 1")
    List<Packet> getPacketsNotCommitted();

    @Query("select * from packet_table where commitStatus like '0' order by timestamp, metaSeqNumber limit 1")
    Packet getSinglePacketsNotCommitted();

    @Query("select * from packet_table where pktId like :pkt_id")
    Packet findPacketByPktId(int pkt_id);

    @Delete
    void deletePacket(Packet packet);

    @Query("delete from packet_table where commitStatus like '1'")
    void deleteCommittedPacket();

    @Update
    void updatePacket(Packet packet);

    @Update
    void updateNPacketsToCommitted(List<Packet> packets);

}
