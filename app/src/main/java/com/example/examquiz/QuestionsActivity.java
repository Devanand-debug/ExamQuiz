package com.example.examquiz;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class QuestionsActivity extends AppCompatActivity {

    public static final String FILE_NAME = "QUIZZER";
    public static final String KEY_NAME = "QUESTIONS";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    Toolbar toolbar;
    private TextView question,no_indicator;
    private FloatingActionButton bookmarks_btn;
    private LinearLayout option_container;
    private Button share,next;
    private  int count=0;
    private List<QuestionModel> list;
    private int position = 0;
    private int score = 0;
    private String category;
    private int setNo;
    private Dialog loadingDialog;
    private int matchedQuestionPosition;

    private List<QuestionModel> bookmarksList;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        loadAdds();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        question = findViewById(R.id.question);
        no_indicator = findViewById(R.id.no_indicator);
        bookmarks_btn = findViewById(R.id.bookmarks_btn);
        option_container = findViewById(R.id.options_container);
        share = findViewById(R.id.share_btn);
        next = findViewById(R.id.next_btn);

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        preferences = getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();

        getBookmarks();
        bookmarks_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (modelMatch()){
                bookmarksList.remove(matchedQuestionPosition);
                bookmarks_btn.setImageDrawable(getDrawable(R.drawable.bookmark_boarder));
            }else{
                bookmarksList.add(list.get(position));
                bookmarks_btn.setImageDrawable(getDrawable(R.drawable.bookmark));
            }
            }
        });

        category = getIntent().getStringExtra("category");
        setNo = getIntent().getIntExtra("setNo", 1);

        list = new ArrayList<>();

        loadingDialog.show();
        myRef.child("SETS").child(category).child("questions").orderByChild("setNo").equalTo(setNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    list.add(snapshot.getValue(QuestionModel.class));
                }
                if (list.size() > 0) {
                    for (int i = 0; i < 4; i++) {
                        option_container.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View view) {
                                checkAnswer((Button) view);
                            }
                        });
                    }
                    playAnimation(question, 0, list.get(position).getQuestion());  //bidefault first question will be select.
                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            next.setEnabled(false);
                            next.setAlpha(0.7f);
                            enableOption(true);
                            position++;
                            if (position == list.size()) {
                                //Score Activity
                                Intent scoreintent = new Intent(QuestionsActivity.this,ScoresActivity.class);
                                scoreintent.putExtra("score",score);
                                scoreintent.putExtra("total",list.size());
                                startActivity(scoreintent);
                                finish();
                                return;
                            }
                            count = 0;
                            playAnimation(question, 0, list.get(position).getQuestion());
                        }
                    });

                    share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String body = list.get(position).getQuestion() + "\n" +
                                    list.get(position).getOptionA() + "\n" +
                                    list.get(position).getOptionB() + "\n" +
                                    list.get(position).getOptionC() + "\n" +
                                    list.get(position).getOptionD();
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Quizzer Challenge");
                            shareIntent.putExtra(Intent.EXTRA_TEXT,body);
                            startActivity(Intent.createChooser(shareIntent,"Share Via"));
                        }
                    });
                }
                else {
                    finish();
                    Toast.makeText(QuestionsActivity.this, "no questions", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(QuestionsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }

    private void playAnimation(final View view, final int value, final String data){

        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if( value == 0 && count < 4 ){
                    String option = "";
                    switch (count){
                        case 0: option = list.get(position).getOptionA(); break;
                        case 1: option = list.get(position).getOptionB(); break;
                        case 2: option = list.get(position).getOptionC(); break;
                        case 3: option = list.get(position).getOptionD(); break;

                    }
                    playAnimation(option_container.getChildAt(count),0,option);  //view is invisible
                    count++;
                }

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(value == 0){
                    try {
                        ((TextView) view).setText(data);
                        no_indicator.setText(position + 1 + "/"+list.size());
                        if (modelMatch()){
                            bookmarks_btn.setImageDrawable(getDrawable(R.drawable.bookmark));
                        }else{
                            bookmarks_btn.setImageDrawable(getDrawable(R.drawable.bookmark_boarder));
                        }

                    }catch (ClassCastException e) {
                        ((Button) view).setText(data);
                    }
                    view.setTag(data);
                    playAnimation(view,1,data);   //now view is visible (data change)
                }

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });


    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkAnswer(Button selectedOption){
        enableOption(false);
        next.setEnabled(true);
        next.setAlpha(1);
        if (selectedOption.getText().toString().equals(list.get(position).getCorrectANS())){
            //Ans Correct
            score++;
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CE613")));
        }
        else{
            // Incorrect answer
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
            Button correctoption = option_container.findViewWithTag(list.get(position).getCorrectANS());
            correctoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CE613")));
        }


    }
    private void enableOption(boolean enable){
        for (int i=0; i<4; i++){
            option_container.getChildAt(i).setEnabled(enable);
            if(enable){
                option_container.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
            }
        }
    }
    private void getBookmarks(){

        String json = preferences.getString(KEY_NAME,"");

        Type type = new TypeToken<List<QuestionModel>>(){}.getType();

        bookmarksList = gson.fromJson(json,type);

        if(bookmarksList == null){
            bookmarksList = new ArrayList<>();
        }
    }

    private  boolean modelMatch(){
        boolean matched = false;
        int i = 0;
        for (QuestionModel model : bookmarksList) {
            if(model.getQuestion().equals(list.get(position).getQuestion())
            && model.getCorrectANS().equals(list.get(position).getCorrectANS())
            && model.getSetNo() == list.get(position).getSetNo()){
                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return matched;
    }

    private void storeBookmarks(){

        String json = gson.toJson(bookmarksList);    //convert gson list to json format.
        editor.putString(KEY_NAME,json);
        editor.commit();
    }

    private void loadAdds(){
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}