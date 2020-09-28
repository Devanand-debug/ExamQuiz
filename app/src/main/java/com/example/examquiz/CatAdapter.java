package com.example.examquiz;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CatAdapter extends RecyclerView.Adapter<CatAdapter.Viewholder> {

    private List<CatModel> catModelList;

    public CatAdapter(List<CatModel> catModels) {
        this.catModelList = catModels;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item,parent,false);

        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        holder.setData(catModelList.get(position).getUrl() ,catModelList.get(position).getName(),catModelList.get(position).getSets());
    }

    @Override
    public int getItemCount() {
        return catModelList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        private CircleImageView imageView;
        private TextView mtitle;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.image_view);
            mtitle=itemView.findViewById(R.id.title);
        }

        private void setData(String url, final String title, final int sets){
            Glide.with(itemView.getContext()).load(url).into(imageView); //set image on image view
            mtitle.setText(title);                                       //set title on mtitle.

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(itemView.getContext(),SetsActivity.class);
                    intent.putExtra("title",title);
                    intent.putExtra("sets",sets);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
