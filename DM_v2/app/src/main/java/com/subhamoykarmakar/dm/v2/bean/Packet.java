package com.subhamoykarmakar.dm.v2.bean;

public class Packet {
    private int pktId;
    private String sensorId;
    private String rssi;
    private String nativeMAC;
    private String longitude;
    private String latitude;
    private String id;
    private String advRecord;
    private String name;
    private String type;
    private String timestamp;
    private String first_packet;
    private String commitStatus;
    private int orderedDeliveryStatus;
    private int msgSemantics;
    private int metaSeqNumber;

    public Packet(int pktId, String sensorId, String rssi, String nativeMAC, String longitude, String latitude, String id, String advRecord, String name, String type, String timestamp, String first_packet, String commitStatus, int orderedDeliveryStatus, int msgSemantics, int metaSeqNumber) {
        this.pktId = pktId;
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
