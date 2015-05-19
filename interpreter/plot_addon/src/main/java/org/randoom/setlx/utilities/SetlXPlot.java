package org.randoom.setlx.utilities;

import java.util.List;

/**
 * This Interface defines how you can call your plotlibrary from setlX
 */
public interface SetlXPlot {

    /**
     * creates a canvas to draw on
     *
     * @return
     */
    public Canvas createCanvas();


    public Canvas createCanvas(String titel);
    /**
     * adds a function as graph to the given canvas
     *
     * @param canvas
     * @param function
     * @return Graph object, identifier of the graph
     */
    public Graph addGraph(Canvas canvas, String function);

    /**
     * adds a function as graph to the given canvas and give it a name
     *
     * @param canvas
     * @param function
     * @param name     of the graphe
     * @return identifier of the graph
     */
    public Graph addGraph(Canvas canvas, String function, String name);

    /**
     * adds a function as graph to the given canvas and plots the area to the y-axis (integral) if plotArea ist true
     *
     * @param canvas
     * @param function
     * @param plotArea draw integral if true
     * @param color
     * @return
     */
    public Graph addGraph(Canvas canvas, String function, boolean plotArea, List<Integer> color);

    /**
     * adds a function as graph to the canvas, give it a name and plots the integral if plotArea is true
     *
     * @param canvas
     * @param function
     * @param name
     * @param plotArea
     * @param color
     * @return
     */
    public Graph addGraph(Canvas canvas, String function, String name, boolean plotArea, List<Integer> color);

    /**
     * adds a graph consisting of single points
     *
     * @param canvas
     * @param function list of points to be plotted
     * @return
     */
    public Graph addListGraph(Canvas canvas, List<List<Double>> function);

    /**
     * adds a graph consisting of single points and a name for the graph
     *
     * @param canvas
     * @param function list of points to be plotted
     * @param name
     * @param color
     * @return
     */
    public Graph addListGraph(Canvas canvas, List<List<Double>> function, String name, List<Integer> color);

    public Graph addListGraph(Canvas canvas, List<List<Double>> function, String name, boolean plotArea, List<Integer> color);

    public Graph addParamGraph(Canvas canvas, String xfunction, String yfunction);

    public Graph addParamGraph(Canvas canvas, String xfunction, String yfunction, String name, List<Integer> color);

    public Graph addParamGraph(Canvas canvas, String xfunction, String yfunction, Boolean plotArea, List<Integer> color);

    public Graph addParamGraph(Canvas canvas, String xfunction, String yfunction, String name, Boolean plotArea, List<Integer> color);

    public Graph addChart(Canvas canvas, String chartType, List values);

    public Graph addChart(Canvas canvas, String chartType, List values, String name);

    public void removeGraph(Canvas canvas, Graph graph);

    public void insertLabel(Canvas canvas, String xLabel, String yLabel);

    public void defineTitle(Canvas canvas, String title);

    /**
     * defines if the legend describing the graphs is visible or not
     *
     * @param canvas
     * @param visible
     */
    public void legendVisible(Canvas canvas, Boolean visible);

    /**
     * sets the shown area of the graph to the given min and max values for x and y axis
     *
     * @param canvas
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     */
    public void modScale(Canvas canvas, double xMin, double xMax, double yMin, double yMax);

    /**
     * exports the canvas as image file to the path
     *
     * @param canvas
     * @param path
     */
    public void exportCanvas(Canvas canvas, String path);

    /**
     * modulate the type (eq. linear or logarithmic) of the axis
     *
     * @param canvas
     * @param xType
     * @param yType
     */
    public void modScaleType(Canvas canvas, String xType, String yType);

    /**
     * add a single bullet to the canvas at the given x and y coordinates
     * @param canvas
     * @param x
     * @param y
     * @param color
     */
    public void addBullet(Canvas canvas, double x, double y, List<Integer> color);
}
