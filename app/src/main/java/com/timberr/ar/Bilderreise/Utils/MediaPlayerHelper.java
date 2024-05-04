package com.timberr.ar.Bilderreise.Utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;


import java.io.IOException;

public class MediaPlayerHelper {

    private MediaPlayer mediaPlayer;
    private String videoName;

    public MediaPlayerHelper(String videoName) {
        this.videoName = videoName;
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public boolean isLooping() {
        if (isPlaying() && mediaPlayer != null) {
            return mediaPlayer.isLooping();
        }
        return false;
    }

    public void playMedia(final Context context, Surface mediaSurface) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setSurface(mediaSurface);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(videoName);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setSurface(mediaSurface);
        mediaPlayer.setLooping(true);

        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {

            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                Toast.makeText(context, "onPrepare --> Start", Toast.LENGTH_SHORT).show();
                mediaPlayer.start();
            }
        });


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer m) {
//                Toast.makeText(context, "End Play", Toast.LENGTH_LONG).show();
                m.stop();
                m.release();
            }
        });

        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
