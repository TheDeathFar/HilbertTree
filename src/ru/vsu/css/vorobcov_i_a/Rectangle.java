package ru.vsu.css.vorobcov_i_a;

import ru.vsu.css.vorobcov_i_a.types.Geometry;
import ru.vsu.css.vorobcov_i_a.types.Point;

public class Rectangle implements Geometry{

    private double x;
    private double width;
    private double y;
    private double height;


    public Rectangle(double width, double height, double x, double y) {
        this.width = width ;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    public double lowest() {
        return (x+width) + this.y;
    }

    public double highest() {
        return (y-height) + this.x;
    }

    public Rectangle getMbr() {
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }


    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public boolean overlaps(Rectangle boundingBox){
        return (this.x <= boundingBox.getX() + boundingBox.getWidth() && this.x + this.width >= boundingBox.getX()) &&
                this.y + this.height >= boundingBox.getY() && boundingBox.getY() + boundingBox.getHeight() >= this.y;
    }


    public boolean containedBy(Rectangle boundingBox){
        return this.x >= boundingBox.getX() && this.x + this.width <= boundingBox.getX() + boundingBox.getWidth() && this.y >= boundingBox.getY() && this.y + this.height <= boundingBox.getY() + boundingBox.getHeight();
    }

    public void growRectangleToFit(Rectangle boundingBox) {
        this.height = Math.max(this.y + this.height, boundingBox.getY() + boundingBox.getHeight()) - Math.min(this.y, boundingBox.getY());
        this.width = Math.max(this.x + this.width, boundingBox.getX() + boundingBox.getWidth()) - Math.min(this.x, boundingBox.getX());
        this.x = Math.min(this.x, boundingBox.getX());
        this.y = Math.min(this.y, boundingBox.getY());
    }

    public static Rectangle buildRectangle(Rectangle boundingBox1, Rectangle boundingBox2) {
        double maxYCoordinate = Math.max(boundingBox1.getY() + boundingBox1.getHeight(), boundingBox2.getY() + boundingBox2.getHeight());
        double minYCoordinate = Math.min(boundingBox1.getY(), boundingBox2.getY());
        double maxXCoordinate = Math.max(boundingBox1.getX() + boundingBox1.getWidth(), boundingBox2.getX() + boundingBox2.getWidth());
        double minXCoordinate = Math.min(boundingBox1.getX(), boundingBox2.getX());
        return new Rectangle(minXCoordinate, maxXCoordinate, minYCoordinate , maxYCoordinate);
    }

    public double calculateEnlargement(Rectangle boundingBox) {
        Rectangle overlappingRectangle = Rectangle.buildRectangle(this, boundingBox);
        return overlappingRectangle.getArea() - this.getArea();
    }

    public double getArea(){
        return this.height * this.width;
    }

    public Point getCenter() {
        double centerX = this.x + (this.width / 2);
        double centerY=  this.y + (this.height / 2);
        return new Point(centerX, centerY);
    }
}
