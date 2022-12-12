package com.grash.utils;

import com.grash.model.Reading;

import java.util.Comparator;

public class ReadingComparator implements Comparator<Reading> {
    @Override
    public int compare(Reading o1, Reading o2) {
        return o1.getCreatedAt().compareTo(o2.getCreatedAt());
    }
}
