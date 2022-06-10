package ru.vsu.css.vorobcov_i_a.types;


import ru.vsu.css.vorobcov_i_a.Rectangle;

public class Record <T extends Point> extends Rectangle {
    private final T value;

    public Record(double width, double height, double x, double y, T value) {
        super(width, height, x, y);
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Record<T> r = (Record<T>) obj;
        return r.getValue().equals(this.value);
    }

}
