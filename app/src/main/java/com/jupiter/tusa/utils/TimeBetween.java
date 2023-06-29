package com.jupiter.tusa.utils;

public class TimeBetween {
    long hours;
    long minutes;
    long seconds;

    TimeBetween(long hours, long minutes, long seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public long getHours() {return hours;}
    public long getMinutes() {return minutes;}
    public long getSeconds() {return seconds;}


}
