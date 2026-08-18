package com.delightlane.keyboard;

public class ModifierKey extends Key{
    private ModifierState state = ModifierState.Disabled;

    public final ModifierState getState(){
        return state;
    }

    public void setState (ModifierState state) {
        this.state = state;
    }
}
