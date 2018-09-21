package br.com.rafael.pocketclass.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;

import br.com.rafael.pocketclass.BuildConfig;
import br.com.rafael.pocketclass.utils.helper.FormularioHelper;
import br.com.rafael.pocketclass.R;
import br.com.rafael.pocketclass.data.dao.AlunoDAO;
import br.com.rafael.pocketclass.data.modelo.Aluno;
import br.com.rafael.pocketclass.web.retrofit.RetrofitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormularioActivity extends AppCompatActivity {

    public static final int CODIGO_CAMERA = 567;
    public static final int CALL_CODE = 123;
    private FormularioHelper helper;
    private String caminhoFoto;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        helper = new FormularioHelper(this);

        final Intent intent = getIntent();
        Aluno aluno = (Aluno) intent.getSerializableExtra("aluno");
        if (aluno != null){
            helper.preencheFormulario(aluno);
        }

        Button botaoFoto = (Button) findViewById(R.id.formulario_botao_foto);
        botaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                 caminhoFoto = getExternalFilesDir(null)+ "/" + System.currentTimeMillis() + ".jpg";
                File arquivoFoto = new File(caminhoFoto);
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                        FormularioActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", arquivoFoto));
                startActivityForResult(intentCamera, CODIGO_CAMERA);
            }
        });
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODIGO_CAMERA && resultCode == Activity.RESULT_OK) {
            helper.carregaImagem(caminhoFoto);
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_formulario, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Aluno aluno = helper.pegaAluno();
        switch (item.getItemId()){
            case R.id.menu_formulario_ok:
                aluno.desincroniza();
                AlunoDAO dao = new AlunoDAO(this);

                if (aluno.getId() != null){
                    dao.altera(aluno);
                } else {
                    dao.insere(aluno);
                }
                dao.close();

                //new InsereAlunoTask(aluno).execute();
                Call call = new RetrofitInicializador().getAlunoService().insere(aluno);
                call.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) {}

                    @Override
                    public void onFailure(Call call, Throwable t) {}
                });

                Toast.makeText(FormularioActivity.this, "Aluno "+ aluno.getNome() + " salvo!", Toast.LENGTH_SHORT).show();
                finish();
                break;

            case R.id.menu_formulario_ligar:
                if(ActivityCompat.checkSelfPermission(FormularioActivity.this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(FormularioActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, CALL_CODE);
                } else {
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                    startActivity(intentLigar);
                }
                break;

            case R.id.menu_formulario_sms:
                Intent intentSMS = new Intent(Intent.ACTION_VIEW);
                intentSMS.setData(Uri.parse("sms:"+aluno.getTelefone()));
                startActivity(intentSMS);
                break;

            case R.id.menu_formulario_mapa:
                Intent intentMapa = new Intent(Intent.ACTION_VIEW);
                intentMapa.setData(Uri.parse("geo:0,0?q="+aluno.getEndereco()));
                startActivity(intentMapa);
                break;

            case R.id.menu_formulario_site:
                Intent intentSite = new Intent(Intent.ACTION_VIEW);
                String site = aluno.getSite();
                if (!site.startsWith("http://")){
                    site = "http://" + site;
                }
                intentSite.setData(Uri.parse(site));
                startActivity(intentSite);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
