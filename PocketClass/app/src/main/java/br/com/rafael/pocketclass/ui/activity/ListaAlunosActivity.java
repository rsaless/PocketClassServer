package br.com.rafael.pocketclass.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import br.com.rafael.pocketclass.R;
import br.com.rafael.pocketclass.data.dao.AlunoDAO;
import br.com.rafael.pocketclass.ui.recyclerview.adapter.AlunosAdapter;
import br.com.rafael.pocketclass.ui.recyclerview.adapter.listener.OnItemClickListener;
import br.com.rafael.pocketclass.ui.recyclerview.adapter.listener.OnItemLongClickListener;
import br.com.rafael.pocketclass.utils.event.AtualizaListaAlunoEvent;
import br.com.rafael.pocketclass.data.modelo.Aluno;
import br.com.rafael.pocketclass.utils.helper.AlunoItemTouchHelperCallback;
import br.com.rafael.pocketclass.utils.sinc.AlunoSincronizador;
import br.com.rafael.pocketclass.utils.task.EnviaAlunosTask;

public class ListaAlunosActivity extends AppCompatActivity {

    public static final int SMS_CODE = 321;
    private final AlunoSincronizador sincronizador = new AlunoSincronizador(this);
    private RecyclerView listaAlunos;
    private SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        EventBus eventBus = EventBus.getDefault();
        eventBus.register(this);

        listaAlunos = findViewById(R.id.lista_alunos);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe_lista_aluno);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sincronizador.buscaTodos();
            }
        });

        Button novoAluno = (Button) findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(intentVaiProFormulario);
            }
        });

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_CODE);
        }

        registerForContextMenu(listaAlunos);
        sincronizador.buscaTodos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregaLista();
    }

    public void carregaLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaAlunos();
        
        for (Aluno aluno: alunos){
            Log.i("aluno sincronizado", String.valueOf(aluno.getSincronizado()));
        }
        
        dao.close();
        configuraRecyclerView(alunos);


    }

    private void configuraRecyclerView(List<Aluno> alunos) {
        //ArrayAdapter<Aluno> adapter = new ArrayAdapter<Aluno>(this, R.layout.list_item, alunos);
        final AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Aluno aluno, int posicao) {
                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                intentVaiProFormulario.putExtra("aluno", aluno);
                startActivity(intentVaiProFormulario);
            }
        });
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Aluno aluno, int posicao) {
                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.deleta(aluno);
                dao.close();
                carregaLista();
                sincronizador.deleta(aluno);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listaAlunos.setLayoutManager(layoutManager);
    }


    /*
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);

            MenuItem itemLigar = menu.add("Ligar");
            itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(ListaAlunosActivity.this, new String[]{Manifest.permission.CALL_PHONE}, CALL_CODE);
                    } else {
                        Intent intentLigar = new Intent(Intent.ACTION_CALL);
                        intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                        startActivity(intentLigar);
                    }
                     return false;
                }
            });

            MenuItem itemSMS = menu.add("Enviar SMS");
            Intent intentSMS = new Intent(Intent.ACTION_VIEW);
            intentSMS.setData(Uri.parse("sms:"+aluno.getTelefone()));
            itemSMS.setIntent(intentSMS);

            MenuItem itemMapa = menu.add("Visualizar no mapa");
            Intent intentMapa = new Intent(Intent.ACTION_VIEW);
            intentMapa.setData(Uri.parse("geo:0,0?q="+aluno.getEndereco()));
            itemMapa.setIntent(intentMapa);

            MenuItem itemSite = menu.add("Visistar site");
            Intent intentSite = new Intent(Intent.ACTION_VIEW);
            String site = aluno.getSite();
            if (!site.startsWith("http://")){
                site = "http://" + site;
            }
            intentSite.setData(Uri.parse(site));
            itemSite.setIntent(intentSite);

            MenuItem deletar = menu.add("Deletar");
            deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                    dao.deleta(aluno);
                    dao.close();
                    carregaLista();
                    sincronizador.deleta(aluno);
                    return false;
                }
            });
        }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_enviar_notas:
                new EnviaAlunosTask(this).execute();
                break;

            case R.id.menu_baixar_provas:
                Intent vaiaraProvas = new Intent(this, ProvasActivity.class);
                startActivity(vaiaraProvas);
                break;
            /*
            case R.id.menu_mapa:
                Intent vaiParaMapa = new Intent(this, MapaActivity.class);
                startActivity(vaiParaMapa);
                break;
                */
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void atualizaListaAlunoEvent(AtualizaListaAlunoEvent event){
        if (swipe.isRefreshing()){
            swipe.setRefreshing(false);
        }
        carregaLista();
    }
}
