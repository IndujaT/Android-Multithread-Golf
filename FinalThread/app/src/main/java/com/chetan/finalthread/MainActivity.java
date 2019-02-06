package com.chetan.finalthread;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public int WinHoleID, WinIDGrp;
    public int isel = 0, P1CurrentID = 0, P2CurrentID = 0;
    Button mButton;
    public TextView mTextView;
    RadioGroup rgp1,rgp2, rgp3,rgp4,rgp5;
    RadioButton rb;
    public Thread p1 = null;
    public Thread p2 = null;
    public Handler h1;
    public Handler h2;
    ArrayList<Integer> grpid1 = new ArrayList<Integer>(Arrays.asList(1001,1002,1003,1004,1005,1006,1007,1008,1009,1010));
    ArrayList<Integer> grpid2 = new ArrayList<Integer>(Arrays.asList(1011,1012,1013,1014,1015,1016,1017,1018,1019,1020));
    ArrayList<Integer> grpid3 = new ArrayList<Integer>(Arrays.asList(1021,1022,1023,1024,1025,1026,1027,1028,1029,1030));
    ArrayList<Integer> grpid4 = new ArrayList<Integer>(Arrays.asList(1031,1032,1033,1034,1035,1036,1037,1038,1039,1040));
    ArrayList<Integer> grpid5 = new ArrayList<Integer>(Arrays.asList(1041,1042,1043,1044,1045,1046,1047,1048,1049,1015));
    ArrayList<ArrayList<Integer>> holeIdList = new ArrayList<ArrayList<Integer>>(Arrays.asList(grpid1,grpid2, grpid3, grpid4, grpid5));
    public boolean gameStarted = false;
    int[] selectarray = new int[]{CLOSE_GROUP, SAME_GROUP, TARGET_SHOT};
    public static final int NEW_GAME = 0;
    public static final int P1_UPDATE = 1;
    public static final int P2_UPDATE = 2;
    public static final int NEXT_MOVE = 3;
    public static final int GAME_ENDED = 4;
    public static final int RANDOM_SHOT = 6;
    public static final int CLOSE_GROUP = 7;
    public static final int SAME_GROUP = 8;
    public static final int TARGET_SHOT = 9;
    public static final int JACKPOT = 10;
    public static final int NEARMISS = 11;
    public static final int NEARGROUP = 12;
    public static final int BIGMISS = 13;
    public static final int CATASTROPHE = 14;
    Handler mHandler = new Handler(){
        private Message m;
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // sends a message to thread 1 to go ahead and put down a piece
                case NEW_GAME:
                    h1.sendMessage(msg);
                    break;

                // means an update was received from thread 1. updates the board and
                // notifies thread 2 to make a move
                // also checks if game is finished.
                case P1_UPDATE:
                    Log.i("MAIN","P1 update");
                    mTextView.setText("Player1 - "+getStatusString(msg.arg2));
                    rb = (RadioButton)findViewById(msg.arg1);
                    rb.setEnabled(true);
                    rb.setChecked(true);
                    rb.setTextColor(getResources().getColor(R.color.P1color));
                    if(msg.arg2 == CATASTROPHE){
                        Button mainbtn = (Button)findViewById(R.id.start_button);
                        mainbtn.setText("P2 wins");
                        m = h1.obtainMessage(GAME_ENDED);
                        m.what = GAME_ENDED;
                        h2.sendMessage(m);
                        resetThreads();
                        break;
                    }
                    if(msg.arg1 == WinHoleID){
                        Toast.makeText(MainActivity.this, "GAME OVER P1 wins!",
                                Toast.LENGTH_LONG).show();
                        //Button mainbtn = (Button)findViewById(R.id.start_button);
                        mTextView.setText("P1 wins");
                        m = h1.obtainMessage(GAME_ENDED);
                        m.what = GAME_ENDED;
                        h2.sendMessage(m);
                        resetThreads();
                        break;
                    }
                    m = h1.obtainMessage(NEXT_MOVE);
                    m.what = NEXT_MOVE;
                    h2.sendMessage(m);
                    break;

                // means an update was received from thread 1. updates the board and
                // notifies thread 2 to make a move
                // also checks if the game is finished.
                case P2_UPDATE:
                    Log.i("MAIN","P2 update");
                    mTextView.setText("Player2 - "+getStatusString(msg.arg2));
                    rb = (RadioButton)findViewById(msg.arg1);
                    rb.setEnabled(true);
                    rb.setChecked(true);
                    rb.setTextColor(getResources().getColor(R.color.P2color));
                    if(msg.arg2 == CATASTROPHE){
                        Button mainbtn = (Button)findViewById(R.id.start_button);
                        mainbtn.setText("P1 wins");
                        m = h2.obtainMessage(GAME_ENDED);
                        m.what = GAME_ENDED;
                        h1.sendMessage(m);
                        resetThreads();
                        break;
                    }
                    if(msg.arg1 == WinHoleID){
                        Toast.makeText(MainActivity.this, "GAME OVER P2 wins!",
                                Toast.LENGTH_LONG).show();
                        //Button mainbtn = (Button)findViewById(R.id.start_button);
                        mTextView.setText("P2 wins");
                        m = h2.obtainMessage(GAME_ENDED);
                        m.what = GAME_ENDED;
                        h1.sendMessage(m);
                        resetThreads();
                        break;
                    }
                    m = h2.obtainMessage(NEXT_MOVE);
                    m.what = NEXT_MOVE;
                    h1.sendMessage(m);
                    break;

                default:
                    break;
            }
        }
    };

    public class p1Runnable implements Runnable  {
        private boolean firstMoveMade = false;
        private int movesMade, prevID = 0;

        public void run() {

            // Starts the looper and the handler for thread 1
            Looper.prepare();
            h1 = new Handler() {
                public void handleMessage(Message msg) {
                    // sleep for 1 second so it's detectable by a human
                    try {
                        Log.i("MAIN","Before sleep layout");
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Log.i("HARSH ", "INTERRUPTED !");
                        return;
                    }

                    // less than 3 moves... keep putting down pieces at random position
                    if (msg.what == NEXT_MOVE ) {
                        Log.i("MAIN","next move P1");
                        Message m = new Message();
                        //m.arg1 = new Random().nextInt(50);
                        //m.arg1 = shots(RANDOM_SHOT, 0);
                        P1CurrentID = shots(selStratergyP1(), prevID);
                        m.arg2 = statusMsg(P1CurrentID);
                        m.arg1 = P1CurrentID;
                        m.what = P1_UPDATE;
                        prevID = m.arg1;
                        mHandler.sendMessage(m);

                    }
                    else if (msg.what == GAME_ENDED) {
                        resetThreads();
                    }
                }
            };

            // The following code starts off the game.
            // player 1 is always the first to start
            if (!firstMoveMade) {
                firstMoveMade = true;
                Message m = new Message();
                m.what = P1_UPDATE;
                // posts a runnable to the handler of the main ui thread
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText("Set in first move Wins!");
                        Message m = h1.obtainMessage(NEXT_MOVE);
                        //m.arg1 = new Random().nextInt(50);
                        P1CurrentID = shots(RANDOM_SHOT, 0);
                        m.arg1 = P1CurrentID;
                        m.arg2 = statusMsg(P1CurrentID);
                        m.what = NEXT_MOVE;
                        prevID = m.arg1;
                        h2.sendMessage(m);
                    }
                });
            }
            Looper.loop();
        }
    }

    public class p2Runnable implements Runnable {
        private int movesMade;
        int prevID = 0;
        public void run() {

            // Starts the looper and the handler for thread 1
            Looper.prepare();
            h2 = new Handler() {
                public void handleMessage(Message msg) {
                    // sleep for 1 second so it's detectable by a human
                    try {
                        Log.i("MAIN","Before sleep p2");
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Log.i("HARSH ", "INTERRUPTED !");
                        return;
                    }

                    // less than 3 moves. keep putting pieces on the board randomly
                    if (msg.what == NEXT_MOVE) {

                        Log.i("MAIN","next move P2 layout");
                        Message m = new Message();
                        //m.arg1 = new Random().nextInt(50);
                        //m.arg1 = shots(RANDOM_SHOT, 0);
                        P2CurrentID = shots(selStratergyP2(), prevID);
                        m.arg2 = statusMsg(P2CurrentID);
                        m.arg1 = P2CurrentID;
                        m.what = P2_UPDATE;
                        prevID = m.arg1;
                        mHandler.sendMessage(m);

                    }
                    else if (msg.what == GAME_ENDED) {
                        resetThreads();
                    }
                }
            };

            Message m = new Message();
            m.what = 999;
            mHandler.sendMessage(m);

            Looper.loop();
        }
    }

    public int shots(int shotType, int prevID){
        Log.i("MAIN","IN SHOTS");
        int calcValue = 1001;
        switch (shotType){
            case RANDOM_SHOT:
                calcValue = holeIdList.get(new Random().nextInt(5)).get(new Random().nextInt(10));
                break;
            case SAME_GROUP:
                for(int i=0;i<5;i++){
                    if(holeIdList.get(i).contains(prevID))
                        if(i == 4)
                            calcValue = holeIdList.get(i-new Random().nextInt(2)).get(new Random().nextInt(10));
                        else
                            calcValue = holeIdList.get(i+new Random().nextInt(2)).get(new Random().nextInt(10));
                }
                break;
            case CLOSE_GROUP:
                for(int i=0;i<5;i++){
                    if(holeIdList.get(i).contains(prevID))
                        calcValue = holeIdList.get(i).get(new Random().nextInt(10));
                }
                break;
            case TARGET_SHOT:
                for(int i=0;i<5;i++){
                    if(holeIdList.get(i).contains(prevID))
                        if(i == 4)
                            calcValue = prevID - new Random().nextInt(5);
                        else
                            calcValue = prevID + new Random().nextInt(5);
                }
                break;
            default:
                break;
        }
        Log.i("MAIN","calcualted value "+calcValue);
        return calcValue;
    }

    public int selStratergyP1(){
        //int[] arr1 = new int[]{CLOSE_GROUP, SAME_GROUP, TARGET_SHOT};
        Log.i("MAIN","sel stratergy before "+isel);

        if(isel < selectarray.length - 1)
            isel +=1;
        else
            isel = 0;
        Log.i("MAIN","sel stratergy after "+isel);
        return selectarray[isel];
    }

    public int selStratergyP2(){
        int[] arr1 = new int[]{CLOSE_GROUP, SAME_GROUP, TARGET_SHOT};
        return arr1[new Random().nextInt(arr1.length)];
    }

    public int statusMsg(int currentID){
        int status = 0;
        if(currentID == WinHoleID)
            status = JACKPOT;
        else if(P1CurrentID == P2CurrentID)
            status = CATASTROPHE;
        else {
            for (int i = 0; i < 5; i++) {
                if (holeIdList.get(i).contains(currentID)) {
                    if (i == WinIDGrp)
                        status = NEARMISS;
                    else if (Math.abs(i - WinIDGrp) == 1)
                        status = NEARGROUP;
                    else if (Math.abs(i-WinIDGrp)>1)
                        status = BIGMISS;
                }
            }
        }
        return status;
    }
    public String getStatusString(int id){
        String str = "";
            switch (id){
                case JACKPOT:
                    str = "Jackpot" ;
                    break;
                case NEARMISS:
                    str = "Near Miss";
                    break;
                case NEARGROUP :
                    str = "Near Group";
                    break;
                case BIGMISS:
                    str = "Big Miss";
                    break;
                case CATASTROPHE:
                    str = "Catastrophe";
                    break;
                default:
                    break;
            }
        return str;
    }
    public void resetThreads() {
        h1.removeCallbacksAndMessages(null);
        h2.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);

        if (p1.isAlive() && p2.isAlive()) {
            p1.interrupt();
            p2.interrupt();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.start_button);
        mTextView = (TextView)findViewById(R.id.textview1);
        rgp1 = (RadioGroup) findViewById(R.id.radio_group1);
        rgp2 = (RadioGroup) findViewById(R.id.radio_group2);
        rgp3 = (RadioGroup) findViewById(R.id.radio_group3);
        rgp4 = (RadioGroup) findViewById(R.id.radio_group4);
        rgp5 = (RadioGroup) findViewById(R.id.radio_group5);

        Log.i("MAIN","IN ON CREATE");
    }

    public void add_holes(RadioGroup rgp, int id){
        rgp.setOrientation(LinearLayout.VERTICAL);

        for (int i = 1; i <= 10; i++) {
            RadioButton rbn = new RadioButton(this);
            rbn.setId(i + id);
            String id_str = "HOLE " + String.valueOf(i + id - 1000);
            rbn.setText(id_str);
            //rbn.setChecked(true);
            rbn.setEnabled(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.weight = 1.0f;
            rbn.setLayoutParams(params);
            rgp.addView(rbn);
        }
    }

    public void start_click(View v){
        mButton.setText("RESTART");
        Log.i("MAIN","start layout");
        if (!gameStarted) {
            gameStarted = true;
            add_holes(rgp1,1000);
            add_holes(rgp2,1010);
            add_holes(rgp3,1020);
            add_holes(rgp4,1030);
            add_holes(rgp5,1040);
            WinHoleID = holeIdList.get(new Random().nextInt(5)).get(new Random().nextInt(10));
            for(int i=0;i<5;i++)
                if(holeIdList.get(i).contains(WinHoleID))
                    WinIDGrp = i;
            RadioButton rb1 = (RadioButton) findViewById(WinHoleID);
            rb1.setEnabled(true);
            rb1.setChecked(true);
            rb1.setText("Winning Hole");
            rb1.setTextColor(getResources().getColor(R.color.golfcourse));
            rb1.setHighlightColor(getResources().getColor(R.color.golfcourse));
            p1 = new Thread(new p1Runnable());
            p2 = new Thread(new p2Runnable());
            p1.start();
            p2.start();
        }

        // if button is pressed again, reset everything, start over
        else {
            Log.i("MAIN","reset layout");
            resetThreads();
            rgp1.clearCheck();
            rgp1.removeAllViews();
            rgp2.clearCheck();
            rgp2.removeAllViews();
            rgp3.clearCheck();
            rgp3.removeAllViews();
            rgp4.clearCheck();
            rgp4.removeAllViews();
            rgp5.clearCheck();
            rgp5.removeAllViews();
            Toast.makeText(MainActivity.this, "NEW GAME",
                    Toast.LENGTH_LONG).show();
            gameStarted = false;
            mButton.setText("Start Game");
            mTextView.setText("Start Ga,e");
        }
    }
}
