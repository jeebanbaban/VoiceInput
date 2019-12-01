package com.qi.voiceinput.network;

import com.qi.voiceinput.model.DictionaryResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET(AllUrls.DICTIONARY_URL)
    Call<DictionaryResponse> getDictionary();
}
