package com.qi.voiceinput.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit client=null;

    public static synchronized ApiService getClient(){
        if (null==ApiClient.client){
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            Retrofit.Builder builder=new Retrofit.Builder()
                    .baseUrl(AllUrls.BASE_URL)
                    .client(clientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create());
            ApiClient.client= builder.build();
        }
        return client.create(ApiService.class);
    }

}
