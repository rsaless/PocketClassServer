package br.com.rafael.pocketclass.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitInicializador {

    private final Retrofit retrofit;

    public RetrofitInicializador(){
        retrofit = new Retrofit.Builder().baseUrl("http://200.18.98.222:8080/api/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
}
