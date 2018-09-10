package br.com.rafael.pocketclass.task;

import android.os.AsyncTask;

import br.com.rafael.pocketclass.WebClient;
import br.com.rafael.pocketclass.converter.AlunoConverter;
import br.com.rafael.pocketclass.modelo.Aluno;

public class InsereAlunoTask extends AsyncTask{
    private final Aluno aluno;

    public InsereAlunoTask(Aluno aluno) {
        this.aluno = aluno;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json = new AlunoConverter().converteParaJSONCompleto(aluno);
        new WebClient().insere(json);
        return null;

    }
}
