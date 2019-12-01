package com.qi.voiceinput.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.qi.voiceinput.R;
import com.qi.voiceinput.adapter.DictionaryAdapter;
import com.qi.voiceinput.databinding.ActivityMainBinding;
import com.qi.voiceinput.model.Dictionary;
import com.qi.voiceinput.model.DictionaryResponse;
import com.qi.voiceinput.network.ApiClient;
import com.qi.voiceinput.util.AppConstant;
import com.qi.voiceinput.util.AppUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private ProgressDialog progressDialog;
    private final int RC_SPEECH_INPUT = 100;


    private List<Dictionary> dictionaries = new ArrayList<>();
    private Dictionary dictionary;
    private DictionaryAdapter dictionaryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initUI();
    }

    private void initUI(){
        progressDialog = new ProgressDialog(this);
        binding.fabSpeak.setOnClickListener(this);
        setAdapter();
    }

    private void setAdapter(){
        dictionaryAdapter = new DictionaryAdapter(this,dictionaries);
        binding.rvDictionary.setAdapter(dictionaryAdapter);
        //get dictionary list(word,frequency) from server
        getDictionary();
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "permision not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void inputSpeech(){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");
        try {
            startActivityForResult(intent, RC_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fabSpeak:
//                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                inputSpeech();
                break;
        }
    }

    private void getDictionary(){
        AppUtils.showProgressDialog(progressDialog,getString(R.string.dictionary_progress_title),getString(R.string.dictionary_progress_msg));
        ApiClient.getClient().getDictionary().enqueue(new Callback<DictionaryResponse>() {
            @Override
            public void onResponse(Call<DictionaryResponse> call, Response<DictionaryResponse> response) {
                AppUtils.dismissProgressDialog(progressDialog);
                if (response.code() == AppConstant.STATUS_OK){
                    final DictionaryResponse dictionaryResponse = response.body();
                    if (null!=dictionaryResponse){
                        //set the dictionary list in recycleview in descending order with item appear animation
                        setDictionaryData(dictionaryResponse.getDictionary());
                    }else {
                        //Not getting data from server
                        noDataFound();
                    }
                }else {
                    //Not getting data from server
                    noDataFound();
                }
            }
            @Override
            public void onFailure(Call<DictionaryResponse> call, Throwable t) {
                AppUtils.dismissProgressDialog(progressDialog);
                noDataFound();
            }
        });
    }

    private void loadAnimation(){
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(binding.getRoot().getContext(), R.anim.layout_animation_down_to_up);

        binding.rvDictionary.setLayoutAnimation(controller);
        binding.rvDictionary.getAdapter().notifyDataSetChanged();
        binding.rvDictionary.scheduleLayoutAnimation();
    }

    private void setDictionaryData(List<Dictionary> dictionaries){
        Collections.sort(dictionaries);
        this.dictionaries.addAll(dictionaries);
        //Item appear animation
        loadAnimation();
        dictionaryAdapter.notifyDataSetChanged();
    }

    private void noDataFound(){
        binding.rvDictionary.setVisibility(View.GONE);
        binding.fabSpeak.setVisibility(View.GONE);
        binding.tvNoDataFound.setVisibility(View.VISIBLE);
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case RC_SPEECH_INPUT:
                if (resultCode==RESULT_OK & null!=data){
                    int phraseMatch=0;
                    //get user input result as an ArrayList
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                        //Match the word in dictionary with list(0 position) then increment frequency and then highlight the matching object
                        ListIterator<Dictionary> iterator = dictionaries.listIterator();
                        while (iterator.hasNext()) {
                            dictionary = iterator.next();
                            if (null!=result && result.size()>0) {
                                System.out.println("hjkl"+dictionary.getWord().equalsIgnoreCase(result.get(0)));
                                if (result.get(0).equalsIgnoreCase(dictionary.getWord())) {
                                    dictionary.setFrequency(dictionary.getFrequency() + 1);
                                    dictionary.setHighlightStatus(true);
                                    phraseMatch++;
                                }else {
                                    dictionary.setHighlightStatus(false);
                                }
                            }

                        }
//                    Phrase not matching message
                    if (phraseMatch==0){
                        Toast.makeText(this, getString(R.string.phrase_not_availabe), Toast.LENGTH_SHORT).show();
                    }
                    //After frequency increment sort list again in descending order
                    Collections.sort(this.dictionaries);
                    dictionaryAdapter.notifyDataSetChanged();


                }
                break;
        }
    }
}
