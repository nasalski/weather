package com.example.wwwsl.testweather;

/**
 * Created by wwwsl on 02.08.2017.
 */

public class FireMessage {
    private String city;
    private long high;
    private long low;

    public FireMessage() {
    }

    public FireMessage(String city, long high, long low) {
        this.city = city;
        this.high = high;
        this.low = low;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }
}
