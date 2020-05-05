package io.nosqlbench.activitytype.cql.datamappers.functions.geometry;

import com.datastax.driver.dse.geometry.Point;
import com.datastax.driver.dse.geometry.Polygon;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.curves4.discrete.long_int.Uniform;

import java.util.function.LongFunction;

/**
 * This function will return a polygon in the form of a rectangle from the specified
 * grid system. The coordinates define the top left and bottom right coordinates in
 * (x1,y1),(x2,y2) order, while the number of rows and columns divides these ranges
 * into the unit-length for each square.
 * x1 must be greater than x2. y1 must be less than y2.
 *
 * This grid system can be used to construct a set of overlapping grids such that the
 * likelyhood of overlap is somewhat easy to reason about. For example, if you create
 * one grid system as a refernce grid, then attempt to map another grid system which
 * half overlaps the original grid, you can easily determine that half the time, a
 * random rectangle selected from the second grid will overlap a rectangle from the
 * first, for simple even-numbered grids and the expected uniform sampling on the
 * internal coordinate selector functions.
 */
@SuppressWarnings("ALL")
@ThreadSafeMapper
public class PolygonOnGrid implements LongFunction<Polygon> {

    private final double rows;
    private final double columns;
    private final double x_topleft;
    private final double y_topleft;
    private final double x_bottomright;
    private final double y_bottomright;
    private final Uniform rowfunc;
    private final Uniform colfunc;
    private final double xwidth;
    private final double yheight;

    @Example({"PolygonOnGrid(1, 11, 11, 1, 10, 10)","Create a 10x10 grid with cells 1x1, spaced one off the y=0 and x=0 axes"})
    public PolygonOnGrid(double x_topleft, double y_topleft, double x_bottomright, double y_bottomright, int rows, int columns) {

        if (x_topleft>=x_bottomright) {
            throw new RuntimeException("x_topleft should be less than x_bottomright");
        }
        if (y_topleft<=y_bottomright) {
            throw new RuntimeException("y_topleft should be more than y_bottomright");
        }

        this.x_topleft = x_topleft;
        this.y_topleft = y_topleft;
        this.x_bottomright = x_bottomright;
        this.y_bottomright = y_bottomright;

        this.rows = rows;
        this.columns = columns;

        this.xwidth = (x_bottomright-x_topleft) / columns;
        this.yheight = (y_topleft-y_bottomright) / columns;

        this.rowfunc = new Uniform(0, rows - 1);
        this.colfunc = new Uniform(0,columns-1);
    }

    @Override
    public Polygon apply(long value) {
        int row = rowfunc.applyAsInt(value);
        int column = colfunc.applyAsInt(value+33);

        double left=x_topleft + (column*xwidth);
        double top =y_topleft - (row*yheight);
        double right = left+xwidth;
        double bottom = top - yheight;

        Polygon polygon = new Polygon(
                new Point(left, bottom),
                new Point(left, top),
                new Point(right, top),
                new Point(right, bottom)
        );

        return polygon;

    }
}
