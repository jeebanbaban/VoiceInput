package com.qi.voiceinput.util;

import android.app.ProgressDialog;

public class AppUtils {

    public static void showProgressDialog(ProgressDialog progressDialog, String title, String message){
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void showProgressDialog(ProgressDialog progressDialog,String message){
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void dismissProgressDialog(ProgressDialog progressDialog){
        if (progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

}
