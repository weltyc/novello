package com.welty.othello.api;

public abstract class NBoardEngine {
    /**
     * Send a single line of text to the Engine
     *
     * @param command the text of the command
     */
    public abstract void sendCommand(String command);
}
