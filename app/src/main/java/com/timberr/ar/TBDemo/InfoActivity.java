package com.timberr.ar.TBDemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.timberr.ar.TBDemo.Utils.DataHelper;

import java.util.Locale;

public class InfoActivity extends AppCompatActivity {
    private Button back;
    private DataHelper dataHelper;
    private Button lang_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        dataHelper= new DataHelper(this);
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        lang_btn=findViewById(R.id.lang_btn);
        if (dataHelper.getLanguage()==1)
            lang_btn.setBackgroundResource(R.drawable.en);
        else
            lang_btn.setBackgroundResource(R.drawable.de);
        lang_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLangBtnClicked(view);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    public void onLangBtnClicked(View view) {

        dataHelper.toggleLanguage();
        if (dataHelper.getLanguage()==1) {
            setLocale("de");
            lang_btn.setBackgroundResource(R.drawable.en);
        }
        else {
            setLocale("en");
            lang_btn.setBackgroundResource(R.drawable.de);
        }
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        recreate();
    }

    public void onHeinsbergerClicked(View view) {
        String url = "https://heinsberger-land.de/erleben/radfahren/tim-berresheims-bilderreise/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void onBlitzClicked(View view) {
        String url = "https://timberresheim.de";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void onSNAClicked(View view) {
        String url = "https://studiosnewamerika.com";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void onNRWClicked(View view) {
        String url = "https://www.nrw-tourismus.de";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}