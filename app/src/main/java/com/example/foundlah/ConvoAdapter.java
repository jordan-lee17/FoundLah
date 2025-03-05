package com.example.foundlah;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConvoAdapter extends RecyclerView.Adapter<ConvoAdapter.ConvoViewHolder>{
    private List<Convo> convoList;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(Convo convo);
    }

    public ConvoAdapter(List<Convo> convoList, OnUserClickListener onUserClickListener) {
        this.convoList = convoList;
        this.onUserClickListener = onUserClickListener;
    }

    public ConvoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_convo, parent, false);
        return new ConvoViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ConvoViewHolder holder, int position) {
        Convo convo = convoList.get(position);
        holder.user1.setText(convo.getUser1());
        holder.user2.setText(convo.getUser2());

        Log.d("ConvoAdapter", "Binding" + convo.getUser1() + "&" + convo.getUser2());
        holder.itemView.setOnClickListener(v -> onUserClickListener.onUserClick(convo));
    }

    @Override
    public int getItemCount() {
        return convoList.size();
    }

    public static class ConvoViewHolder extends RecyclerView.ViewHolder {
        TextView user1, user2;

        public ConvoViewHolder(@NonNull View itemView) {
            super(itemView);
            user1 = itemView.findViewById(R.id.user1);
            user2 = itemView.findViewById(R.id.user2);
        }
    }
}
