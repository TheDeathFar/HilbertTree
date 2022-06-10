package ru.vsu.css.vorobcov_i_a;

import java.util.ArrayList;

import ru.vsu.css.vorobcov_i_a.types.Point;
import ru.vsu.css.vorobcov_i_a.types.RTreeRectangle;
import ru.vsu.css.vorobcov_i_a.utils.Pair;

public class LinearSplitter<T extends Point> extends NodeSplitter<T> {

    private int min_num_records;

    public LinearSplitter(int min_num_records) {
        this.min_num_records = min_num_records;
    }

    protected Pair<RTreeRectangle<T>, RTreeRectangle<T>> pickSeeds(ArrayList<RTreeRectangle<T>> records) {
        Pair<RTreeRectangle<T>, RTreeRectangle<T>> NewNodes;

        RTreeRectangle<T> E = records.get(0);
        Rectangle EMbr = E.getMbr();
        double lowest = EMbr.lowest();
        double highest = EMbr.highest();
        RTreeRectangle<T> EH = E, EL = E;
        for (int i = 1; i < records.size(); i++) {
            RTreeRectangle<T> Ei = records.get(i);
            Rectangle EMbri = Ei.getMbr();
            double lowxy = EMbri.lowest();
            double highxy = EMbri.highest();
            // check whether the current rectangle is higher or lower than the highest and
            // lowest
            // rectangle we have so far
            if (lowxy < lowest) {
                lowest = lowxy;
                EL = Ei;
            }
            if (highxy > highest) {
                highest = highxy;
                EH = Ei;
            }
        }
        NewNodes = new Pair<RTreeRectangle<T>, RTreeRectangle<T>>(EL, EH);

        return NewNodes;
    }

    protected void pickNext(ArrayList<RTreeRectangle<T>> records, RTreeRectangle<T> L1, RTreeRectangle<T> L2) {
        RTreeRectangle<T> chosenEntry = null;
        double maxDifference = 0;
        // get the max difference between area enlargments
        for (RTreeRectangle<T> entry : records) {
            Rectangle entryMbr = entry.getMbr();
            double enlargementL1 = L1.getMbr().calculateEnlargement(entryMbr);
            double enlargementL2 = L2.getMbr().calculateEnlargement(entryMbr);
            double maxEnlargementDifference = Math.abs(enlargementL1 - enlargementL2);
            if (maxEnlargementDifference >= maxDifference) {
                chosenEntry = entry;
                maxDifference = maxEnlargementDifference;
            }
        }
        // selecting group to which we add the selected entry
        this.resolveTies(L1, L2, chosenEntry);
        // remove chosenRecord from records
        records.remove(chosenEntry);
    }


    private void resolveTies(RTreeRectangle<T> L1, RTreeRectangle<T> L2, RTreeRectangle<T> chosenEntry) {
        double enlargementL1 = L1.getMbr().calculateEnlargement(chosenEntry.getMbr());
        double enlargementL2 = L2.getMbr().calculateEnlargement(chosenEntry.getMbr());
        if (enlargementL1 == enlargementL2) {
            // select group with min area
            double area1 = L1.getMbr().getArea();
            double area2 = L2.getMbr().getArea();
            if (area1 == area2) {
                int numEntries1 = L1.numChildren();
                int numEntries2 = L2.numChildren();
                // if it's still equal, resolve by default to L1
                if (numEntries1 <= numEntries2) {
                    L1.add(chosenEntry);
                } else {
                    L2.add(chosenEntry);
                }
            } else if (area1 < area1) {
                L1.add(chosenEntry);
            } else {
                L2.add(chosenEntry);
            }
        } else if (enlargementL1 < enlargementL2) {
            L1.add(chosenEntry);
        } else {
            L2.add(chosenEntry);
        }
    }

    public Pair<RTreeRectangle<T>, RTreeRectangle<T>> splitNodes(RTreeRectangle<T> nodeToSplit, RTreeRectangle<T> overflowNode) {
        // create a set of entries mbr
        ArrayList<RTreeRectangle<T>> records = new ArrayList<RTreeRectangle<T>>();
        for (RTreeRectangle<T> childRecord : nodeToSplit.getChildren()) {
            records.add(childRecord);
        }
        records.add(overflowNode);
        // find the 2 nodes that maximizes the space waste, and assign them to a node
        Pair<RTreeRectangle<T>, RTreeRectangle<T>> seeds = this.pickSeeds(records);
        RTreeRectangle<T> L1 = new RTreeRectangle<T>(seeds.getFirst());
        RTreeRectangle<T> L2 = new RTreeRectangle<T>(seeds.getSecond());
        records.remove(seeds.getFirst());
        records.remove(seeds.getSecond());
        // examine remaining entries and add them to either L1 or L2 with the least
        // enlargement criteria
        while (records.size() > 0) {
            // if one node must take all remaining entries, assign them with no criteria
            if (L1.numChildren() + records.size() == this.min_num_records) {
                L1.addChildren(records);
                break;
            }
            if (L2.numChildren() + records.size() == this.min_num_records) {
                L2.addChildren(records);
                break;
            }
            // add the next record to the node which will require the least enlargement
            this.pickNext(records, L1, L2);
        }
        return new Pair<RTreeRectangle<T>, RTreeRectangle<T>>(L1, L2);
    }
}
