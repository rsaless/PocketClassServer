package br.com.rafael.pocketclass.utils.sinc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.com.rafael.pocketclass.data.dao.AlunoDAO;
import br.com.rafael.pocketclass.data.dto.AlunoSync;
import br.com.rafael.pocketclass.utils.event.AtualizaListaAlunoEvent;
import br.com.rafael.pocketclass.data.modelo.Aluno;
import br.com.rafael.pocketclass.utils.preferences.AlunoPreferences;
import br.com.rafael.pocketclass.web.retrofit.RetrofitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlunoSincronizador {

    private final Context context;
    private EventBus bus = EventBus.getDefault();
    private AlunoPreferences preferences;

    private void buscaAlunos() {
        Call<AlunoSync> call = new RetrofitInicializador().getAlunoService().lista();
        call.enqueue(buscaAlunosCallback());
    }
    private void buscaNovos() {
        String versao = preferences.getVersao();
        Call<AlunoSync> call = new RetrofitInicializador().getAlunoService().novos(versao);
        call.enqueue(buscaAlunosCallback());
    }
    private void sincronizaAlunosInternos(){
        final AlunoDAO dao = new AlunoDAO(context);
        final List<Aluno> alunos = dao.listaNaoSincronizados();
        dao.close();
        Call<AlunoSync> call = new RetrofitInicializador().getAlunoService().atualiza(alunos);
        call.enqueue(new Callback<AlunoSync>() {
            @Override
            public void onResponse(Call<AlunoSync> call, Response<AlunoSync> response) {
                AlunoSync alunoSync = response.body();
                sincroniza(alunoSync);
            }

            @Override
            public void onFailure(Call<AlunoSync> call, Throwable t) {

            }
        });
    }
    public void deleta(final Aluno aluno) {
        Call<Void> call = new RetrofitInicializador().getAlunoService().deleta(aluno.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                AlunoDAO dao = new AlunoDAO(context);
                dao.deleta(aluno);
                dao.close();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
    public void buscaTodos(){
        if (preferences.temVersao()){
            buscaNovos();
        } else {
            buscaAlunos();
        }
    }

    public AlunoSincronizador(Context context) {
        this.context = context;
        preferences = new AlunoPreferences(context);
    }

    @NonNull private Callback<AlunoSync> buscaAlunosCallback() {
        return new Callback<AlunoSync>() {
            @Override
            public void onResponse(Call<AlunoSync> call, Response<AlunoSync> response) {
                AlunoSync alunoSync = response.body();
                sincroniza(alunoSync);
//                Log.i("versao", preferences.getVersao());
                bus.post(new AtualizaListaAlunoEvent());
                sincronizaAlunosInternos();
            }

            @Override
            public void onFailure(Call<AlunoSync> call, Throwable t) {
                Log.e("onFailure", t.getMessage());
                bus.post(new AtualizaListaAlunoEvent());
            }
        };
    }

    public void sincroniza(AlunoSync alunoSync) {
        String versao = alunoSync.getMomentoDaUltimaModificacao();
        Log.i("Versao externa", versao);
        if (temVersaoNova(versao)){
            preferences.salvaVersao(versao);
            Log.i("Versao atual", preferences.getVersao());
            AlunoDAO dao = new AlunoDAO(context);
            dao.sincroniza(alunoSync.getAlunos());
            dao.close();
        }
    }

    private boolean temVersaoNova(String versao){
        if (!preferences.temVersao())
            return true;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        try {
            String versaoInterna = preferences.getVersao();
            Log.i("Versao interna", versaoInterna);
            Date dataExterna = format.parse(versao);
            Date dataInterna = format.parse(versaoInterna);
            return dataExterna.after(dataInterna);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
};