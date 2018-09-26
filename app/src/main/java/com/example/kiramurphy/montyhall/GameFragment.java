package com.example.kiramurphy.montyhall;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.Image;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameFragment extends Fragment {
    private TextView iv_prompt = null;
    private TextView iv_WinsAfterSwitch = null;
    private TextView iv_LossesAfterSwitch = null;
    private TextView iv_WinsAfterStay = null;
    private TextView iv_LossesAfterStay = null;
    private TextView iv_TotalWins = null;
    private TextView iv_TotalLosses = null;
    private ImageButton ib_door1 = null;
    private ImageButton ib_door2 = null;
    private ImageButton ib_door3 = null;
    private Button yes_button = null;
    private Button no_button = null;
    private Button playAgain_button = null;
    private String behindDoor1 = null;
    private String behindDoor2 = null;
    private String behindDoor3 = null;
    private Integer doorChosen = 0;
    private Integer doorRevealed = 0;
    private Integer numberOfWinsAfterSwitch = 0;
    private Integer numberOfWinsAfterStay = 0;
    private Integer numberOfLossesAfterSwitch = 0;
    private Integer numberOfLossesAfterStay = 0;
    private Integer totalWins = 0;
    private Integer totalLosses = 0;
    private Boolean winStatus;
    private Boolean doorSwitched;
    private Boolean winnerChecked = false;
    private Timer t = null;
    private Counter ctr = null;
    private Animation startAnimation = null;
    private AudioAttributes audioAttributes = null;
    private SoundPool soundPool = null;
    private Integer goatSound = 0;
    private Integer carSound = 0;




    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create a view for the fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);


        // Define the prompts and the yes and no buttons
        this.iv_prompt = view.findViewById(R.id.prompt);
        this.iv_LossesAfterStay = view.findViewById(R.id.Losses_After_Stay);
        this.iv_WinsAfterStay = view.findViewById(R.id.Wins_After_Stay);
        this.iv_LossesAfterSwitch = view.findViewById(R.id.Losses_After_Switch);
        this.iv_WinsAfterSwitch = view.findViewById(R.id.Wins_After_Switch);
        this.iv_TotalWins = view.findViewById(R.id.Total_Wins);
        this.iv_TotalLosses = view.findViewById(R.id.Total_Losses);
        this.yes_button = view.findViewById(R.id.yes_button);
        this.no_button = view.findViewById(R.id.no_button);
        this.playAgain_button = view.findViewById(R.id.play_again);

        // Define the image buttons for each door
        this.ib_door1 = view.findViewById(R.id.door1);
        this.ib_door2 = view.findViewById(R.id.door2);
        this.ib_door3 = view.findViewById(R.id.door3);

        // Define the Audio Attributes, Sound Pool, and animations
        startAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_in);
        audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        goatSound = soundPool.load(getContext(),R.raw.goat,1);
        carSound = soundPool.load(getContext(), R.raw.car,1);

        SharedPreferences preferences = getActivity().getSharedPreferences("MontyHall",Context.MODE_PRIVATE);
        if(preferences == null){
            numberOfWinsAfterStay = 0;
            numberOfWinsAfterSwitch = 0;
            numberOfLossesAfterStay = 0;
            numberOfLossesAfterSwitch = 0;
            totalLosses = 0;
            totalWins = 0;
        }else {
            numberOfWinsAfterStay = preferences.getInt("winsAfterStay",0);
            numberOfWinsAfterSwitch = preferences.getInt("winsAfterSwitch",0);
            numberOfLossesAfterStay = preferences.getInt("lossesAfterStay",0);
            numberOfLossesAfterSwitch = preferences.getInt("lossesAfterSwitch",0);
            totalLosses = preferences.getInt("totalLosses",0);
            totalWins = preferences.getInt("totalWins",0);
        }

        iv_WinsAfterSwitch.setText(Integer.toString(numberOfWinsAfterSwitch));
        iv_WinsAfterStay.setText(Integer.toString(numberOfWinsAfterStay));
        iv_LossesAfterSwitch.setText(Integer.toString(numberOfLossesAfterSwitch));
        iv_LossesAfterStay.setText(Integer.toString(numberOfLossesAfterStay));
        iv_TotalLosses.setText(Integer.toString(totalLosses));
        iv_TotalWins.setText(Integer.toString(totalWins));

        if(preferences.getBoolean("NewClicked",false)){
            setDoors();
            preferences.edit().putBoolean("NewClicked",false).apply();
        }else{
            behindDoor1 = preferences.getString("behindDoor1",null);
            behindDoor2 = preferences.getString("behindDoor2",null);
            behindDoor3 = preferences.getString("behindDoor3",null);
            doorChosen = preferences.getInt("doorChosen",0);
            doorRevealed = preferences.getInt("doorRevealed",0);
            winnerChecked = preferences.getBoolean("winnerChecked",false);
            winStatus = preferences.getBoolean("winStatus",false);
            doorSwitched = preferences.getBoolean("doorSwitched",false);
            if(winnerChecked && doorRevealed>0){
                ib_door1.setEnabled(false);
                ib_door2.setEnabled(false);
                ib_door3.setEnabled(false);
                ImageButton ib_doorRevealed = getIb_door(doorRevealed);
                ib_doorRevealed.setImageLevel(3);
                checkIfWinner();
                no_button.setVisibility(View.VISIBLE);
                yes_button.setVisibility(View.VISIBLE);
                playAgain_button.setVisibility(View.VISIBLE);
            }else if(!winnerChecked && doorRevealed>0){
                ib_door1.setEnabled(false);
                ib_door2.setEnabled(false);
                ib_door3.setEnabled(false);
                ImageButton ib_doorChosen = getIb_door(doorChosen);
                ImageButton ib_doorRevealed = getIb_door(doorRevealed);
                ib_doorChosen.setImageLevel(1);
                ib_doorRevealed.setImageLevel(3);
                iv_prompt.setText(R.string.change_door_prompt);
                no_button.setVisibility(View.VISIBLE);
                yes_button.setVisibility(View.VISIBLE);
            }else{
                setDoors();
            }
        }


        this.ib_door1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_door1.setImageLevel(1);
                doorChosen = 1;
                showGoat();
                ib_door1.setEnabled(false);
                ib_door2.setEnabled(false);
                ib_door3.setEnabled(false);

            }
        });

        this.ib_door2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_door2.setImageLevel(1);
                doorChosen = 2;
                showGoat();
                ib_door1.setEnabled(false);
                ib_door2.setEnabled(false);
                ib_door3.setEnabled(false);
            }
        });

        this.ib_door3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_door3.setImageLevel(1);
                doorChosen = 3;
                showGoat();
                ib_door1.setEnabled(false);
                ib_door2.setEnabled(false);
                ib_door3.setEnabled(false);
            }
        });

        this.yes_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (doorChosen) {
                    case 1:
                        if (doorRevealed == 2) {
                            ib_door3.setImageLevel(1);
                            ib_door1.setImageLevel(0);
                            doorChosen = 3;
                        }
                        if (doorRevealed == 3) {
                            ib_door2.setImageLevel(1);
                            ib_door1.setImageLevel(0);
                            doorChosen = 2;
                        }
                        break;

                    case 2:
                        if (doorRevealed == 1) {
                            ib_door3.setImageLevel(1);
                            ib_door2.setImageLevel(0);
                            doorChosen = 3;

                        }
                        if (doorRevealed == 3) {
                            ib_door1.setImageLevel(1);
                            ib_door2.setImageLevel(0);
                            doorChosen = 1;
                        }
                        break;

                    case 3:
                        if (doorRevealed == 1) {
                            ib_door2.setImageLevel(1);
                            ib_door3.setImageLevel(0);
                            doorChosen = 2;
                        }
                        if (doorRevealed == 2) {
                            ib_door1.setImageLevel(1);
                            ib_door3.setImageLevel(0);
                            doorChosen = 1;
                        }
                }
                doorSwitched = true;
                winnerChecked = true;
                t = new Timer();
                ctr = new Counter();
                t.scheduleAtFixedRate(ctr, 0, 1000);

            }
        });

        this.no_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                doorSwitched = false;
                winnerChecked = true;
                t = new Timer();
                ctr = new Counter();
                t.scheduleAtFixedRate(ctr,0,1000);
                playAgain_button.setVisibility(View.VISIBLE);
            }
        });

        this.playAgain_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_door1.setImageLevel(0);
                ib_door2.setImageLevel(0);
                ib_door3.setImageLevel(0);
                doorChosen = 0;
                doorRevealed = 0;
                iv_prompt.setText(R.string.choose_a_door);
                setDoors();
                yes_button.setVisibility(View.INVISIBLE);
                no_button.setVisibility(View.INVISIBLE);
                playAgain_button.setVisibility(View.INVISIBLE);
                winnerChecked = false;
                ib_door1.setEnabled(true);
                ib_door2.setEnabled(true);
                ib_door3.setEnabled(true);
            }
        });

        return view;
    }


    public void setDoors() {
        Random randomizer = new Random();
        int randomNumber1 = randomizer.nextInt(3) + 1;

        switch (randomNumber1) {
            case 1:
                behindDoor1 = "Car";
                behindDoor2 = "Goat";
                behindDoor3 = "Goat";
                break;
            case 2:
                behindDoor1 = "Goat";
                behindDoor2 = "Car";
                behindDoor3 = "Goat";
                break;
            case 3:
                behindDoor1 = "Goat";
                behindDoor2 = "Goat";
                behindDoor3 = "Car";
                break;
            default:
                break;
        }

    }

    public void showGoat() {
        switch (doorChosen) {
            case 1:
                if (this.behindDoor2.equals("Goat") && this.behindDoor3.equals("Goat")) {
                    Random randomizer = new Random();
                    int randomNumber = randomizer.nextInt(2);
                    if (randomNumber == 0) {
                        ib_door2.setImageLevel(3);
                        iv_prompt.setText(R.string.change_door_prompt);
                        doorRevealed = 2;
                    }
                    if (randomNumber == 1) {
                        ib_door3.setImageLevel(3);
                        iv_prompt.setText(R.string.change_door_prompt);
                        doorRevealed = 3;
                    }
                } else if (this.behindDoor2.equals("Goat") && this.behindDoor3.equals("Car")) {
                    ib_door2.setImageLevel(3);
                    iv_prompt.setText(R.string.change_door_prompt);
                    doorRevealed = 2;
                } else if (this.behindDoor2.equals("Car") && this.behindDoor3.equals("Goat")) {
                    ib_door3.setImageLevel(3);
                    iv_prompt.setText(R.string.change_door_prompt);
                    doorRevealed = 3;
                }
                break;
            case 2:
                if (this.behindDoor1.equals("Goat") && this.behindDoor3.equals("Goat")) {
                    Random randomizer = new Random();
                    int randomNumber = randomizer.nextInt(2);
                    if (randomNumber == 0) {
                        ib_door1.setImageLevel(3);
                        iv_prompt.setText(R.string.change_door_prompt);
                        doorRevealed = 1;
                    }
                    if (randomNumber == 1) {
                        ib_door3.setImageLevel(3);
                        iv_prompt.setText(R.string.change_door_prompt);
                        doorRevealed = 3;
                    }
                } else if (this.behindDoor1.equals("Goat") && this.behindDoor3.equals("Car")) {
                    ib_door1.setImageLevel(3);
                    iv_prompt.setText(R.string.change_door_prompt);
                    doorRevealed = 1;
                } else if (this.behindDoor1.equals("Car") && this.behindDoor3.equals("Goat")) {
                    ib_door3.setImageLevel(3);
                    iv_prompt.setText(R.string.change_door_prompt);
                    doorRevealed = 3;
                }
                break;
            case 3:
                if (this.behindDoor1.equals("Goat") && this.behindDoor2.equals("Goat")) {
                    Random randomizer = new Random();
                    int randomNumber = randomizer.nextInt(2);
                    if (randomNumber == 0) {
                        ib_door1.setImageLevel(3);
                        iv_prompt.setText(R.string.change_door_prompt);
                        doorRevealed = 1;
                    }
                    if (randomNumber == 1) {
                        ib_door2.setImageLevel(3);
                        iv_prompt.setText(R.string.change_door_prompt);
                        doorRevealed = 2;
                    }
                } else if (this.behindDoor1.equals("Goat") && this.behindDoor2.equals("Car")) {
                    ib_door1.setImageLevel(3);
                    iv_prompt.setText(R.string.change_door_prompt);
                    doorRevealed = 1;
                } else if (this.behindDoor1.equals("Car") && this.behindDoor2.equals("Goat")) {
                    ib_door2.setImageLevel(3);
                    iv_prompt.setText(R.string.change_door_prompt);
                    doorRevealed = 2;
                }
                break;
        }
        yes_button.setVisibility(View.VISIBLE);
        no_button.setVisibility(View.VISIBLE);
    }

    public void checkIfWinner() {

        switch (doorChosen) {
            case 1:
                if (behindDoor1.equals("Car")) {
                    iv_prompt.setText(R.string.winner);
                    ib_door1.setImageLevel(2);
                    soundPool.play(carSound, 1,1,1,0,1f);
                    ib_door1.startAnimation(startAnimation);
                    winStatus = true;

                }
                if (behindDoor1.equals("Goat")) {
                    iv_prompt.setText(R.string.loser);
                    ib_door1.setImageLevel(3);
                    soundPool.play(goatSound, 1,1,1, 0, 1f );
                    ib_door1.startAnimation(startAnimation);
                    winStatus = false;
                }
                break;
            case 2:
                if (behindDoor2.equals("Car")) {
                    iv_prompt.setText(R.string.winner);
                    ib_door2.setImageLevel(2);
                    soundPool.play(carSound, 1,1,1,0,1f);
                    ib_door2.startAnimation(startAnimation);
                    winStatus = true;
                }
                if (behindDoor2.equals("Goat")) {
                    iv_prompt.setText(R.string.loser);
                    ib_door2.setImageLevel(3);
                    soundPool.play(goatSound, 1,1,1, 0, 1f );
                    ib_door2.startAnimation(startAnimation);
                    winStatus = false;
                }
                break;
            case 3:
                if (behindDoor3.equals("Car")) {
                    iv_prompt.setText(R.string.winner);
                    ib_door3.setImageLevel(2);
                    soundPool.play(carSound, 1,1,1,0,1f);
                    ib_door3.startAnimation(startAnimation);
                    winStatus = true;
                }
                if (behindDoor3.equals("Goat")) {
                    iv_prompt.setText(R.string.loser);
                    ib_door3.setImageLevel(3);
                    soundPool.play(goatSound, 1,1,1, 0, 1f );
                    ib_door3.startAnimation(startAnimation);
                    winStatus = false;
                }
        }
    }



    class Counter extends TimerTask {
        private int count = 0;
        @Override

        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageButton doorIB = getIb_door(doorChosen);
                    if (count == 1) {
                        doorIB.setImageLevel(4);
                    } else if (count == 2) {
                        doorIB.setImageLevel(5);
                    } else if (count == 3) {
                        doorIB.setImageLevel(6);
                    }else if(count == 4){
                        checkIfWinner();
                        incrementStats();
                        t.cancel();
                        ctr.cancel();
                        playAgain_button.setVisibility(View.VISIBLE);
                    }
                    count++;
                }
            });
        }

    }

    public void incrementStats(){
        if (winStatus && doorSwitched) {
            numberOfWinsAfterSwitch++;
            totalWins++;
            iv_WinsAfterSwitch.setText(Integer.toString(numberOfWinsAfterSwitch));
            iv_TotalWins.setText(Integer.toString(totalWins));
        }
        if (!winStatus && doorSwitched) {
            numberOfLossesAfterSwitch++;
            totalLosses++;
            iv_LossesAfterSwitch.setText(Integer.toString(numberOfLossesAfterSwitch));
            iv_TotalLosses.setText(Integer.toString(totalLosses));
        }
        if (winStatus && !doorSwitched) {
            numberOfWinsAfterStay++;
            totalWins++;
            iv_WinsAfterStay.setText(Integer.toString(numberOfWinsAfterStay));
            iv_TotalWins.setText(Integer.toString(totalWins));

        }
        if (!winStatus && !doorSwitched) {
            numberOfLossesAfterStay++;
            totalLosses++;
            iv_LossesAfterStay.setText(Integer.toString(numberOfLossesAfterStay));
            iv_TotalLosses.setText(Integer.toString(totalLosses));
        }
    }


    public ImageButton getIb_door(int door) {

        if(door == 1){
            return ib_door1;
        }else if(door ==2){
            return ib_door2;
        }else if(door == 3){
            return ib_door3;
        }else{
            return null;
        }
    }

    @Override
    public void onDestroy() {

        SharedPreferences preferences = getActivity().getSharedPreferences("MontyHall", Context.MODE_PRIVATE);
        preferences.edit().putString("behindDoor1",behindDoor1).apply();
        preferences.edit().putString("behindDoor2",behindDoor2).apply();
        preferences.edit().putString("behindDoor3", behindDoor3).apply();
        preferences.edit().putInt("doorChosen",doorChosen).apply();
        preferences.edit().putInt("doorRevealed", doorRevealed).apply();
        preferences.edit().putInt("winsAfterSwitch", numberOfWinsAfterSwitch).apply();
        preferences.edit().putInt("winsAfterStay", numberOfWinsAfterStay).apply();
        preferences.edit().putInt("lossesAfterSwitch", numberOfLossesAfterSwitch).apply();
        preferences.edit().putInt("lossesAfterStay", numberOfLossesAfterStay).apply();
        preferences.edit().putInt("totalWins",totalWins).apply();
        preferences.edit().putInt("totalLosses",totalLosses).apply();
        preferences.edit().putBoolean("ContinueClicked",false).apply();
        preferences.edit().putBoolean("NewClicked",false).apply();
        if(winnerChecked){
            preferences.edit().putBoolean("winnerChecked",winnerChecked).apply();
            preferences.edit().putBoolean("winStatus", winStatus).apply();
            preferences.edit().putBoolean("doorSwitched",doorSwitched).apply();
        }else{
            preferences.edit().putBoolean("winnerChecked", false).apply();
        }
        super.onDestroy();
    }
}



