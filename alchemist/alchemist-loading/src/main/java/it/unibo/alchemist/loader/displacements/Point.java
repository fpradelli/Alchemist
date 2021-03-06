package it.unibo.alchemist.loader.displacements;

import java.util.stream.Stream;

import it.unibo.alchemist.loader.PositionMaker;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * A single node in a single point.
 */
public class Point implements Displacement {

    private final double x, y;
    private final PositionMaker pm;

    /**
     * @param pm
     *            The {@link PositionMaker}
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     */
    public Point(final PositionMaker pm, final double x, final double y) {
        this.x = x;
        this.y = y;
        this.pm = pm;
    }

    @Override
    public Stream<Position> stream() {
        return Stream.of(pm.makePosition(x, y));
    }

}
