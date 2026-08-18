
package com.delightlane.keyboard.event;

public abstract class SebeolHangulIMEEvent {

	private boolean cancelled;

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}

