package com.gpstracker.data_clases;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by RGarai on 24.8.2016.
 */

/*
 * list of fake data for past runs
 */

public class RunData {
    public static List<MyRun> previousRuns = Arrays.asList(
            new MyRun(new Date(1472728271000L), 2, 2),
            new MyRun(new Date(1472742671000L), 2, 2),
            new MyRun(new Date(1472829071000L), 2, 2),
            new MyRun(new Date(1472832671000L), 2, 2),

            new MyRun(new Date(1469171207000L), 12, 2),
            new MyRun(new Date(1469171217000L), 13, 4),
            new MyRun(new Date(1469171227000L), 10, 2),
            new MyRun(new Date(1469171237000L), 11, 2),
            new MyRun(new Date(1469171247000L), 12, 2),
            new MyRun(new Date(1469171257000L), 13, 4),
            new MyRun(new Date(1469171267000L), 10, 2),
            new MyRun(new Date(1469171277000L), 11, 2),
            new MyRun(new Date(1469171287000L), 12, 2),
            new MyRun(new Date(1469171297000L), 13, 4),
            new MyRun(new Date(1469171307000L), 10, 2),
            new MyRun(new Date(1469171317000L), 11, 2),

            new MyRun(new Date(1473073871000L), 10, 2),
            new MyRun(new Date(1473160271000L), 8, 2),
            new MyRun(new Date(1473246671000L), 2, 2)
    );

}
