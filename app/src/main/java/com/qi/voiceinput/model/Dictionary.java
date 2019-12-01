package com.qi.voiceinput.model;

import android.graphics.Typeface;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import java.io.Serializable;

public class Dictionary implements Serializable, Comparable {

    private String word;
    private int frequency;
    private boolean highlightStatus;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public boolean isHighlightStatus() {
        return highlightStatus;
    }

    public void setHighlightStatus(boolean highlightStatus) {
        this.highlightStatus = highlightStatus;
    }

    @BindingAdapter("isBold")
    public static void setBold(TextView view, boolean isBold) {
        if (isBold) {
            view.setTypeface(null, Typeface.BOLD);
        } else {
            view.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int compareTo(Object o) {
        int frequencyOld = ((Dictionary) o).frequency;
        //Descending order sorting
        return  frequencyOld - this.frequency;
    }

    @Override
    public String toString() {
        return "Dictionary{" +
                "word='" + word + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}
