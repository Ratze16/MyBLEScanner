package com.riesenbeck.myblescanner.Data;

/**
 * Created by Michael Riesenbeck on 06.10.2016.
 */

public class Trilateration {

    private double posX, posY, errRad;
    private boolean init = false;

    public void dist2Pos(double[][] posEmp, double[] radius, int numEmp, double[] posXY){
        switch(numEmp){
            case 1: break;
            case 2: //Berechne Distanz zwischen zwei Empfängern. Befinden sich in 1. und 2. Stelle im Array
                double dist = Math.sqrt(Math.pow(posEmp[0][0] - posEmp[1][0], 2) + Math.pow(posEmp[0][1] - posEmp[1][1], 2));
                //Wenn Distanz ist größer als summe der Radien, dann gibt es keine Überschneidungen
                if (dist > (radius[0] + radius[1])) {
                    //Berechne mittlere Distanz zwischen den Empfängern, da sich Kreise nicht überschneiden
                    posX = (posEmp[0][0] + posEmp[1][0]) / 2;
                    posY = (posEmp[0][1] + posEmp[1][1]) / 2;
                }
                else {
                    double[][] pos = new double[2][2]; //Kreisschnittpunkte
                    circlepoints2(posEmp[0][0], posEmp[0][1], posEmp[1][0], posEmp[1][1], radius[0], radius[1], pos);
                    //Schnittpunkte sind identisch, Beacon ist exakt zu lokalisieren
                    if ((pos[0][0] == pos[1][0]) && (pos[0][1] == pos[1][1])) {
                        posX = pos[0][0];
                        posY = pos[0][1];
                    }else {
                        posX = (pos[0][0] + pos[1][0]) / 2;
                        posY = (pos[0][1] + pos[1][1]) / 2;
                        errRad = Math.abs(dist -(radius[0] + radius[1]));
                    }
                }break;
            case 3: break;
            case 4: break;
            default: break;
        }
    }

    private void circlepoints2(double x1, double y1, double x2, double y2, double rad1, double rad2, double[][] pos){
        double t1 = (y1 - y2) / (x2 - x1);
        double t2 = (((((x2 * x2 - x1 * x1) + y2 * y2) - y1 * y1) + rad1 * rad1) - rad2 * rad2);

        double a = (t1*t1 + 1.0);
        double b = 2.0 * ((t1 * t2 - t1 * x1) - y1);
        double c = (t2*t2 - 2*t2*x1 + x1*x1 + y1*y1 - rad1*rad1);

        //y-Values
        pos[0][1] = (-b + Math.sqrt(b*b - 4 * a*c)) / (2 * a);
        pos[1][1] = (-b - Math.sqrt(b*b - 4 * a*c)) / (2 * a);

        //x-Values
        pos[0][0] = t1 * pos[0][1] + t2;
        pos[1][0] = t1 * pos[1][1] + t2;
    }

}
