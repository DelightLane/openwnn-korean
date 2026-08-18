package com.delightlane.keyboard.event;

import com.delightlane.keyboard.hangul.EngineMode;

public class EngineModeChangeEvent extends SebeolHangulIMEEvent {

	EngineMode engineMode;

	public EngineModeChangeEvent(EngineMode engineMode) {
		this.engineMode = engineMode;
	}

	public EngineMode getEngineMode() {
		return engineMode;
	}

}
