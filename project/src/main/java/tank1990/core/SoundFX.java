/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tank1990.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @class SoundFX
 * @brief A class for handling sound effects.
 * @details This class loads and plays sound files using Java's Clip API. It supports
 * functionalities such as playing, stopping, pausing, resuming, looping, 
 * fading out, and delayed playback.
 */
public class SoundFX implements Serializable{
    private String path;                            /**< Path to the sound file. */
    private transient Clip clip;                    /**< The audio clip used for playback. */
    private transient FloatControl volumeControl;   /**< Control for adjusting the volume. */
    private long lastClipPosition = 0;              /**< Stores the paused position of the clip. */

    /**
     * Constructs a SoundFX object with the specified sound file path.
     * @param path The path to the sound file.
     */
    public SoundFX(String path) {
        this.path = path;
        initializeClip();
    }

    /**
     * Initializes the audio clip by loading the sound file.
     */
    private void initializeClip() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResourceAsStream(this.path));
            this.clip = AudioSystem.getClip();
            this.clip.open(audioStream);
            this.volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the audio playback with a fade-out effect.
     * @param fadeOutDurationMs The duration (in milliseconds) for the fade-out effect.
     */
    private void fadeOutStop(int fadeOutDurationMs) {
        if (this.clip != null && this.clip.isRunning()) {
            new Thread(() -> {
                float minVolume = volumeControl.getMinimum();
                float currentVolume = volumeControl.getValue();
                int steps = 30; // Number of steps in fade-out
                int stepDelay = fadeOutDurationMs / steps;

                for (int i = 0; i < steps; i++) {
                    currentVolume -= (currentVolume - minVolume) / (steps - i);
                    currentVolume = Math.max(currentVolume, minVolume); // Prevent volume going below min
                    volumeControl.setValue(currentVolume);

                    try {
                        Thread.sleep(stepDelay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
                
                this.clip.stop(); // Stop music after fade-out
                this.clip.setFramePosition(0); // Reset for next play
                this.lastClipPosition = 0;
            }).start();
        }
    }

    /**
     * Stops the audio playback immediately.
     */
    public void stop() {
        if (this.clip != null) {
            if (this.clip.isRunning()) {
                this.clip.stop(); // Stop the music immediately
            }
            this.clip.setFramePosition(0); // Reset to the beginning for the next play
    
            // Release resources
            //this.clip.close();
            //this.clip = null;
        }
        this.lastClipPosition = 0;
    }

    /**
     * Plays the sound.
     * @param loop If true, the sound will loop continuously.
     */
    public void play(boolean loop) {
        if (this.clip != null) {
            new Thread(() -> {
                if (this.lastClipPosition > 0) {
                    clip.setMicrosecondPosition(this.lastClipPosition); // Resume from paused position                
                } else {
                    this.clip.setFramePosition(0); // Start from beginning
                }

                if (loop) {
                    this.clip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    this.clip.start();
                }
            }).start();
        }
    }

    /**
     * Plays the sound with an optional fade-out effect.
     * @param loop If true, the sound will loop continuously.
     * @param fadeOutDurationMs The duration (in milliseconds) of the fade-out effect.
     */
    public void play(boolean loop, int fadeOutDurationMs) {
        if (this.clip != null) {
            new Thread(() -> {
                this.clip.setFramePosition(0); // Start from beginning
                if (loop) {
                    this.clip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    this.clip.start();
                }
            }).start();
        }

        if (fadeOutDurationMs > 0) {
            fadeOutStop(fadeOutDurationMs);
        }
    }

    /**
     * Delays the playback of the sound.
     * @param loop If true, the sound will loop continuously.
     * @param delayDurationMS The delay time in milliseconds before playback starts.
     */
    public void delayedPlay(boolean loop, int delayDurationMS) {

        if (this.clip != null && !this.clip.isRunning()) {
            new Thread(() -> {
                try {
                    if (this.lastClipPosition > 0) {
                        clip.setMicrosecondPosition(this.lastClipPosition); // Resume from paused position                
                    } else {
                        this.clip.setFramePosition(0); // Start from beginning
                    }

                    Thread.sleep(delayDurationMS); // Delay of 1 second before playing the next sound

                    if (loop) {
                        this.clip.loop(Clip.LOOP_CONTINUOUSLY);
                    } else {
                        this.clip.start();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Pauses the sound playback.
     * 
     * Stores the current position of the clip so playback can be resumed.
     */
    public void pause() {
        if (this.clip != null && this.clip.isRunning()) {
            this.lastClipPosition = this.clip.getMicrosecondPosition(); // Save current position
            this.clip.stop();
        }
    }

    /**
     * Handles object deserialization and reinitialized the audio clip.
     * @param in The object input stream.
     * @throws IOException If an I/O error occurs.
     * @throws ClassNotFoundException If the class definition is not found.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResourceAsStream(this.path));
            this.clip = AudioSystem.getClip();
            this.clip.open(audioStream);

            this.volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
