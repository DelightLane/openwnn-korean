package com.delightlane.keyboard.event;

import android.view.KeyEvent;

public class KeyUpEvent extends SebeolHangulIMEEvent {

	private KeyEvent keyEvent;

	public KeyUpEvent(KeyEvent keyEvent) {
		this.keyEvent = keyEvent;
	}

	public KeyEvent getKeyEvent() {
		return keyEvent;
	}

}
