package ru.vsu.css.vorobcov_i_a.types;

import ru.vsu.css.vorobcov_i_a.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class RTreeRectangle <T extends Point>{

    public List<RTreeRectangle<T>> children;
    public RTreeRectangle<T> parent;
    public Record<T> record;
    private Rectangle mbr;

    public RTreeRectangle() {
        this.children = new ArrayList<RTreeRectangle<T>>();
    }

    public RTreeRectangle(RTreeRectangle<T> child) {
        this.children = new ArrayList<RTreeRectangle<T>>();
        this.add(child);
        this.mbr = child.getMbr();
    }

    public RTreeRectangle(Record<T> record) {
        this.record = record;
        this.mbr = record.getMbr();
    }

    public Rectangle getMbr() {
        return this.mbr;
    }
    private void setMbr(Rectangle value) {
        this.mbr = value;
    }

    public RTreeRectangle<T> getParent() {
        return this.parent;
    }
    public void setParent(RTreeRectangle node) {
        this.parent = node;
    }

    public List<RTreeRectangle<T>> getChildren() {
        return this.children;
    }

    public Record<T> getRecord() {
        return this.record;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public int numChildren() {
        if (this.children != null)
            return this.children.size();
        return 0;
    }

    public boolean isLeaf() {
        if (this.children == null || this.children.size() == 0) {
            return true;
        }
        RTreeRectangle<T> leafTest = this.children.get(0);
        if (leafTest.getRecord() != null) {
            return true;
        }
        return false;
    }

    public void remove(RTreeRectangle<T> node) {
        this.children.remove(node);
        node.setParent(null);
        this.updateMbr(null);
    }

    public void add(RTreeRectangle<T> node) {
        this.children.add(node);
        node.setParent(this);
    }

    public void addChildren(List<RTreeRectangle<T>> nodes) {
        for (RTreeRectangle<T> node : nodes) {
            this.add(node);
        }
    }

    @Override
    public String toString() {
        return this.toString("");
    }

    public String toString(String padding) {
        StringBuilder strBuilder = new StringBuilder();
        if (this.record != null) {
            strBuilder.append("record=" + this.record.toString() + ")");
        } else {
            for (RTreeRectangle<T> child : this.children) {
                strBuilder.append(child.toString(padding + "  "));
            }
        }
        strBuilder.append("\n");
        return strBuilder.toString();
    }

    public void updateMbr(Rectangle childMbrChange) {
        // if there is already a minimum bounding rectangle
        if (this.mbr != null) {
            Rectangle enclosing = null;
            if (childMbrChange != null)
                enclosing = Rectangle.buildRectangle(this.mbr, childMbrChange);
            else {
                // traverse all childs
                for (RTreeRectangle<T> child : this.children) {
                    if (enclosing == null) {
                        enclosing = child.getMbr();
                    } else {
                        enclosing = Rectangle.buildRectangle(enclosing, child.getMbr());
                    }
                }
            }
            if (enclosing != this.mbr) {
                this.setMbr(enclosing);
            }
        } else {
            this.setMbr(childMbrChange);
        }
    }
}
