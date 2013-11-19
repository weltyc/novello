package com.welty.novello.eval;

/**
 */
public abstract class Term {
    private final SoloFeature feature = new CornerFeature();

    public abstract int instance(long mover, long enemy, long moverMoves, long enemyMoves);

    /**
     * Get the orid of the position.
     *
     * @return  orid
     */
    public int orid(long mover, long enemy, long moverMoves, long enemyMoves) {
        return getFeature().orid(instance(mover, enemy, moverMoves, enemyMoves));
    }

    public SoloFeature getFeature() {
        return feature;
    }
}
