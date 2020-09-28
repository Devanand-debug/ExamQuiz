package com.example.examquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ScoresActivity extends AppCompatActivity {

    private TextView score_tv,total;
    private Button done_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        loadAdds();

        score_tv = findViewById(R.id.score_tv);
        total = findViewById(R.id.total);
        done_btn = findViewById(R.id.done_btn);

        score_tv.setText(String.valueOf(getIntent().getIntExtra("score",0)));
        total.setText("OUT OF "+String.valueOf(getIntent().getIntExtra("total",0)));

        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void loadAdds(){
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}