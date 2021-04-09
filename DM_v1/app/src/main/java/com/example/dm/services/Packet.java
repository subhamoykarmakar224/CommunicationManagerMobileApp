package com.example.dm.services;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "packet_table")
public class Packet {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pktId")
    private int pktId;

    @ColumnInfo(name = "sensor_id")
    private String sensorId;

    @ColumnInfo(name = "rssi")
    private String rssi;

    @ColumnInfo(name = "native") // native MAC
    private String nativeMAC;

    @ColumnInfo(name = "lon")
    private String longitude;

    @ColumnInfo(name = "lat")
    private String latitude;

    @ColumnInfo(name = "id") // base16Mac
    private String id;

    @ColumnInfo(name = "advrecord")
    private String advRecord;

    @ColumnInfo(name = "name") // device name
    private String name;

    @ColumnInfo(name = "type") // deviceType
    private String type;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    @ColumnInfo(name = "first_packet")
    private String first_packet;

    @ColumnInfo(name = "commitStatus")
    private String commitStatus;

    @ColumnInfo(name = "orderedDeliveryStatus")
    private int orderedDeliveryStatus;

    @ColumnInfo(name = "msgSemantics")
    private int msgSemantics;

    @ColumnInfo(name = "metaSeqNumber")
    private int metaSeqNumber;

    public Packet(String sensorId, String rssi, String nativeMAC, String longitude, String latitude, String id, String advRecord, String name, String type, String timestamp, String first_packet, String commitStatus, int orderedDeliveryStatus, int msgSemantics, int metaSeqNumber) {
        this.sensorId = sensorId;
        this.rssi = rssi;
        this.nativeMAC = nativeMAC;
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = id;
        this.advRecord = advRecord;
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
        this.first_packet = first_packet;
        this.commitStatus = commitStatus;
        this.orderedDeliveryStatus = orderedDeliveryStatus;
        this.msgSemantics = msgSemantics;
        this.metaSeqNumber = metaSeqNumber;
    }

    public int getPktId() {
        return pktId;
    }

    public void setPktId(int pktId) {
        this.pktId = pktId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getNativeMAC() {
        return nativeMAC;
    }

    public void setNativeMAC(String nativeMAC) {
        this.nativeMAC = nativeMAC;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdvRecord() {
        return advRecord;
    }

    public void setAdvRecord(String advRecord) {
        this.advRecord = advRecord;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFirst_packet() {
        return first_packet;
    }

    public void setFirst_packet(String first_packet) {
        this.first_packet = first_packet;
    }

    public String getCommitStatus() {
        return commitStatus;
    }

    public void setCommitStatus(String commitStatus) {
        this.commitStatus = commitStatus;
    }

    public int getOrderedDeliveryStatus() {
        return orderedDeliveryStatus;
    }

    public void setOrderedDeliveryStatus(int orderedDeliveryStatus) {
        this.orderedDeliveryStatus = orderedDeliveryStatus;
    }

    public int getMsgSemantics() {
        return msgSemantics;
    }

    public void setMsgSemantics(int msgSemantics) {
        this.msgSemantics = msgSemantics;
    }

    public int getMetaSeqNumber() {
        return metaSeqNumber;
    }

    public void setMetaSeqNumber(int metaSeqNumber) {
        this.metaSeqNumber = metaSeqNumber;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "pktId=" + pktId +
                ", sensorId='" + sensorId + '\'' +
                ", rssi='" + rssi + '\'' +
                ", nativeMAC='" + nativeMAC + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", id='" + id + '\'' +
                ", advRecord='" + advRecord + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", first_packet='" + first_packet + '\'' +
                ", commitStatus='" + commitStatus + '\'' +
                ", orderedDeliveryStatus=" + orderedDeliveryStatus +
                ", msgSemantics=" + msgSemantics +
                ", metaSeqNumber=" + metaSeqNumber +
                '}';
    }
}

//     var json2 = "{\"sensor_id\":" + "\"" + newPost.clientID + "\"" + ",";
//    json2 = json2 + "\"name\":" + "\"" + newPost.name + "\"" + ",";
//    json2 = json2 + "\"type\":" + "\"" + newPost.type + "\"" + ",";
//    json2 = json2 + "\"timestamp\":" + "\"" + newPost.timestamp + "\"" + ",";
//    json2 = json2 + "\"payload\":{\"id\":" + "\"" + newPost.id + "\"" + ",";
//    json2 = json2 + "\"native\":" + "\"" + newPost.native + "\"" + ",";
//  json2 = json2 + "\"rssi\":" + "\"" + newPost.rssi + "\"" + ",";
//  json2 = json2 + "\"lat\":" + "\"" + newPost.lat + "\"" + ",";
//  json2 = json2 + "\"lon\":" + "\"" + newPost.lon + "\"" + ",";
//  json2 = json2 + "\"advrecord\":" + "\"" + newPost.advRecord + "\"";
//  json2 = json2 + "}}";