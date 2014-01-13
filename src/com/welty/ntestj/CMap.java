package com.welty.ntestj;

import static com.welty.ntestj.PatternUtils.*;
import static com.welty.ntestj.table.CridTable.*;
import static com.welty.ntestj.table.OridTable.*;
import static com.welty.ntestj.table.R33Table.Base3ToR33ID;
import static com.welty.ntestj.table.R33Table.R33IDToBase3;
import com.orbanova.common.misc.Require;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 7, 2009
 * Time: 8:20:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class CMap {
    // todo change the switch statements to functions on the enums
    public enum TIdType {
        kBase3, kORID, kCRID, kR25ID, kR33ID, kMobCombo, kNumber
    }

    private final TIdType idType;
    public final char size;

    public CMap(TIdType idType, int size) {
        this.idType = idType;
        Require.lt(size, "size", 65536);
        this.size = (char) size;
    }

    public char NIDs() {
        switch (idType) {
            case kBase3:
                Require.lt(size, "size", maxBase3PatternSize);
                return nBase3s[size];
            case kORID:
                Require.lt(size, "size", maxORIDPatternSize);
                return nORIDs[size];
            case kCRID:
                Require.lt(size, "size", maxCRIDPatternSize);
                return nCRIDs[size];
            case kMobCombo:
            case kNumber:
                return size;
            case kR33ID:
                return 14 * 729;
            default:
                throw new IllegalStateException("Shouldn't be here");
        }
    }

    public char NConfigs() {
        switch (idType) {
            case kBase3:
            case kORID:
            case kCRID:
                Require.lt(size, "size", maxBase3PatternSize);
                return nBase3s[size];
            case kMobCombo:
            case kNumber:
                return size;
            case kR25ID:
                return nBase3s[10];
            case kR33ID:
                return nBase3s[9];
            default:
                throw new IllegalStateException("Shouldn't be here");
        }
    }

    public char ConfigToID(char config) {
        switch (idType) {
            case kORID:
                return Base3ToORID(config, size);
            case kCRID:
                return Base3ToCRID(config, size);
            case kR33ID:
                return Base3ToR33ID(config);
            case kBase3:
            case kMobCombo:
            case kNumber:
            case kR25ID:
                return config;
            default:
                throw new IllegalStateException("Shouldn't be here");
        }
    }

    char IDToConfig(char id) {
        switch (idType) {
            case kBase3:
                Require.lt(size, "size", maxBase3PatternSize);
                Require.lt(id, "id", nBase3s[size]);
                return id;
            case kORID:
                Require.lt(size, "size", maxORIDPatternSize);
                Require.lt(id, "id", nORIDs[size]);
                return ORIDToBase3(id, size);
            case kCRID:
                Require.lt(size, "size", maxCRIDPatternSize);
                Require.lt(id, "id", nCRIDs[size]);
                Require.isTrue(size == 6 || size == 10, "size must be 6 or 10");
                return CRIDToBase3(id, size);
            case kR33ID:
                return R33IDToBase3(id);
            case kMobCombo:
            case kNumber:
                return id;
            default:
                throw new IllegalStateException("Shouldn't be here");
        }
    }

    public String IDToString(char id) {
        switch (idType) {
            case kNumber:
                return "";
            case kMobCombo:
                return String.format("%2d-%2d", id / 16, id % 16);
            default:
                return PrintBase3(IDToConfig(id), size);
        }
    }

    String ConfigToString(char config) {
        switch (idType) {
            case kNumber:
                return "";
            case kMobCombo:
                return String.format("%2d-%2d", config / 16, config % 16);
            default:
                return PrintBase3(config, size);
        }
    }
}
