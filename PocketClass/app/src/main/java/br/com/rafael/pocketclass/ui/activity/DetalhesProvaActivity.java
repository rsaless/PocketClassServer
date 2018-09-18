package br.com.rafael.pocketclass.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import br.com.rafael.pocketclass.R;
import br.com.rafael.pocketclass.data.modelo.Prova;

public class DetalhesProvaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_prova);

        Intent intent = getIntent();
        Prova prova = (Prova) intent.getSerializableExtra("prova");

        TextView data = (TextView) findViewById(R.id.detalhes_prova_data);
        TextView materia = (TextView) findViewById(R.id.detalhes_prova_materia);
        ListView listaTopicos = (ListView) findViewById(R.id.detalhes_prova_topicos);

        data.setText(prova.getData());
        materia.setText(prova.getMateria());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, prova.getTopicos());
        listaTopicos.setAdapter(adapter);
    }
}
