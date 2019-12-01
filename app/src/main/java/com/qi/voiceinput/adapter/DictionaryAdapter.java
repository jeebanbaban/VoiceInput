package com.qi.voiceinput.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.qi.voiceinput.R;
import com.qi.voiceinput.databinding.ItemPhraseWithFrequencyBinding;
import com.qi.voiceinput.model.Dictionary;

import java.util.List;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.MyViewHolder> {

    private Context context;
    private List<Dictionary> dictionaryList;

    public DictionaryAdapter(Context context, List<Dictionary> dictionaryList) {
        this.context = context;
        this.dictionaryList = dictionaryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new MyViewHolder((ItemPhraseWithFrequencyBinding) DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_phrase_with_frequency,null,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Dictionary dictionary = dictionaryList.get(position);
        holder.binding.setDictionary(dictionary);
//
//        if (highlightPosition==position){
//            holder.binding.llPhrase.setBackgroundColor(Color.GREEN);
//        }else {
//            holder.binding.llPhrase.setBackgroundColor(Color.WHITE);
//        }
    }

    @Override
    public int getItemCount() {
        return dictionaryList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public ItemPhraseWithFrequencyBinding binding;

        public MyViewHolder(ItemPhraseWithFrequencyBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }

}
