package com.example.ayo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ayo.data.Board;
import com.example.ayo.data.Pit;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private final static String pit_prefix = "pit_";
    private final static String seeds_prefix = "seeds_";

    private static final int WINNING_NUM_OF_SEEDS = 4;

    private Board board = new Board();
    private TextView playerOneSeedsCountTextView;
    private TextView playerTwoSeedsCountTextView;

    private boolean playerOneTurn = true;
    private int playerOneSeedsCount;
    private int playerTwoSeedsCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerOneSeedsCountTextView = findViewById(R.id.player_one_seeds_count_text_view);
        playerTwoSeedsCountTextView = findViewById(R.id.player_two_seeds_count_text_view);

        for (int i = 0; i < board.getPitsCount(); i++) {

            View view = getPitView(i);

            if (view != null) {

                view.setOnClickListener(this::onPitClicked);
                view.setOnLongClickListener(v -> {
                    toast(String.valueOf(board.getPit(getPitNumber(v)).getSeedsCount()));
                    return true;
                });
            }
        }

        populateBoardView();
        populatePlayersSeedsCountView();
    }

    private void populateBoardView() {

        for (int pitNumber = 0; pitNumber < board.getPitsCount(); pitNumber++) {

            int pitSeedsCount = board.getPit(pitNumber).getSeedsCount();

            ImageButton button = getPitView(pitNumber);

            if (button != null) {

                button.setImageResource(getResourceId("drawable", seeds_prefix + pitSeedsCount));
            }
        }
    }

    private void populatePlayersSeedsCountView() {

        playerOneSeedsCountTextView.setText(String.valueOf(playerOneSeedsCount));
        playerTwoSeedsCountTextView.setText(String.valueOf(playerTwoSeedsCount));
    }


    public void onPitClicked(View view) {

        int pitNum = getPitNumber(view);

        if (pitNum == -1)
            toast("invalid pit view format");
        else {

            if ((playerOneTurn && pitNum < board.getPitsCount() / 2) ||
                    (!playerOneTurn && pitNum >= board.getPitsCount() / 2)) {

                if (board.getPit(pitNum).getSeedsCount() == 0) toast("you selected a seedless pit");
                else {

                    play(pitNum);
                }

            } else toast("It's not your turn");
        }
    }

    private boolean availableMoveForPlayer(boolean playerOne) {

        if (playerOne) {

            for (int i = 0; i < board.getPitsCount() / 2; i++) {

                if (board.getPit(i).getSeedsCount() > 0)
                    return true;
            }
        } else {

            for (int i = board.getPitsCount() / 2; i < board.getPitsCount(); i++) {

                if (board.getPit(i).getSeedsCount() > 0)
                    return true;
            }
        }

        return false;
    }

    private void play(int pitNum) {

        Pit pit = board.getPit(pitNum);
        int seedsCount = pit.getSeedsCount();

        pit.clear();
        updatePitView(pitNum % board.getPitsCount(), pit.getSeedsCount(), new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }

                updatePlay(seedsCount, pitNum + 1, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        int lastPlayedPit = seedsCount + pitNum;

                        if (board.getPit(lastPlayedPit).getSeedsCount() > 1) {

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {
                            }

                            play(lastPlayedPit);
                        } else {

                            playerOneTurn = !playerOneTurn;

                            if (!availableMoveForPlayer(true) || !availableMoveForPlayer(false))
                                displayGameOverDialog();
                        }
                    }
                });
            }
        });
    }

    private void updatePlay(int seedsCount, int pitNum, @NotNull final AnimatorListenerAdapter listener) {

        if (seedsCount <= 0)
            listener.onAnimationEnd(null);
        else {

            Pit nextPit = board.getPit(pitNum);
            nextPit.incrementSeedCount();

            final int newSeedCount = seedsCount - 1;
            final int nextPitNum = pitNum + 1;

            updatePitView(pitNum % board.getPitsCount(), nextPit.getSeedsCount(), new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {

                    if (nextPit.getSeedsCount() != WINNING_NUM_OF_SEEDS) {

                        updatePlay(newSeedCount, nextPitNum, listener);
                    } else {

                        nextPit.clear();
                        updateWinningPitView(pitNum % board.getPitsCount(), new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {

                                int newValue = (playerOneTurn) ? (playerOneSeedsCount += WINNING_NUM_OF_SEEDS) : (playerTwoSeedsCount += WINNING_NUM_OF_SEEDS);
                                updateSeedsCountView(playerOneTurn, newValue, null);

                                updatePlay(newSeedCount, nextPitNum, listener);
                            }
                        });
                    }
                }
            });
        }
    }

    private void displayGameOverDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_over);

        //making the dialog span the whole screen
        Window window = dialog.getWindow();
        if (window != null) {

            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.8f;
            window.setAttributes(lp);

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        dialog.setCancelable(false);

        Button restartButton = dialog.findViewById(R.id.restart_button);
        Button exitButton = dialog.findViewById(R.id.exit_button);

        TextView message = dialog.findViewById(R.id.message);

        if (playerOneSeedsCount > playerTwoSeedsCount)
            message.setText("Player one wins the match");
        else if (playerTwoSeedsCount > playerOneSeedsCount)
            message.setText("Player two wins the match");
        else
            message.setText("It is a tie");

        restartButton.setOnClickListener(v -> {

            board = new Board();
            playerOneSeedsCount = 0;
            playerTwoSeedsCount = 0;

            playerOneTurn = true;

            populateBoardView();
            populatePlayersSeedsCountView();

            dialog.dismiss();
        });

        exitButton.setOnClickListener(v -> finish());

        dialog.show();
    }

    private void updatePitView(int pitNumber, int pitSeedCount, @Nullable AnimatorListenerAdapter listener) {

        ImageButton button = getPitView(pitNumber);

        if (button != null) {

            if (pitSeedCount == 0) {

                button.animate().alpha(0f).setDuration(300).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        button.setImageDrawable(null);
                        button.setAlpha(1f);

                        if (listener != null)
                            listener.onAnimationEnd(animation);

                    }
                }).start();
            } else {

                button.animate().scaleX(1.7f).scaleY(1.7f).alpha(0f).setDuration(300).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        button.setImageResource(getResourceId("drawable", seeds_prefix + pitSeedCount));
                        button.animate().scaleX(1.5f).scaleY(1.5f).alpha(1f).setDuration(300).setInterpolator(new AccelerateInterpolator()).setListener(listener).start();
                    }
                }).start();
            }
        }
    }

    private void updateWinningPitView(int pitNumber, @Nullable AnimatorListenerAdapter listener) {

        ImageButton button = getPitView(pitNumber);

        if (button != null) {

            button.animate().scaleX(1.8f).scaleY(1.8f).setDuration(300).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                    button.animate().scaleX(1.5f).scaleY(1.5f).alpha(0f).setDuration(300).setInterpolator(new AccelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            button.setImageDrawable(null);
                            button.setAlpha(1f);

                            if (listener != null)
                                listener.onAnimationEnd(animation);
                        }
                    });
                }

            }).start();
        }
    }

    private void updateSeedsCountView(boolean playerOne, int newValue, @Nullable AnimatorListenerAdapter listener) {

        TextView viewToAnimate = playerOne ? playerOneSeedsCountTextView : playerTwoSeedsCountTextView;

        viewToAnimate.animate().alpha(0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {

                viewToAnimate.setText(String.valueOf(newValue));
                viewToAnimate.animate().alpha(1f).setDuration(200).setListener(listener).setInterpolator(new AccelerateInterpolator()).start();
            }
        }).start();
    }


    private @Nullable ImageButton getPitView(int pitNumber) {

        int resId = getResourceId("id", pit_prefix + pitNumber);
        return findViewById(resId);
    }

    private int getPitNumber(@NotNull View view) {

        try {
            String pitButtonId = view.getResources().getResourceEntryName(view.getId());
            return Integer.parseInt(pitButtonId.substring(4));

        } catch (Exception exception) {

            return -1;
        }
    }


    private int getResourceId(@NotNull String resType, @NotNull String id) {

        return getResources().getIdentifier(id, resType, getPackageName());
    }

    private void toast(String message) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}