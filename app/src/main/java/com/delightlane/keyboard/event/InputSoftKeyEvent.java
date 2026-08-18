package com.delightlane.keyboard.event;

import android.view.KeyEvent;

public class InputSoftKeyEvent extends SebeolHangulIMEEvent {

	private KeyEvent keyEvent;

	public InputSoftKeyEvent(KeyEvent keyEvent) {
		this.keyEvent = keyEvent;
	}

	public KeyEvent getKeyEvent() {
		return keyEvent;
	}

}
