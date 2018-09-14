package br.com.rafael.pocketclass.services;

import br.com.rafael.pocketclass.modelo.Aluno;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AlunoService {

    @POST("aluno")
    Call<Void> insere(@Body Aluno aluno);
}
