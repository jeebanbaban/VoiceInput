package com.qi.voiceinput.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.qi.voiceinput.R;
import com.qi.voiceinput.adapter.DictionaryAdapter;
import com.qi.voiceinput.databinding.ActivityMainBinding;
import com.qi.voiceinput.databinding.DialogListenBinding;
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

    private final int RC_SPEECH_INPUT = 100;
    private static final int MY_PERMISSIONS_RECORD_AUDIO = 101;
    private ActivityMainBinding binding;
    private Dialog dialog;
    private ProgressDialog progressDialog;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
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
        setListData();
        initSpeechRecognizer();
    }

    private void grantPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
            }else {
                openListenPopUpDialog();
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }
        }
    }

    private void openListenPopUpDialog(){
        dialog = new Dialog(this);
        DialogListenBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this),R.layout.dialog_listen,null,false);
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);
        dialog.show();
    }

    private void setListData(){
        dictionaryAdapter = new DictionaryAdapter(this,dictionaries);
        binding.rvDictionary.setAdapter(dictionaryAdapter);
        //get dictionary list(word,frequency) from server
        getDictionary();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initSpeechRecognizer();
                }else {
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fabSpeak:
                grantPermission();
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

    private void initSpeechRecognizer(){
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
                dialog.dismiss();
            }

            @Override
            public void onError(int i) {
                dialog.dismiss();

                switch (i) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        Toast.makeText(MainActivity.this, R.string.error_audio, Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        Toast.makeText(MainActivity.this, R.string.error_client, Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        Toast.makeText(MainActivity.this, R.string.error_permission, Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        Toast.makeText(MainActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        Toast.makeText(MainActivity.this, R.string.error_ntwrk_timeout, Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        Toast.makeText(MainActivity.this, R.string.error_no_match, Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        Toast.makeText(MainActivity.this, R.string.error_recognizer_busy, Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        Toast.makeText(MainActivity.this, R.string.error_server, Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        Toast.makeText(MainActivity.this, R.string.error_speech_timeout, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, R.string.try_again, Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                int phraseMatch=0;
                //Match the word in dictionary with list(0 position) then increment frequency and then highlight the matching object
                ListIterator<Dictionary> iterator = dictionaries.listIterator();
                while (iterator.hasNext()) {
                    dictionary = iterator.next();
                    if (null!=result && result.size()>0) {
                        if (result.get(0).equalsIgnoreCase(dictionary.getWord())) {
                            dictionary.setFrequency(dictionary.getFrequency() + 1);
                            dictionary.setHighlightStatus(true);
                            phraseMatch++;
                        }else {
                            dictionary.setHighlightStatus(false);
                        }
                    }
                }
                //Phrase not matching message
                if (phraseMatch==0){
                    Toast.makeText(MainActivity.this, R.string.phrase_not_available, Toast.LENGTH_SHORT).show();
                }
                //After frequency increment sort list again in descending order
                Collections.sort(dictionaries);
                dictionaryAdapter.notifyDataSetChanged();

            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }
}
