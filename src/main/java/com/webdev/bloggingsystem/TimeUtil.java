package com.webdev.bloggingsystem;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component("timeUtil")
public class TimeUtil {

    public static String toRelativeTime(Instant time) {
        Duration diff = Duration.between(time, Instant.now());
        long seconds = diff.getSeconds();
        if (seconds < 60) return "Less than a minute ago.";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + (minutes == 1 ? " minute" : " minutes") + " ago.";
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour" : " hours") + " ago.";
        long days = hours / 24;
        if (days < 30) return days + (days == 1 ? " day" : " days") + " ago.";
        long months = days / 30;
        if (months < 12) return months + (months == 1 ? " month" : " months") + " ago.";
        long years = months / 12;
        return years + (years < 2 ? " year" : " years") + " ago.";
    }
}
