package edu.berkeley.cs.cs162.Client;

import edu.berkeley.cs.cs162.Server.GoBoard;

abstract public class Player extends BaseClient {

    GoBoard board;
    boolean waitingForGames;

    public Player(String name, byte type) {
        super(name, type);
        board = new GoBoard(10);
        waitingForGames = true;
    }

    public Player(String name) {
        this(name, (byte) -1);
    }

    public Player() {
        this("");
    }

    /*public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getType() {
        return this.type;
    }

    public void setType(byte type) {
        this.type = type;
    }*/

    public void setBoard(GoBoard b) {
        board = b;
    }

    public GoBoard getBoard() {
        return board;
    }

    public boolean getWaitingForGames() {
        return waitingForGames;
    }

    public void setWaitingForGames(boolean b) {
        waitingForGames = b;
    }
}
