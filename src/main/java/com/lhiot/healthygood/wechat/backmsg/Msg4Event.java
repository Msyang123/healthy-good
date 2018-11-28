//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.lhiot.healthygood.wechat.backmsg;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.w3c.dom.Document;

public class Msg4Event extends Msg {
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String CLICK = "CLICK";
    public static final String SCAN = "scan";
    public static final String LOCATION = "LOCATION";
    private String event;
    private String eventKey;
    private String ticket;
    private String latitude;
    private String longitude;
    private String precision;

    public Msg4Event(Msg4Head head) {
        this.head = head;
    }

    public void write(Document document) {
    }

    public void read(Document document) {
        this.event = this.getElementContent(document, "Event");
        if (!"subscribe".equals(this.event) && !"unsubscribe".equals(this.event) && !"scan".equals(this.event)) {
            if ("LOCATION".equals(this.event)) {
                this.latitude = this.getElementContent(document, "Latitude");
                this.longitude = this.getElementContent(document, "Longitude");
                this.precision = this.getElementContent(document, "Precision");
            } else if ("CLICK".equals(this.event)) {
                this.eventKey = this.getElementContent(document, "EventKey");
            }
        } else {
            this.eventKey = this.getElementContent(document, "EventKey");
            this.ticket = this.getElementContent(document, "Ticket");
        }

    }

    public String getEvent() {
        return this.event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventKey() {
        return this.eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getTicket() {
        return this.ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPrecision() {
        return this.precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }
}
