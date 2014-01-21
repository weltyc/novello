package com.welty.novello.coca;

import com.welty.novello.core.Mr;

import java.io.IOException;
import java.util.Set;

public interface MrSource {
    Set<Mr> getMrs() throws IOException;
}
