package com.welty.othello.gui;

import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

public interface AsyncEngine {
    void requestMove(@NotNull AsyncConsumer consumer, @NotNull Position position, long ping);

    @NotNull String getName();

    void setMaxDepth(int maxDepth);
}
