package com.delightlane.keyboard.event;

public class InputCharEvent extends SebeolHangulIMEEvent {
	private char code;

	public InputCharEvent(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}

}
