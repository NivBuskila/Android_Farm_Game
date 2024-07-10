package com.example.cargame.Utilities;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundPlayer {

    private Context context;
    private MediaPlayer effectPlayer;

    public SoundPlayer(Context context) {
        this.context = context;
    }

    public void playSound(int resID) {
        stopSound();

        effectPlayer = MediaPlayer.create(context, resID);
        if (effectPlayer != null) {
            effectPlayer.setOnCompletionListener(mp -> {
                mp.release();
                effectPlayer = null;
            });
            effectPlayer.start();
        }
    }

    public void stopSound() {
        if (effectPlayer != null) {
            effectPlayer.stop();
            effectPlayer.release();
            effectPlayer = null;
        }
    }

    public void release() {
        stopSound();
    }
}