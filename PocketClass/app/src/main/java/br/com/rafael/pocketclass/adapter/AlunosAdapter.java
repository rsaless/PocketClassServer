package br.com.rafael.pocketclass.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.rafael.pocketclass.ListaAlunosActivity;
import br.com.rafael.pocketclass.R;
import br.com.rafael.pocketclass.modelo.Aluno;

public class AlunosAdapter extends BaseAdapter{

    private final List<Aluno> alunos;
    private final Context contex;

    public AlunosAdapter(Context context, List<Aluno> alunos) {
        this.contex = context;
        this.alunos = alunos;
    }

    @Override public int getCount() {
        return alunos.size();
    }

    @Override public Object getItem(int position) {
        return alunos.get(position);
    }

    @Override public long getItemId(int position) {
        return alunos.get(position).getId();
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {

        Aluno aluno = alunos.get(position);
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = convertView;

        if (convertView == null) view = inflater.inflate(R.layout.list_item, parent, false);

        TextView campoNome = (TextView) view.findViewById(R.id.item_nome);
        TextView campoTelefone = (TextView) view.findViewById(R.id.item_telefone);
        TextView campoEndereco = (TextView) view.findViewById(R.id.item_endereco);
        TextView campoSite = (TextView) view.findViewById(R.id.item_site);
        ImageView campoFoto = (ImageView) view.findViewById(R.id.item_foto);

        campoNome.setText(aluno.getNome());
        campoTelefone.setText(aluno.getTelefone());
        if (campoEndereco != null) campoEndereco.setText(aluno.getEndereco());
        if (campoSite != null) campoSite.setText(aluno.getSite());
        String caminhoFoto = aluno.getCaminhoFoto();


        if (caminhoFoto != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(caminhoFoto);
            Bitmap bitmapReduzido = bitmap.createScaledBitmap(bitmap, 100, 100, true);
            campoFoto.setImageBitmap(bitmapReduzido);
            campoFoto.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        return view;
    }
}
