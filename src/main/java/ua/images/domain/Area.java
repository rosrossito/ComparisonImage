package ua.images.domain;

/**
 * Created by Администратор on 15.10.2017.
 */
public class Area {
    private int areaNumber;
    private int leftUpCornerX;
    private int leftUpCornerY;
    private int RightDownCornerX;
    private int RightDownCornerY;

    public Area() {
    }

    public Area(Area area) {
        this.areaNumber = area.areaNumber;
        this.leftUpCornerX = area.leftUpCornerX;
        this.leftUpCornerY = area.leftUpCornerY;
        this.RightDownCornerX = area.RightDownCornerX;
        this.RightDownCornerY = area.RightDownCornerY;
    }

    public int getAreaNumber() {
        return areaNumber;
    }

    public void setAreaNumber(int areaNumber) {
        this.areaNumber = areaNumber;
    }

    public int getLeftUpCornerX() {
        return leftUpCornerX;
    }

    public void setLeftUpCornerX(int leftUpCornerX) {
        this.leftUpCornerX = leftUpCornerX;
    }

    public int getLeftUpCornerY() {
        return leftUpCornerY;
    }

    public void setLeftUpCornerY(int leftUpCornerY) {
        this.leftUpCornerY = leftUpCornerY;
    }

    public int getRightDownCornerX() {
        return RightDownCornerX;
    }

    public void setRightDownCornerX(int rightDownCornerX) {
        RightDownCornerX = rightDownCornerX;
    }

    public int getRightDownCornerY() {
        return RightDownCornerY;
    }

    public void setRightDownCornerY(int rightDownCornerY) {
        RightDownCornerY = rightDownCornerY;
    }
}
