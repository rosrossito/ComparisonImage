package ua.images.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ua.images.domain.Area;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Created by Àäìèíèñòðàòîð on 15.10.2017.
 */
public class ImageProcessing {

    protected Log log = LogFactory.getLog(getClass());
    private String file = "src/main/resources/image1.png";
    private String file1 = "src/main/resources/image2.png";
    private int width1;
    private int width2;
    private int height1;
    private int height2;
    final private int minDistBetweenArea = 2;
    private int[][] pict;
    private ArrayList<Area> areaArr = new ArrayList<Area>();
    private ArrayList<Area> expandedAreaArr = new ArrayList<Area>();
    private ArrayList<Area> finalAreaArr = new ArrayList<Area>();

    public BufferedImage processImage(String file, String file1) {

        BufferedImage img1 = null;
        BufferedImage img2 = null;

        try {
            img1 = ImageIO.read(new File(file));
            img2 = ImageIO.read(new File(file1));
        } catch (IOException e) {
        }
        width1 = img1.getWidth();
        width2 = img2.getWidth();
        height1 = img1.getHeight();
        height2 = img2.getHeight();
        pict = new int[height1][width1];

        // output image
        BufferedImage outImg = new BufferedImage(width1, height1, BufferedImage.TYPE_INT_RGB);

        try {
            if ((width1 != width2) || (height1 != height2)) {
                throw new Exception();
            }
        } catch (Exception e) {
            log.info("Error: Images dimensions mismatch");
            e.printStackTrace();
        }


        // Convert to int (pixels)
        float diff;
        log.info("Converting to pixels...");
        for (int i = 0; i < height1; i++) {
            for (int j = 0; j < width1; j++) {
                int rgb1 = img1.getRGB(j, i);
                int rgb2 = img2.getRGB(j, i);
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = (rgb1) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = (rgb2) & 0xff;

                diff = (Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2)) / 3;

                // result is between 0 - 255, check %difference
                if (diff / 255.0 < 0.1) {
                    pict[i][j] = 0;
                } else {
                    pict[i][j] = -1;
                }
                outImg.setRGB(j, i, rgb2);
            }
        }

        log.info("Search rectangles...");
        searchRect();
        expandArea();
        filledExpandedArea();

        log.info("Merge nearby areas...");

        buildExpandedArea();

        log.info("Draw and save result...");
        //draw rectangles
        return drawR(outImg);
    }

    ArrayList<ArrayList<Integer>> coordinates = new ArrayList<ArrayList<Integer>>();

    void filledExpandedArea() {
        for (Area a : expandedAreaArr) {
            //filled expanded area by -2
            for (int i = a.getLeftUpCornerY(); i <= a.getRightDownCornerY(); i++) {
                for (int j = a.getLeftUpCornerX(); j <= a.getRightDownCornerX(); j++) {
                    pict[i][j] = -2;
                    ArrayList<Integer> coord = new ArrayList<Integer>();
                    coord.add(j);
                    coord.add(i);
                    coordinates.add(coord);
                    coordinates.sort(new Comparator<ArrayList<Integer>>() {
                        public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
                            if (o1.get(0) == o2.get(0)) return 0;
                            else if (o1.get(0) > o2.get(0)) return 1;
                            else return -1;
                        }
                    });
                }
            }
        }
    }

    void buildExpandedArea() {
        counterArea = 0;
        Area area = new Area();
        counterArea++;
        area.setLeftUpCornerX(coordinates.get(0).get(0));
        area.setAreaNumber(counterArea);
        area.setLeftUpCornerY(height1);
        area.setRightDownCornerY(0);

        for (int z =0; z < coordinates.size()-1; z++){

            area.setLeftUpCornerY(Math.min(coordinates.get(z).get(1), area.getLeftUpCornerY()));
            area.setRightDownCornerY(Math.max(coordinates.get(z).get(1), area.getRightDownCornerY()));

            if (coordinates.get(z+1).get(0)-coordinates.get(z).get(0)<=1){
                area.setRightDownCornerX(coordinates.get(z+1).get(0));
                area.setLeftUpCornerY(Math.min(coordinates.get(z+1).get(1), area.getLeftUpCornerY()));
                area.setRightDownCornerY(Math.max(coordinates.get(z+1).get(1), area.getRightDownCornerY()));
            }else {
                finalAreaArr.add(area);
                area = new Area();
                counterArea++;
                area.setLeftUpCornerX(coordinates.get(z+1).get(0));
                area.setAreaNumber(counterArea);
                area.setLeftUpCornerY(height1);
                area.setRightDownCornerY(0);
            }
        }
        finalAreaArr.add(area);
    }

    private void expandArea() {
        for (Area a : areaArr) {
            Area area = new Area(a);
            area.setRightDownCornerY(a.getRightDownCornerY() + minDistBetweenArea);
            area.setRightDownCornerX(a.getRightDownCornerX() + minDistBetweenArea);
            area.setLeftUpCornerX(a.getLeftUpCornerX() - minDistBetweenArea);
            area.setLeftUpCornerY(a.getLeftUpCornerY() - minDistBetweenArea);
            expandedAreaArr.add(area);
        }
    }


    private HashSet<HashSet<Integer>> numberMergedAreasArr = new HashSet<HashSet<Integer>>();


    int counterArea = 0;
    int i;
    int j;

    private void searchRect() {
        for (i = 0; i < height1; i++) {
            for (j = 0; j < width1; j++) {
                if (pict[i][j] == -1) {
                    //build area
                    Area area = new Area();
                    area.setAreaNumber(counterArea);
                    counterArea++;
                    area.setLeftUpCornerX(j);
                    area.setLeftUpCornerY(i);
                    area.setRightDownCornerX(j);
                    area.setRightDownCornerY(i);
                    pict[i][j] = counterArea;
                    buildArea(i, j, area);
                    areaArr.add(area);
                }
            }
        }
    }


    public void buildArea(int raw, int column, Area area) {

        //if (pict[raw][column] >= 0) {
        //   flag = false;
        //   return;
        //} else if (checking(raw, column)) {

        if (checking(raw, column)) {
            ArrayList<ArrayList<Integer>> seq = new ArrayList();
            ArrayList<Integer> rightNeighbour = new ArrayList<Integer>();
            ArrayList<Integer> leftNeighbour = new ArrayList<Integer>();
            ArrayList<Integer> downNeighbour = new ArrayList<Integer>();
            ArrayList<Integer> upNeighbour = new ArrayList<Integer>();
            ArrayList<Integer> diagUpLeftNeighbour = new ArrayList<Integer>();
            ArrayList<Integer> diagDownLeftNeighbour = new ArrayList<Integer>();
            ArrayList<Integer> diagUpRightNeighbour = new ArrayList<Integer>();
            ArrayList<Integer> diagDownRightNeighbour = new ArrayList<Integer>();

            rightNeighbour.add(raw);
            rightNeighbour.add(column + 1);
            leftNeighbour.add(raw);
            leftNeighbour.add(column - 1);
            downNeighbour.add(raw + 1);
            downNeighbour.add(column);
            upNeighbour.add(raw - 1);
            upNeighbour.add(column);
            diagUpLeftNeighbour.add(raw - 1);
            diagUpLeftNeighbour.add(column - 1);
            diagDownLeftNeighbour.add(raw + 1);
            diagDownLeftNeighbour.add(column - 1);
            diagUpRightNeighbour.add(raw - 1);
            diagUpRightNeighbour.add(column + 1);
            diagDownRightNeighbour.add(raw + 1);
            diagDownRightNeighbour.add(column + 1);

            seq.add(rightNeighbour);
            seq.add(leftNeighbour);
            seq.add(downNeighbour);
            seq.add(upNeighbour);
            seq.add(diagUpLeftNeighbour);
            seq.add(diagDownLeftNeighbour);
            seq.add(diagUpRightNeighbour);
            seq.add(diagDownRightNeighbour);

            for (int k = 0; k < seq.size(); ++k) {
                //expand area
                if (pict[seq.get(k).get(0)][seq.get(k).get(1)] == -1) {
                    pict[seq.get(k).get(0)][seq.get(k).get(1)] = counterArea;
                    //expand corners
                    //left upper corner
                    area.setLeftUpCornerX(Math.min(seq.get(k).get(1), area.getLeftUpCornerX()));
                    area.setLeftUpCornerY(Math.min(seq.get(k).get(0), area.getLeftUpCornerY()));
                    //right down corner
                    area.setRightDownCornerX(Math.max(seq.get(k).get(1), area.getRightDownCornerX()));
                    area.setRightDownCornerY(Math.max(seq.get(k).get(0), area.getRightDownCornerY()));

                    //search neighbours
                    buildArea(seq.get(k).get(0), seq.get(k).get(1), area);
                }
            }
            seq.clear();
        }
    }

    public boolean checking(int raw, int column) {
        return pict[raw + 1][column] == -1 || pict[raw + 1][column + 1] == -1 || pict[raw][column + 1] == -1 ||
                pict[raw - 1][column + 1] == -1 || pict[raw - 1][column] == -1 || pict[raw - 1][column - 1] == -1 ||
                pict[raw + 1][column - 1] == -1 || pict[raw + 1][column - 1] == -1;
    }


    private BufferedImage drawR(BufferedImage outImg) {

        for (Area a : finalAreaArr) {
            //upper reñtangle side
            for (int j = a.getLeftUpCornerX(); j < a.getRightDownCornerX(); j++) {
                outImg.setRGB(j, a.getLeftUpCornerY(),  Color.red.getRGB());
            }
            //right reñtangle side
            for (int j = a.getLeftUpCornerY(); j < a.getRightDownCornerY(); j++) {
                outImg.setRGB(a.getRightDownCornerX(),j, Color.red.getRGB());
            }
            //down reñtangle side
            for (int j = a.getLeftUpCornerX(); j < a.getRightDownCornerX(); j++) {
                outImg.setRGB(j, a.getRightDownCornerY(),Color.red.getRGB());
            }
            //left reñtangle side
            for (int j = a.getLeftUpCornerY(); j < a.getRightDownCornerY(); j++) {
                outImg.setRGB(a.getLeftUpCornerX(), j, Color.red.getRGB());
            }
        }
        return outImg;
    }

    public void draw() {
        try {
            ImageIO.write(processImage(file, file1), "png", new File("output.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

