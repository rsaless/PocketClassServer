package br.com.rafael.pocketclass.ui.recyclerview.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.rafael.pocketclass.R;
import br.com.rafael.pocketclass.data.modelo.Aluno;
import br.com.rafael.pocketclass.ui.recyclerview.adapter.listener.OnItemClickListener;

public class AlunosAdapter extends RecyclerView.Adapter<AlunosAdapter.AlunoViewHolder> {

    private List<Aluno> alunos;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public AlunosAdapter(Context context, List<Aluno> alunos){
        this.context = context;
        this.alunos = alunos;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override public AlunosAdapter.AlunoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new AlunoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(AlunosAdapter.AlunoViewHolder holder, int position) {
        Aluno aluno = alunos.get(position);
        holder.vincula(aluno);
    }

    @Override
    public int getItemCount() {
        return alunos.size();
    }

    class AlunoViewHolder extends RecyclerView.ViewHolder{

        private final TextView campoNome;
        private final TextView campoTelefone;
        private final TextView campoEndereco;
        private final TextView campoSite;
        private final ImageView campoFoto;
        private Aluno aluno;

        public AlunoViewHolder(View itemView) {
            super(itemView);

            campoNome = itemView.findViewById(R.id.item_nome);
            campoTelefone = itemView.findViewById(R.id.item_telefone);
            campoEndereco = itemView.findViewById(R.id.item_endereco);
            campoSite = itemView.findViewById(R.id.item_site);
            campoFoto = itemView.findViewById(R.id.item_foto);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(aluno, getAdapterPosition());
                }
            });
        }

        public void vincula(Aluno aluno){
            this.aluno = aluno;
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
        }
    }
}
