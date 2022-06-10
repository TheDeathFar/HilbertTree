package ru.vsu.css.vorobcov_i_a;

import ru.vsu.css.vorobcov_i_a.types.Point;
import ru.vsu.css.vorobcov_i_a.types.RTreeRectangle;
import ru.vsu.css.vorobcov_i_a.utils.Pair;

import java.util.ArrayList;

public abstract class NodeSplitter<T extends Point> {

    abstract public Pair<RTreeRectangle<T>, RTreeRectangle<T>> splitNodes(RTreeRectangle<T> nodeToSplit, RTreeRectangle<T> overflowNode);

    abstract protected Pair<RTreeRectangle<T>, RTreeRectangle<T>> pickSeeds(ArrayList<RTreeRectangle<T>> records);

    abstract protected void pickNext(ArrayList<RTreeRectangle<T>> records, RTreeRectangle<T> L1, RTreeRectangle<T> L2);
}
