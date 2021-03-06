package edu.berkeley.cs.cs162.Server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 * Helpers for game rules.
 */
public class Rules {
    /**
     * Get the set of stones which would be captured if a stone of the specified
     * color were to be placed at the specified BoardLocation.
     * 
     * @return A Vector of captured BoardLocations.
     */
    public static Vector<BoardLocation> getCapturedStones(Board board,
            StoneColor color, BoardLocation BoardLocation) {
        Helper helper = new Helper(board);
        helper.connectedComponents();

        HashSet<Integer> threatened = new HashSet<Integer>();
        boolean newStoneIsSafe = false;
        if (BoardLocation.getX() > 0) {
            int group = helper.labels[BoardLocation.getY()][BoardLocation.getX() - 1];
            if (helper.colorMap.get(group) != StoneColor.NONE) {
                threatened.add(group);
            } else {
                newStoneIsSafe = true;
            }
        }
        if (BoardLocation.getY() > 0) {
            int group = helper.labels[BoardLocation.getY() - 1][BoardLocation.getX()];
            if (helper.colorMap.get(group) != StoneColor.NONE) {
                threatened.add(group);
            } else {
                newStoneIsSafe = true;
            }
        }
        if (BoardLocation.getX() < board.getSize() - 1) {
            int group = helper.labels[BoardLocation.getY()][BoardLocation.getX() + 1];
            if (helper.colorMap.get(group) != StoneColor.NONE) {
                threatened.add(group);
            } else {
                newStoneIsSafe = true;
            }
        }
        if (BoardLocation.getY() < board.getSize() - 1) {
            int group = helper.labels[BoardLocation.getY() + 1][BoardLocation.getX()];
            if (helper.colorMap.get(group) != StoneColor.NONE) {
                threatened.add(group);
            } else {
                newStoneIsSafe = true;
            }
        }	

        Vector<Integer> capturedOpponents = new Vector<Integer>();
        Vector<Integer> capturedSelf = new Vector<Integer>();
        Iterator<Integer> iter = threatened.iterator();
        while (iter.hasNext()) {
            // did we kill this group?
            int i = (Integer) iter.next();
            boolean foundLiberty = false;
            Vector<BoardLocation> BoardLocations = helper.neighbourMap.get(i);
            for (int j = 0; j < BoardLocations.size() && !foundLiberty; j++) {
                BoardLocation l = (BoardLocation) BoardLocations.get(j);
                if (!l.equals(BoardLocation)
                        && board.getAtLocation(l) == StoneColor.NONE) {
                    foundLiberty = true;
                }
            }
            if (!foundLiberty) {
                // dead.
                if (helper.colorMap.get(i) != color) {
                    capturedOpponents.add(i);
                } else if (!newStoneIsSafe) {
                    capturedSelf.add(i);
                }
            }
        }
        Vector<BoardLocation> result = new Vector<BoardLocation>();
        if (!capturedOpponents.isEmpty()) {
            for (int i = 0; i < capturedOpponents.size(); i++) {
                helper.addGroupBoardLocations(capturedOpponents.get(i), result);
            }
        } else if (!capturedSelf.isEmpty()) {
            for (int i = 0; i < capturedSelf.size(); i++) {
                helper.addGroupBoardLocations(capturedSelf.get(i), result);
            }
            // the newly placed stone also dies
            result.add(BoardLocation);
        }
        return result;
    }

    /**
     * Counts the amount of territory surrounded by by stones of this color.
     * 
     * @return The amount of territory owned by the given color.
     */
    public static int countOwnedTerritory(Board board, StoneColor color) {
        int result = 0;

        Helper helper = new Helper(board);
        helper.connectedComponents();

        Iterator<Integer> iter = helper.colorMap.keySet().iterator();
        while (iter.hasNext()) {
            int i = (Integer) iter.next();
            if (helper.colorMap.get(i) == StoneColor.NONE
                    && helper.countMap.keySet().contains(i)) {
                // may count.
                boolean touchesPlayer = false, touchesOpponent = false;
                Vector<BoardLocation> BoardLocations = helper.neighbourMap.get(i);
                for (int j = 0; j < BoardLocations.size(); j++) {
                    if (board.getAtLocation(BoardLocations.get(j)) == color) {
                        touchesPlayer = true;
                    } else if (board.getAtLocation(BoardLocations.get(j)) != color) {
                        touchesOpponent = true;
                    }
                }
                if (touchesPlayer && !touchesOpponent) {
                    result += helper.countMap.get(i);
                }
            }
        }
        return result;
    }

    private static class Helper {
        public Board board;
        public int[][] labels;
        public HashMap<Integer, StoneColor> colorMap;
        public HashMap<Integer, Integer> countMap;
        public HashMap<Integer, Vector<BoardLocation>> neighbourMap;

        public Helper(Board board) {
            this.board = board;
        }

        public void addGroupBoardLocations(int label, Vector<BoardLocation> result) {
            for (int y = 0; y < board.getSize(); y++) {
                for (int x = 0; x < board.getSize(); x++) {
                    if (labels[y][x] == label) {
                        result.add(new BoardLocation(x, y));
                    }
                }
            }
        }

        public void connectedComponents() {
            HashMap<Integer, HashSet<Integer>> equiv = new HashMap<Integer, HashSet<Integer>>();
            int nextLabel = 1;
            labels = new int[board.getSize()][board.getSize()];
            colorMap = new HashMap<Integer, StoneColor>();
            HashMap<Integer, HashSet<BoardLocation>> neighbours = new HashMap<Integer, HashSet<BoardLocation>>();
            // first pass
            for (int y = 0; y < board.getSize(); y++) {
                for (int x = 0; x < board.getSize(); x++) {
                    StoneColor c = board.getAtLocation(new BoardLocation(x, y));
                    boolean isWestEqual = (x > 0 && c == board
                            .getAtLocation(new BoardLocation(x - 1, y)));
                    boolean isNorthEqual = (y > 0 && c == board
                            .getAtLocation(new BoardLocation(x, y - 1)));

                    if (isWestEqual && isNorthEqual) {
                        labels[y][x] = Math.min(labels[y - 1][x],
                                labels[y][x - 1]);
                        HashSet<Integer> s = new HashSet<Integer>();
                        s.addAll(equiv.get(labels[y - 1][x]));
                        s.addAll(equiv.get(labels[y][x - 1]));
                        Iterator<Integer> iter = s.iterator();
                        while (iter.hasNext()) {
                            equiv.get(iter.next()).addAll(s);
                        }
                    } else if (isWestEqual) {
                        labels[y][x] = labels[y][x - 1];
                    } else if (isNorthEqual) {
                        labels[y][x] = labels[y - 1][x];
                    } else {
                        labels[y][x] = nextLabel++;
                        equiv.put(labels[y][x], new HashSet<Integer>());
                        equiv.get(labels[y][x]).add(labels[y][x]);
                        colorMap.put(labels[y][x], c);
                        neighbours.put(labels[y][x], new HashSet<BoardLocation>());
                    }

                    // neighbours
                    if (x > 0 && !isWestEqual) {
                        neighbours.get(labels[y][x])
                        .add(new BoardLocation(x - 1, y));
                        neighbours.get(labels[y][x - 1])
                        .add(new BoardLocation(x, y));
                    }
                    if (y > 0 && !isNorthEqual) {
                        neighbours.get(labels[y][x])
                        .add(new BoardLocation(x, y - 1));
                        neighbours.get(labels[y - 1][x])
                        .add(new BoardLocation(x, y));
                    }
                }
            }
            // find the mapping
            HashMap<Integer, Integer> minMap = new HashMap<Integer, Integer>();
            Iterator<Integer> iter = equiv.keySet().iterator();
            while (iter.hasNext()) {
                Integer i = (Integer) iter.next();
                Integer minValue = i;
                Iterator<Integer> iter2 = equiv.get(i).iterator();
                while (iter2.hasNext()) {
                    minValue = Math.min(minValue, (Integer) iter2.next());
                }
                minMap.put(i, minValue);
            }

            // second pass, translate.
            int[] counts = new int[nextLabel];
            for (int y = 0; y < board.getSize(); y++) {
                for (int x = 0; x < board.getSize(); x++) {
                    labels[y][x] = minMap.get(labels[y][x]);
                    counts[labels[y][x]]++;
                }
            }
            countMap = new HashMap<Integer, Integer>();
            neighbourMap = new HashMap<Integer, Vector<BoardLocation>>();
            for (int i = 0; i < nextLabel; i++) {
                if (counts[i] > 0) {
                    countMap.put(i, counts[i]);
                    HashSet<BoardLocation> BoardLocations = new HashSet<BoardLocation>();
                    HashSet<Integer> e = equiv.get(i);
                    iter = e.iterator();
                    while (iter.hasNext()) {
                        BoardLocations.addAll(neighbours.get(iter.next()));
                    }
                    Vector<BoardLocation> vl = new Vector<BoardLocation>();
                    vl.addAll(BoardLocations);
                    neighbourMap.put(i, vl);
                }
            }
        }

        @SuppressWarnings("unused")
		public void printLabels() {
            for (int i = 0; i < board.getSize(); i++) {
                String s = "";
                for (int j = 0; j < board.getSize(); j++) {
                    s = s + labels[i][j];
                }
                System.out.println(s);
            }
        }
    }
}
