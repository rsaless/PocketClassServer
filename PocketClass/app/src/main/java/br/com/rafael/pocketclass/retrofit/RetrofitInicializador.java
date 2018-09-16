package br.com.rafael.pocketclass.retrofit;

import br.com.rafael.pocketclass.services.AlunoService;
import br.com.rafael.pocketclass.services.DispositivoService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;


public class RetrofitInicializador {
    public String ip = "192.168.56.1";
    private final Retrofit retrofit;

    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);


    OkHttpClient.Builder client = new OkHttpClient.Builder().addInterceptor(interceptor);

    public RetrofitInicializador(){
        retrofit = new Retrofit.Builder()
                .baseUrl("http://"+ip+":8080/api/")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client.build())
                .build();
    }

    public AlunoService getAlunoService() {
        return retrofit.create(AlunoService.class);
    }
    public DispositivoService getDispositivoService() {return retrofit.create(DispositivoService.class);}
}
