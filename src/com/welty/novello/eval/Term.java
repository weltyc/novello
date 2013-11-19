package com.welty.novello.eval;

/**
 */
public abstract class Term {
    private final Feature feature = new CornerFeature();

    public abstract int instance(long mover, long enemy, long moverMoves, long enemyMoves);

    /**
     * Get the orid of the position.
     *
     * @return  orid
     */
    public int orid(long mover, long enemy, long moverMoves, long enemyMoves) {
        return getFeature().orid(instance(mover, enemy, moverMoves, enemyMoves));
    }

    public Feature getFeature() {
        return feature;
    }
}
