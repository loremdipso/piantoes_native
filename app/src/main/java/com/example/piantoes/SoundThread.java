package com.example.piantoes;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.SoundPool;

/**
 * Thread for playing sounds
 *
 * @author soh#zolex
 */
public class SoundThread extends Thread {

	private SoundPool soundPool;
	public BlockingQueue<SoundItem> sounds = new LinkedBlockingQueue<>();
	public boolean stop = false;

	/**
	 * Constructor
	 *
	 * @param soundPool
	 */
	public SoundThread(SoundPool soundPool) {
		this.soundPool = soundPool;
	}

	/**
	 * Dispose a sound
	 *
	 * @param soundID
	 */
	public void unloadSound(int soundID) {

		this.soundPool.unload(soundID);
	}

	@Override
	/**
	 * Wait for sounds to play
	 */
	public void run() {
		try {
			SoundItem item;
			while (!this.stop) {
				item = this.sounds.take();
				if (item.stop) {

					this.stop = true;
					break;
				}

				this.soundPool.play(item.soundID, item.volume, item.volume, 0, 0, 1);
			}

		} catch (InterruptedException e) {
		}
	}
}