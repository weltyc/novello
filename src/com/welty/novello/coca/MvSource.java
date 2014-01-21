package com.welty.novello.coca;

import com.welty.novello.core.MeValue;

import java.io.IOException;
import java.util.List;

public interface MvSource {
    /**
     * Get a list of MeValues suitable for coefficient calculation
     *
     * @return the list of MeValues
     */
    public List<MeValue> getMvs() throws IOException;
}
