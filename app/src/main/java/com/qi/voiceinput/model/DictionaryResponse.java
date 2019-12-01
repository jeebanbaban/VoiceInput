package com.qi.voiceinput.model;

import java.util.List;

public class DictionaryResponse {

    private List<Dictionary> dictionary = null;

    public List<Dictionary> getDictionary() {
        return dictionary;
    }

    public void setDictionary(List<Dictionary> dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String toString() {
        return "DictionaryResponse{" +
                "dictionary=" + dictionary +
                '}';
    }
}
