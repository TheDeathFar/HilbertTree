package ru.vsu.css.vorobcov_i_a;


import ru.vsu.css.vorobcov_i_a.types.Point;
import ru.vsu.css.vorobcov_i_a.types.RTreeRectangle;
import ru.vsu.css.vorobcov_i_a.types.Record;
import ru.vsu.css.vorobcov_i_a.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class RTree<T extends Point> {
    private final static int DEFAULT_MIN_CHILDREN = 2;
    private final static int DEFAULT_MAX_CHILDREN = 4;

    private RTreeRectangle<T> rootNode;
    protected int min_num_records;
    protected int max_num_records;
    protected int num_entries;
    protected NodeSplitter<T> splitter;

    public RTree (){
        this.num_entries = 0;
        this.min_num_records = DEFAULT_MIN_CHILDREN;
        this.max_num_records = DEFAULT_MAX_CHILDREN;
        this.splitter = new LinearSplitter<T>(DEFAULT_MIN_CHILDREN);
    }

    public RTree(int min_num_records, int max_num_records, NodeSplitter<T> splitter) {
        this.num_entries = 0;
        this.min_num_records = min_num_records;
        this.max_num_records = max_num_records;
        this.rootNode = new RTreeRectangle<T>();
        this.rootNode = new RTreeRectangle<T>();
        this.splitter = splitter;
    }

    public RTreeRectangle<T> getRoot() {
        return rootNode;
    }

    public void insert(Record<T> record) {
        Rectangle recordMbr = record.getMbr();
        // choose leaf that needs the least enlargement with mbr
        RTreeRectangle<T> leaf = this.chooseLeaf(recordMbr, this.rootNode);
        RTreeRectangle<T> newNode = new RTreeRectangle<T>(record);
        // if node has enough space to insert the child
        if (leaf.numChildren() < this.max_num_records) {
            leaf.add(newNode);
            this.adjustTree(leaf, null);
        } else {
            this.splitNodeAndReassign(leaf, newNode);
        }
        this.num_entries += 1;
    }

    public void insertLeaf(List<Record<T>> records) {
        for (Record<T> record : records) {
            this.insert(record);
        }
    }

    private RTreeRectangle<T> chooseLeaf(Rectangle recordMbr, RTreeRectangle<T> R) {
        RTreeRectangle<T> current = this.rootNode;
        while (!current.isLeaf()) {
            ArrayList<RTreeRectangle<T>> minEnlargedRecords = this.getMinEnlargedRecords(current, recordMbr);
            if (minEnlargedRecords.size() == 1)
                current = minEnlargedRecords.get(0);
            else {
                // resolve ties if any, by choosing the node with least mbr's area
                current = this.getMinAreaRecord(minEnlargedRecords);
            }
        }
        return current;
    }

    private RTreeRectangle<T> getMinAreaRecord(ArrayList<RTreeRectangle<T>> nodes) {
        RTreeRectangle<T> minAreaRecord = null;
        double minArea = Float.MAX_VALUE;
        for (RTreeRectangle<T> node : nodes) {
            double area = node.getMbr().getArea();
            if (area < minArea) {
                minAreaRecord = node;
                minArea = area;
            }
        }
        return minAreaRecord;
    }

    private ArrayList<RTreeRectangle<T>> getMinEnlargedRecords(RTreeRectangle<T> current, Rectangle recordMbr) {
        double minEnlargement = Float.MAX_VALUE;
        ArrayList<RTreeRectangle<T>> minEnlargedRecords = new ArrayList<RTreeRectangle<T>>();
        // choose record which mbr's enlarge the less with current record's mbr
        for (RTreeRectangle<T> child : current.getChildren()) {
            Rectangle childMbr = child.getMbr();
            double enlargement = childMbr.calculateEnlargement(recordMbr);
            if (enlargement == minEnlargement || minEnlargedRecords.size() == 0) {
                minEnlargedRecords.add(child);
                minEnlargement = enlargement;
            } else if (enlargement < minEnlargement) {
                minEnlargedRecords = new ArrayList<RTreeRectangle<T>>();
                minEnlargedRecords.add(child);
                minEnlargement = enlargement;
            }
        }
        return minEnlargedRecords;
    }

    private void splitNodeAndReassign(RTreeRectangle<T> nodeToSplit, RTreeRectangle<T> overflowNode) {
        Pair<RTreeRectangle<T>, RTreeRectangle<T>> splittedNodes = this.splitter.splitNodes(nodeToSplit, overflowNode);
        RTreeRectangle<T> splittedLeft = splittedNodes.getFirst(), splittedRight = splittedNodes.getSecond();
        if (nodeToSplit == this.rootNode)
            this.assignNewRoot(splittedLeft, splittedRight);
        else {
            RTreeRectangle<T> splittedParent = nodeToSplit.getParent();
            splittedParent.remove(nodeToSplit);
            splittedParent.add(splittedLeft);
            splittedParent.add(splittedRight);
            this.adjustTree(splittedLeft, splittedRight);
        }
    }

    protected void assignNewRoot(RTreeRectangle<T> child1, RTreeRectangle<T> child2) {
        RTreeRectangle<T> newRoot = new RTreeRectangle<T>();
        newRoot.add(child1);
        newRoot.add(child2);
        this.rootNode = newRoot;
    }

    private void adjustTree(RTreeRectangle<T> node, RTreeRectangle<T> createdNode) {
        RTreeRectangle<T> previousNode = node;
        // node resulting from split
        RTreeRectangle<T> splittedNode = createdNode;
        // while we do not reach root
        while (!previousNode.isRoot()) {
            RTreeRectangle<T> previousParent = previousNode.getParent();
            // updating parent recursively in the no-split case
            if (splittedNode == null) {
                previousParent.updateMbr(previousNode.getMbr());
            }
            // see if there is a node overflow, and update accordingly
            else if (previousParent.numChildren() > this.max_num_records) {
                previousParent.remove(splittedNode);
                this.splitNodeAndReassign(previousParent, splittedNode);
            }
            previousNode = previousParent;
        }
    }

    public boolean delete(Record<T> record) {
        // choose leaf that contains record
        RTreeRectangle<T> leaf = this.findLeafAndRemove(record, this.rootNode);
        if (leaf == null)
            return false;
        this.condenseTree(leaf);
        // if root needs to be reassigned
        if (this.rootNode.numChildren() == 1 && !this.rootNode.isLeaf()) {
            RTreeRectangle<T> newRoot = this.rootNode.getChildren().get(0);
            newRoot.setParent(null);
            this.rootNode = newRoot;
        }
        this.num_entries -= 1;
        return true;
    }

    public List<Boolean> deleteRes(List<Record<T>> records) {
        List<Boolean> deletedRecords = new ArrayList<Boolean>();
        for (Record<T> record : records) {
            deletedRecords.add(this.delete(record));
        }
        return deletedRecords;
    }

    private RTreeRectangle<T> findLeafAndRemove(Record<T> record, RTreeRectangle<T> node) {
        if (!node.isLeaf()) {
            // perform DFS of child nodes
            for (RTreeRectangle<T> child : node.getChildren()) {
                if (child.getMbr().containedBy(record.getMbr())) {
                    RTreeRectangle<T> foundLeaf = this.findLeafAndRemove(record, child);
                    if (foundLeaf != null) {
                        return foundLeaf;
                    }
                }
            }
        } else {
            for (RTreeRectangle<T> child : node.getChildren()) {
                Record<T> childRecord = child.getRecord();
                if (childRecord.equals(record)) {
                    node.remove(child);
                    return node;
                }
            }
        }
        return null;
    }

    private void condenseTree(RTreeRectangle<T> leaf) {
        RTreeRectangle<T> N = leaf;
        ArrayList<RTreeRectangle<T>> removedEntries = new ArrayList<RTreeRectangle<T>>();
        if (!N.isRoot()) {
            RTreeRectangle<T> P = N.getParent();
            // N has underflow of childs
            if (N.numChildren() < this.min_num_records) {
                P.remove(N);
                // we will reinsert remaining entries if they have at least one child
                if (N.numChildren() > 0)
                    removedEntries.add(N);
            } else {
                N.updateMbr(null);
            }
            // update parent recursively
            this.condenseTree(P);
        }
        // reinsert temporarily deleted entries
        for (RTreeRectangle<T> deletedChild : removedEntries) {
            this.insertFromNode(deletedChild);
        }
    }

    private void insertFromNode(RTreeRectangle<T> node) {
        if (node.getChildren() != null) {
            for (RTreeRectangle<T> child : node.getChildren()) {
                if (child.getRecord() != null)
                    this.insert(child.getRecord());
                else
                    this.insertFromNode(child);
            }
        }
    }

    public Record<T> search(Record<T> record) {
        Rectangle recordMbr = record.getMbr();
        Record<T> result = null;
        // init stack for dfs in valid childs
        Stack<RTreeRectangle<T>> validNodes = new Stack<RTreeRectangle<T>>();
        validNodes.push(this.rootNode);
        // traverse whole tree
        while (!validNodes.empty() && result == null) {
            RTreeRectangle<T> currentNode = validNodes.pop();
            for (RTreeRectangle<T> child : currentNode.getChildren()) {
                // record node
                Record<T> childRecord = child.getRecord();
                if (childRecord != null) {
                    if (childRecord.equals(record)) {
                        result = childRecord;
                        break;
                    }
                } else if (child.getMbr().containedBy(recordMbr)) {
                    validNodes.push(child);
                }
            }
        }
        return result;
    }

    public List<Record<T>> searchRes(List<Record<T>> records) {
        List<Record<T>> searchResults = new ArrayList<Record<T>>();
        for (Record<T> searchRecord : records) {
            searchResults.add(this.search(searchRecord));
        }
        return searchResults;
    }


}
