package br.com.rafael.pocketclass.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import br.com.rafael.pocketclass.data.modelo.Aluno;

public class AlunoDAO extends SQLiteOpenHelper{

    public AlunoDAO(Context context) {
        super(context, "PocketClass", null, 6);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Alunos(" +
                "id CHAR(36) PRIMARY KEY, " +
                "nome TEXT NOT NULL, " +
                "endereco TEXT, " +
                "telefone TEXT, " +
                "site TEXT, " +
                "nota REAL, " +
                "caminhoFoto TEXT, " +
                "sincronizado INT DEFAULT 0," +
                "desativado INT DEFAULT 0" +
                ");";
        db.execSQL(sql);
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "";
        switch (oldVersion){
            case 1:
                sql = "ALTER TABLE Alunos ADD COLUMN caminhoFoto TEXT;"; db.execSQL(sql);
            case 2:
                db.beginTransaction();
                try {
                    sql = "CREATE TABLE Alunos_novo(id CHAR(36) PRIMARY KEY, nome TEXT NOT NULL, endereco TEXT, telefone TEXT, site TEXT, nota REAL, caminhoFoto TEXT);"; db.execSQL(sql);
                    sql = "INSERT INTO Alunos_novo(id, nome, endereco, telefone, site, nota, caminhoFoto) "+"SELECT id, nome, endereco, telefone, site, nota, caminhoFoto FROM Alunos;"; db.execSQL(sql);
                    sql = "DROP TABLE Alunos;"; db.execSQL(sql);
                    sql = "ALTER TABLE Alunos_novo RENAME TO Alunos;"; db.execSQL(sql);
                    db.setTransactionSuccessful();
                }catch (SQLException e){}
                finally {
                    db.endTransaction();
                }
            case 3:
                sql = "SELECT * FROM Alunos";
                Cursor cursor = db.rawQuery(sql, null);
                List<Aluno> alunos = populaAlunos(cursor);
                String atualizaAluno = "UPDATE Alunos SET id=? WHERE id=?";
                for (Aluno aluno: alunos){
                    db.execSQL(atualizaAluno,new String[] {geraUUID(), aluno.getId()});
                }
            case 4:
                sql = "ALTER TABLE Alunos ADD COLUMN sincronizado INT DEFAULT 0;"; db.execSQL(sql);
            case 5:
                sql = "ALTER TABLE Alunos ADD COLUMN desativado INT DEFAULT 0;"; db.execSQL(sql);
        }
    }

    @NonNull private ContentValues pegaDadosDoAluno(Aluno aluno) {
        ContentValues dados = new ContentValues();
        dados.put("id", aluno.getId());
        dados.put("nome", aluno.getNome());
        dados.put("endereco", aluno.getEndereco());
        dados.put("telefone", aluno.getTelefone());
        dados.put("site", aluno.getSite());
        dados.put("nota", aluno.getNota());
        dados.put("caminhoFoto", aluno.getCaminhoFoto());
        dados.put("sincronizado", aluno.getSincronizado());
        dados.put("desativado", aluno.getDesativado());
        return dados;
    }
    @NonNull private List<Aluno> populaAlunos(Cursor c) {
        List<Aluno> alunos = new ArrayList<Aluno>();
        while (c.moveToNext()){
            Aluno aluno = new Aluno();
            aluno.setId(c.getString(c.getColumnIndex("id")));
            aluno.setNome(c.getString(c.getColumnIndex("nome")));
            aluno.setEndereco(c.getString(c.getColumnIndex("endereco")));
            aluno.setTelefone(c.getString(c.getColumnIndex("telefone")));
            aluno.setSite(c.getString(c.getColumnIndex("site")));
            aluno.setNota(c.getDouble(c.getColumnIndex("nota")));
            aluno.setCaminhoFoto(c.getString(c.getColumnIndex("caminhoFoto")));
            aluno.setSincronizado(c.getInt(c.getColumnIndex("sincronizado")));
            aluno.setDesativado(c.getInt(c.getColumnIndex("desativado")));
            alunos.add(aluno);
        }
        return alunos;
    }

    private boolean existe(Aluno aluno) {
        SQLiteDatabase db = getReadableDatabase();
        String existe = "SELECT id FROM Alunos WHERE id=? LIMIT 1;";
        Cursor cursor = db.rawQuery(existe, new String[]{aluno.getId()});
        int quantidade = cursor.getCount();
        return quantidade > 0;
    }
    public boolean ehAluno(String telefone){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Alunos WHERE telefone = ?", new String[]{telefone});
        int resultados = c.getCount();
        c.close();
        return resultados > 0;
    }

    public void sincroniza(List<Aluno> alunos) {
        for (Aluno aluno: alunos){
            aluno.sincroniza();
            if (existe(aluno)){
                if (aluno.estaDesativado()){
                    deleta(aluno);
                } else {
                    altera(aluno);
                }
            }else if (!aluno.estaDesativado()){
                insere(aluno);
            }
        }
    }
    public void altera(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = pegaDadosDoAluno(aluno);
        String[] params = {aluno.getId().toString()};
        db.update("Alunos", dados, "id = ?", params );
    }
    public void deleta(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        String[] params = {aluno.getId().toString()};
        if (aluno.estaDesativado()){
            db.delete("Alunos", "id = ?", params );
        } else {
            aluno.desativa();
            altera(aluno);
        }
    }
    public void insere(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        if (aluno.getId() == null) aluno.setId(geraUUID());
        ContentValues dados = pegaDadosDoAluno(aluno);
        db.insert("Alunos", null, dados);
    }

    public List<Aluno> buscaAlunos() {
        String sql = "SELECT * FROM Alunos WHERE desativado = 0;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        List<Aluno> alunos = populaAlunos(c);
        c.close();

        return alunos;
    }
    public List<Aluno> listaNaoSincronizados(){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM Alunos WHERE sincronizado = 0;";
        Cursor cursor = db.rawQuery(sql, null);
        return populaAlunos(cursor);
    }

    private String geraUUID() {
        return UUID.randomUUID().toString();
    }

}
