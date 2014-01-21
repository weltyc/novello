package com.welty.ggf;

import com.orbanova.common.misc.Require;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;

/**
 * A parsed representation of a GGF game.
 * <p/>
 * GgfGame instances are immutable.
 */
public class GgfGame {
    /**
     * Game, for example "Othello"
     */
    public final String game;

    /**
     * Match location, for example "GGS/os"
     */
    public final String place;

    /**
     * Match date.
     * <p/>
     * The format of this varies over the life of GGS, so it's just stored as text.
     */
    public final String date;

    /**
     * Name of the black player
     */
    public final String blackPlayer;

    /**
     * Name of the white player
     */
    public final @NotNull String whitePlayer;

    /**
     * Rating of black player
     */
    public final double blackRating;

    /**
     * black player rating, as a string, so we can exactly reproduce GGS games
     */
    public final String blackRatingString;

    /**
     * Rating of white player
     */
    public final double whiteRating;

    /**
     * white player rating, as a string, so we can exactly reproduce GGS games
     */
    public final String whiteRatingString;

    /**
     * String representation of time remaining.
     * <p/>
     * Use getter to access this - underlying representation may change.
     */
    private final @NotNull String blackTime;

    /**
     * String representation of time remaining.
     * <p/>
     * Use getter to access this - underlying representation may change.
     */
    private final @NotNull String whiteTime;

    /**
     * String representation of game type ("8", "s8r22").
     * <p/>
     * Use getter to access this - underlying representation may change.
     */
    private final @NotNull String typeString;

    /**
     * String representation of game result ("+0.00").
     * <p/>
     * Use getter to access this - underlying representation may change.
     */
    private final @NotNull String resultString;

    /**
     * String representation of game start position ("4 ---- -O*- -*O- ---- *")
     * <p/>
     * Use getter to access this - underlying representation may change.
     */
    private final @NotNull String startPositionString;

    /**
     * List of all moves that occurred in the game.
     * <p/>
     * This is an unmodifiable list of immutable objects, so it's public
     */
    public final List<Move> moves;

    public final @Nullable Move blackKomi;
    public final @Nullable Move whiteKomi;
    public final @Nullable String netKomi;

    public GgfGame(String game, String place, String date, String blackPlayer, @NotNull String whitePlayer
            , @NotNull String blackRatingString, @NotNull String whiteRatingString, @NotNull String blackTime, @NotNull String whiteTime
            , @NotNull String typeString, @NotNull String resultString, @NotNull String startPositionString
            , Map<String, String> map, List<Move> moves) {
        this.game = game;
        this.place = place;
        this.date = date;
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.blackRatingString = blackRatingString;
        this.whiteRatingString = whiteRatingString;
        blackRating = Double.parseDouble(blackRatingString);
        whiteRating = Double.parseDouble(whiteRatingString);
        this.blackTime = blackTime;
        this.whiteTime = whiteTime;
        this.typeString = typeString;
        this.resultString = resultString;
        this.startPositionString = startPositionString;
        this.moves = Collections.unmodifiableList(new ArrayList<>(moves));
        if (typeString.contains("k")) {
            blackKomi = new Move(getRequiredTag(map, "KB"));
            whiteKomi = new Move(getRequiredTag(map, "KW"));
            netKomi = getRequiredTag(map,"KM");
        } else {
            blackKomi = null;
            whiteKomi = null;
            netKomi = null;
        }
    }

    /**
     * Parse the GGF
     *
     * @param ggf game, in GGF format
     * @return parsed representation of the game
     */
    public static @NotNull GgfGame of(@NotNull String ggf) {
        // strip extraneous whitespace
        ggf = ggf.trim();

        // remove preceding and trailing "(;"
        Require.gt(ggf.length(), "ggf length", 4);
        Require.eq(ggf.substring(0, 2), "ggf game start", "(;");
        Require.eq(ggf.substring(ggf.length() - 2), "ggf game end", ";)");
        ggf = ggf.substring(2, ggf.length() - 2);

        // get tags
        final String[] pairs = ggf.split("\\]");
        final Map<String, String> map = new HashMap<>();
        final List<Move> moves = new ArrayList<>();


        for (String pair : pairs) {
            final String[] tagValue = pair.split("\\[");
            if (tagValue.length != 2) {
                throw new IllegalArgumentException("Improper tag: " + (pair + "]"));
            }

            final String tag = tagValue[0];
            final String value = tagValue[1];
            if (tag.equals("B") || tag.equals("W")) {
                moves.add(new Move(value));
            } else {
                map.put(tag, value);
            }
        }

        final String time = map.get("TI");
        final @NotNull String blackTime;
        final @NotNull String whiteTime;
        if (time != null) {
            blackTime = time;
            whiteTime = time;
        } else {
            blackTime = getRequiredTag(map, "TB");
            whiteTime = getRequiredTag(map, "TW");
        }

        return new GgfGame(getRequiredTag(map, "GM")
                , getRequiredTag(map, "PC")
                , getRequiredTag(map, "DT")
                , getRequiredTag(map, "PB")
                , getRequiredTag(map, "PW")
                , getRequiredTag(map, "RB")
                , getRequiredTag(map, "RW")
                , blackTime
                , whiteTime
                , getRequiredTag(map, "TY")
                , getRequiredTag(map, "RE")
                , getRequiredTag(map, "BO")
                , map
                , moves
        );
    }

    /**
     * Get the value of a tag, throwing an IllegalArgumentException if the tag can't be found.
     *
     * @param tags list of tags
     * @param tag  tag name
     * @return tag value
     */
    public static String getRequiredTag(Map<String, String> tags, String tag) {
        final String value = tags.get(tag);
        if (value == null) {
            throw new IllegalArgumentException("GGF game is missing a required tag: " + tag);
        }
        return value;
    }

    public @NotNull String getBlackTimeString() {
        return blackTime;
    }

    public @NotNull String getWhiteTimeString() {
        return whiteTime;
    }

    public @NotNull String getTypeString() {
        return typeString;
    }

    public @NotNull String getResultString() {
        return resultString;
    }

    public @NotNull String getStartPositionString() {
        return startPositionString;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("(;GM[").append(game).append("]PC[").append(place).append("]DT[").append(date)
                .append("]PB[").append(blackPlayer).append("]PW[").append(whitePlayer);
        sb.append("]RB[").append(blackRatingString).append("]RW[").append(whiteRatingString);
        if (getBlackTimeString().equals(getWhiteTimeString())) {
            sb.append("]TI[").append(getBlackTimeString());
        } else {
            sb.append("]TB[").append(getBlackTimeString()).append("]TW[").append(getWhiteTimeString());
        }
        sb.append("]TY[").append(getTypeString());
        if (blackKomi != null) {
            sb.append("]KB[").append(blackKomi).append("]KW[").append(whiteKomi).append("]KM[").append(netKomi);
        }
        sb.append("]RE[").append(getResultString())
                .append("]BO[").append(getStartPositionString()).append("]");
        boolean blackMove = getStartPositionString().endsWith("*");
        for (Move move : moves) {
            sb.append(blackMove ? "B[" : "W[").append(move.toString()).append("]");
            blackMove = !blackMove;
        }
        sb.append(";)");
        return sb.toString();

    }

    public int getBoardSize() {
        return Integer.parseInt(startPositionString.split("\\s+")[0]);
    }

    public boolean isAnti() {
        return getTypeString().contains("a");
    }

    public boolean isSynchro() {
        return getTypeString().contains("s");
    }

    public boolean isKomi() {
        return getTypeString().contains("k");
    }
}
