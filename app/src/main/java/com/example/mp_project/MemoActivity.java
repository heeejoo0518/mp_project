package com.example.mp_project;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;

/**
 * MemoActivity
 * @author 김희주 이가빈
 */


public class MemoActivity extends AppCompatActivity {
    DBHandler handler;
    Utils utils;

    Button editBtn;
    TextView titleView;
    TextView contentView;
    ImageView imgView;
    ImageView fIcon;
    ImageView tagImg;
    TextView tag;

    byte[] bytearrays;
    int key;
    String date;
    int memCode;

    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    private YouTubePlayerSupportFragmentX youTubePlayerFragment;
    private YouTubePlayer youTubePlayer;

    //youtube key
    String videoId;

    //API Key
    String apiKey = "AIzaSyDuy59FohjySY3Zq1LUDUiMG2yNmCNW5cY";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //뒤로가기 버튼 활성화
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //View 연결
        editBtn = (Button) findViewById(R.id.btnEdit);
        titleView = (TextView)findViewById(R.id.titleView);
        contentView = (TextView)findViewById(R.id.contentView);
        imgView = (ImageView)findViewById(R.id.imageView);
        fIcon = (ImageView)findViewById(R.id.feelIcon);
        tagImg = (ImageView)findViewById(R.id.hashtagImg);
        tag = (TextView)findViewById(R.id.mTag);


        //handler open
        handler = DBHandler.open(this);

        //utils
        utils = new Utils(getApplicationContext());

        //key 값 수신
        Intent intent = getIntent();
        key = intent.getExtras().getInt("key");
        date = intent.getExtras().getString("date");
        memCode = intent.getExtras().getInt("userCode");


        //memo 정보 불러오기
        ContentValues memo =  handler.selectOne(key,memCode);

        //set title
        setTitle(dateformat(date));

        youTubePlayerFragment = (YouTubePlayerSupportFragmentX) getSupportFragmentManager()
                .findFragmentById(R.id.youtubeFragment);

        //set data
        if(key>0){
            titleView.setText(memo.getAsString("MemoTitle"));
            contentView.setText(memo.getAsString("MemoContents"));
            bytearrays = memo.getAsByteArray("Image");
            imgView.setImageBitmap(utils.ByteArraytoBitmap(bytearrays));
            String fullUrl = memo.getAsString("YoutubeUrl");
            // url이 DB에 입력되지 않았을 경우 프래그먼트 hide
            if(fullUrl.equals("")){
                fragmentTransaction.hide(youTubePlayerFragment);
                fragmentTransaction.commit();
            } else {
                //전체 url에서 youtube key 추출
                String[] array = fullUrl.split("/");
                videoId = array[array.length-1];
                //set youtube player
                initializeYoutubePlayer(youTubePlayerFragment);
            }
            //기분 이모티콘이 입력된 경우에만 해당 이미지 표시
            if(!memo.getAsString("MemoFeel").equals("")){
                String fNum = memo.getAsString("MemoFeel").substring(0,2);
                switch (fNum){
                    case "f1" : fIcon.setImageResource(R.drawable.f1_selected);
                        break;
                    case "f2" : fIcon.setImageResource(R.drawable.f2_selected);
                        break;
                    case "f3" : fIcon.setImageResource(R.drawable.f3_selected);
                        break;
                    case "f4" : fIcon.setImageResource(R.drawable.f4_selected);
                        break;
                    case "f5" : fIcon.setImageResource(R.drawable.f5_selected);
                        break;
                }
            }
            //Tag를 입력한 경우에만 태그 표시와 함께 내용 표시
            if(!memo.getAsString("MemoTag").equals("")){
                tagImg.setImageResource(R.drawable.hashtag);
                tag.setText(memo.getAsString("MemoTag"));
            }
        }
    }

    //팝업 메뉴 메소드
    public void mOnClick(View v){
        //팝업메뉴 객체 생성
        PopupMenu popup = new PopupMenu(MemoActivity.this, editBtn);
        //팝업메뉴 xml 파일 inflate
        popup.getMenuInflater().inflate(R.menu.edit_menu, popup.getMenu());

        //팝업 메뉴 클릭시 이벤트
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getTitle().toString()){
                    case "수정":
                        Intent intent = new Intent(MemoActivity.this, EditActivity.class);
                        intent.putExtra("key", key); //key값 전달
                        intent.putExtra("date", date);
                        intent.putExtra("userCode",memCode);
                        startActivity(intent);
                        finish();
                        break;
                    case "삭제":
                        //지우고 메인 화면으로 돌아감
                        handler.delete(key);
                        Intent i = new Intent(MemoActivity.this, MainActivity.class);
                        i.putExtra("userCode",memCode);
                        startActivity(i);
                        finish();
                        break;
                    case "취소":
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    //유튜브 플레이어 메소드
    private void initializeYoutubePlayer(YouTubePlayerSupportFragmentX youTubePlayerFragment) {
//        youTubePlayerFragment = (YouTubePlayerSupportFragmentX) getSupportFragmentManager()
//                .findFragmentById(R.id.youtubeFragment);

        if (youTubePlayerFragment == null)
            return;

        youTubePlayerFragment.initialize(apiKey, new YouTubePlayer.OnInitializedListener() {
            //초기화 성공
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                                boolean wasRestored) {
                if (!wasRestored) {
                    youTubePlayer = player;

                    //set the player style
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    youTubePlayer.loadVideo(videoId);
                    youTubePlayer.play();
                }
            }
            //초기화 실패
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1) {
            }
        });
        fragmentTransaction.show(youTubePlayerFragment);
    }
    //뒤로가기 버튼 메소드
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                Intent i = new Intent(MemoActivity.this, MainActivity.class);
                i.putExtra("userCode",memCode);
                startActivity(i);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //물리적 뒤로가기 버튼 메소드
    @Override
    public void onBackPressed() {
        Intent i = new Intent(MemoActivity.this, MainActivity.class);
        i.putExtra("userCode",memCode);
        startActivity(i);
        finish();
    }

    //날짜 포맷 바꾸는 메소드
    public String dateformat(String date){
        return date.substring(0,4)+"년 "+date.substring(4,6)+"월 "+date.substring(6)+"일";
    }
}
