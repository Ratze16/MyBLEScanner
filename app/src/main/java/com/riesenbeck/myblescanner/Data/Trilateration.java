package com.riesenbeck.myblescanner.Data;

/**
 * Created by Michael Riesenbeck on 06.10.2016.
 */

public class Trilateration {

    public static Trilateration trilaterationRef;
    private double xPos_func, yPos_func, errRad_func;
    private boolean init = false;

    private Trilateration(){}

    public double[] dist2Pos(double[][] posReceiver, double[] radius, int numEmp, double[] posXY){
        switch(numEmp){
            case 1: break;
            case 2: //Berechne Distanz zwischen zwei Empfängern. Befinden sich in 1. und 2. Stelle im Array
                double dist = Math.sqrt(Math.pow(posReceiver[0][0] - posReceiver[1][0], 2) + Math.pow(posReceiver[0][1] - posReceiver[1][1], 2));
                //Wenn Distanz ist größer als summe der Radien, dann gibt es keine Überschneidungen
                if (dist > (radius[0] + radius[1])) {
                    //Berechne mittlere Distanz zwischen den Empfängern, da sich Kreise nicht überschneiden
                    xPos_func = (posReceiver[0][0] + posReceiver[1][0]) / 2;
                    yPos_func = (posReceiver[0][1] + posReceiver[1][1]) / 2;
                }
                else {
                    double[][] pos = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1], radius[0], radius[1]);
                    //Schnittpunkte sind identisch, Beacon ist exakt zu lokalisieren
                    if ((pos[0][0] == pos[1][0]) && (pos[0][1] == pos[1][1])) {
                        xPos_func = pos[0][0];
                        yPos_func = pos[0][1];
                    }else {
                        xPos_func = (pos[0][0] + pos[1][0]) / 2;
                        yPos_func = (pos[0][1] + pos[1][1]) / 2;
                        errRad_func = Math.abs(dist -(radius[0] + radius[1]));
                    }
                }break;
            case 3:
                //posEmp[id][x oder y], x = 0, y = 1

                //Betrachtung bei 3 Empfängern
                final double dist12 = Math.sqrt(Math.pow(posReceiver[0][0] - posReceiver[1][0], 2) + Math.pow(posReceiver[0][1] - posReceiver[1][1], 2));
                final double dist13 = Math.sqrt(Math.pow(posReceiver[0][0] - posReceiver[2][0], 2) + Math.pow(posReceiver[0][1] - posReceiver[2][1], 2));
                final double dist23 = Math.sqrt(Math.pow(posReceiver[1][0] - posReceiver[2][0], 2) + Math.pow(posReceiver[1][1] - posReceiver[2][1], 2));
                if ((dist12 < Math.abs(radius[0] - radius[1])) || (dist13 < Math.abs(radius[0] - radius[2])) || (dist23 < Math.abs(radius[1] - radius[2]))) {
                    xPos_func = 0;
                    yPos_func = 0;
                    errRad_func = 0;
                    //values unuseable
                }
                else
                {
                    if ((dist12 > (radius[0] + radius[1])) && (dist13 > (radius[0] + radius[2])) && (dist23 > (radius[1] + radius[2])))
                    {
                        double p1[] = new double[2];	//x
                        double p2[] = new double[2];	//y

                        final double norm12 = Math.sqrt((posReceiver[0][0] - posReceiver[1][0])*(posReceiver[0][0] - posReceiver[1][0]) +
                                (posReceiver[0][1] - posReceiver[1][1])*(posReceiver[0][1] - posReceiver[1][1]));
                        // Verbindungspunkte Kreis 1 und 2
                        p1[0] = posReceiver[0][0] + (radius[0]/norm12) * (posReceiver[1][0] - posReceiver[0][0]); //x-P1
                        p1[1] = posReceiver[0][1] + (radius[0]/norm12) * (posReceiver[1][1] - posReceiver[0][1]); //y-P1
                        p2[0] = posReceiver[1][0] + (radius[1]/norm12) * (posReceiver[0][0] - posReceiver[1][0]); //x-P2
                        p2[1] = posReceiver[1][1] + (radius[1]/norm12) * (posReceiver[0][1] - posReceiver[1][1]); //y-P2
                        double m12[] = new double[2];
                        m12[0] = p1[0] + 0.5 * (p2[0] - p1[0]);	//x
                        m12[1] = p1[1] + 0.5 * (p2[1] - p1[0]);	//y


                        final double norm23 = Math.sqrt((posReceiver[1][0] - posReceiver[2][0])*(posReceiver[1][0] - posReceiver[2][0]) +
                                (posReceiver[1][1] - posReceiver[2][1])*(posReceiver[1][1] - posReceiver[2][1]));
                        // Verbindungspunkte Kreis 2 und 3
                        p1[0] = posReceiver[1][0] + (radius[1]/norm23) * (posReceiver[2][0] - posReceiver[1][0]); //x-P1
                        p1[1] = posReceiver[1][1] + (radius[1]/norm23) * (posReceiver[2][1] - posReceiver[1][1]); //y-P1
                        p2[0] = posReceiver[2][0] + (radius[2]/norm23) * (posReceiver[1][0] - posReceiver[2][0]); //x-P2
                        p2[1] = posReceiver[2][1] + (radius[2]/norm23) * (posReceiver[1][1] - posReceiver[2][1]); //y-P2
                        double m23[] = new double[2];
                        m23[0] = p1[0] + 0.5 * (p2[0] - p1[0]);	//x
                        m23[1] = p1[1] + 0.5 * (p2[1] - p1[0]);	//y


                        final double norm31 = Math.sqrt((posReceiver[2][0] - posReceiver[0][0])*(posReceiver[2][0] - posReceiver[0][0]) +
                                (posReceiver[2][1] - posReceiver[0][1])*(posReceiver[2][1] - posReceiver[0][1]));
                        // Verbindungspunkte Kreis 3 und 1
                        p1[0] = posReceiver[2][0] + (radius[2]/norm31) * (posReceiver[0][0] - posReceiver[2][0]); //x-P1
                        p1[1] = posReceiver[2][1] + (radius[2]/norm31) * (posReceiver[0][1] - posReceiver[2][1]); //y-P1
                        p2[0] = posReceiver[0][0] + (radius[0]/norm31) * (posReceiver[2][0] - posReceiver[0][0]); //x-P2
                        p2[1] = posReceiver[0][1] + (radius[0]/norm31) * (posReceiver[2][1] - posReceiver[0][1]); //y-P2
                        double m31[] = new double[2];
                        m31[0] = p1[0] + 0.5 * (p2[0] - p1[0]);	//x
                        m31[1] = p1[1] + 0.5 * (p2[1] - p1[0]);	//y


                        // Norm der drei Punkte berechnen
                        final double a_p = Math.sqrt((m12[0]* m12[0]) + (m12[1]* m12[1]));
                        final double b_p = Math.sqrt((m23[0]* m23[0]) + (m23[1]* m23[1]));
                        final double c_p = Math.sqrt((m31[0]* m31[0]) + (m31[1]* m31[1]));

                        final double denominator = 2 * (((m12[0] * m23[1]) - (m12[1] * m23[0])) + (m23[0] * m31[1] - m23[1] * m31[0]) + (m31[0] * m12[1] - m31[1] * m12[0]));
                        xPos_func = (1/denominator)*(((b_p*b_p - c_p*c_p)*(-m12[1]) + (c_p*c_p - a_p*a_p)* (-m23[1]) + (a_p*a_p - b_p*b_p)*(-m31[1])));
                        yPos_func = (1/denominator)*(((b_p*b_p - c_p*c_p)*m12[0] + (c_p*c_p - a_p*a_p)* m23[0] + (a_p*a_p - b_p*b_p)*m31[0]));
                        errRad_func = Math.sqrt((xPos_func-m12[0])*(xPos_func-m12[0]) + (yPos_func-m12[1])*(yPos_func-m12[1]));
                    }
		/*
		 * Kreis 1 und 3 schneiden sich
		 */
                    else if ((dist12 > (radius[0] + radius[1])) && (dist23 > (radius[1] + radius[2])))
                    {
                        double p1[] = new double[2];
                        //Kreisschnittpunkte
                        double pos[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[2][0], posReceiver[2][1], radius[0], radius[2]);
                        final double distS1 = Math.sqrt(Math.pow(posReceiver[1][0] - pos[0][0], 2) + Math.pow(posReceiver[1][1] - pos[0][1], 2));
                        final double distS2 = Math.sqrt(Math.pow(posReceiver[1][0] - pos[1][0], 2) + Math.pow(posReceiver[1][1] - pos[1][1], 2));

                        if (distS1 <= distS2)
                        {
                            p1[0] = posReceiver[1][0] + (radius[1]/distS1) * (pos[0][0] - posReceiver[1][0]); //x-P1
                            p1[1] = posReceiver[1][1] + (radius[1]/distS1) * (pos[0][1] - posReceiver[1][1]); //y-P1
                            xPos_func = p1[0] + 0.5 * (pos[0][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos[0][1] - p1[1]);
                            errRad_func = 0.5 * norm(pos[0][0], pos[0][1],p1[0], p1[1]);
                        }
                        else
                        {
                            p1[0] = posReceiver[1][0] + (radius[1]/distS2) * (pos[1][0] - posReceiver[1][0]); //x-P1
                            p1[1] = posReceiver[1][1] + (radius[1]/distS2) * (pos[1][1] - posReceiver[1][1]); //y-P1
                            xPos_func = p1[0] + 0.5 * (pos[1][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos[1][1] - p1[1]);
                            errRad_func = 0.5 * norm(pos[1][0], pos[1][1], p1[0], p1[1]);
                        }
                    }
                    else if ((dist12 > (radius[0] + radius[1])) && (dist13 > (radius[0] + radius[2])))
                    {
                        double p1[] = new double[2];
                        //Kreisschnittpunkte
                        double pos[][] = circlepoints2(posReceiver[1][0], posReceiver[1][1], posReceiver[2][0], posReceiver[2][1], radius[1], radius[2]);
                        final double distS1 = Math.sqrt(Math.pow(posReceiver[1][0] - pos[0][0], 2) + Math.pow(posReceiver[1][1] - pos[0][1], 2));
                        final double distS2 = Math.sqrt(Math.pow(posReceiver[1][0] - pos[1][0], 2) + Math.pow(posReceiver[1][1] - pos[1][1], 2));

                        if (distS1 <= distS2) {
                            p1[0] = posReceiver[1][0] + (radius[0]/distS1) * (pos[0][0] - posReceiver[1][0]); //x-P1
                            p1[1] = posReceiver[1][1] + (radius[0]/distS1) * (pos[0][1] - posReceiver[1][1]); //y-P1
                            xPos_func = p1[0] + 0.5 * (pos[0][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos[0][1] - p1[1]);
                            errRad_func = 0.5 * norm(pos[0][0], pos[0][1],p1[0], p1[1]);
                        }
                        else {
                            p1[0] = posReceiver[1][0] + (radius[0]/distS2) * (pos[1][0] - posReceiver[1][0]); //x-P1
                            p1[1] = posReceiver[1][1] + (radius[0]/distS2) * (pos[1][1] - posReceiver[1][1]); //y-P1
                            xPos_func = p1[0] + 0.5 * (pos[1][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos[1][1] - p1[1]);
                            errRad_func = 0.5 * norm(pos[1][0], pos[1][1], p1[0], p1[1]);
                        }
                    }
                    else if ((dist13 > (radius[0] + radius[2])) && (dist23 > (radius[1] + radius[2])))
                    {
                        double p1[] = new double[2];
                        //Kreisschnittpunkte
                        double pos[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1], radius[0], radius[1]);
                        final double distS1 = Math.sqrt(Math.pow(posReceiver[1][0] - pos[0][0], 2) + Math.pow(posReceiver[1][1] - pos[0][1], 2));
                        final double distS2 = Math.sqrt(Math.pow(posReceiver[1][0] - pos[1][0], 2) + Math.pow(posReceiver[1][1] - pos[1][1], 2));
                        if (distS1 <= distS2)
                        {
                            p1[0] = posReceiver[1][0] + (radius[2]/distS1) * (pos[0][0] - posReceiver[1][0]); //x-P1
                            p1[1] = posReceiver[1][1] + (radius[2]/distS1) * (pos[0][1] - posReceiver[1][1]); //y-P1
                            xPos_func = p1[0] + 0.5 * (pos[0][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos[0][1] - p1[1]);
                            errRad_func = 0.5 * norm(pos[0][0], pos[0][1],p1[0], p1[1]);
                        }
                        else
                        {
                            p1[0] = posReceiver[1][0] + (radius[2]/distS2) * (pos[1][0] - posReceiver[1][0]); //x-P1
                            p1[1] = posReceiver[1][1] + (radius[2]/distS2) * (pos[1][1] - posReceiver[1][1]); //y-P1
                            xPos_func = p1[0] + 0.5 * (pos[1][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos[1][1] - p1[1]);
                            errRad_func = 0.5 * norm(pos[1][0], pos[1][1], p1[0], p1[1]);
                        }
                    }
                    else if ((dist12 > (radius[0] + radius[1])))
                    {
                        double p1[] = new double[2];	//x
                        double p2[] = new double[2];	//y
                        //Kreisschnittpunkte
                        double smallestDist_1, smallestDist_2;
                        double pos23[][] = circlepoints2(posReceiver[1][0], posReceiver[1][1], posReceiver[2][0], posReceiver[2][1], radius[1], radius[2]);
                        double pos13[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[2][0], posReceiver[2][1], radius[0], radius[2]);
                        final double distS1_1 = Math.sqrt(Math.pow(pos23[0][0] - pos13[0][0], 2) + Math.pow(pos23[0][1] - pos13[0][1], 2));
                        final double distS2_1 = Math.sqrt(Math.pow(pos23[1][0] - pos13[0][0], 2) + Math.pow(pos23[1][1] - pos13[0][1], 2));
                        final double distS1_2 = Math.sqrt(Math.pow(pos23[0][0] - pos13[1][0], 2) + Math.pow(pos23[0][1] - pos13[1][1], 2));
                        final double distS2_2 = Math.sqrt(Math.pow(pos23[1][0] - pos13[1][0], 2) + Math.pow(pos23[1][1] - pos13[1][1], 2));
                        if (distS1_1 <= distS2_1)
                        {
                            p1[0] = pos23[0][0]; //x-P1
                            p1[1] = pos23[0][1]; //y-P1
                            smallestDist_1 = distS1_1;
                        }
                        else {
                            p1[0] = pos23[1][0]; //x-P1
                            p1[1] = pos23[1][1]; //y-P1
                            smallestDist_1 = distS2_1;
                        }
                        if (distS1_2 <= distS2_2)
                        {
                            p2[0] = pos23[0][0]; //x-P1
                            p2[1] = pos23[0][1]; //y-P1
                            smallestDist_2 = distS1_2;
                        }
                        else {
                            p2[0] = pos23[1][0]; //x-P1
                            p2[1] = pos23[1][1]; //y-P1
                            smallestDist_2 = distS2_2;
                        }
                        if (smallestDist_1 <= smallestDist_2)
                        {
                            xPos_func = p1[0] + 0.5 * (pos13[0][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos13[0][1] - p1[1]);
                        }
                        else {
                            xPos_func = p2[0] + 0.5 * (pos13[1][0] - p2[0]);
                            yPos_func = p2[1] + 0.5 * (pos13[1][1] - p2[1]);
                        }
                    }
                    else if ((dist23 > (radius[1] + radius[2])))
                    {
                        double p1[] = new double[2];	//x
                        double p2[] = new double[2];	//y
                        //Kreisschnittpunkte
                        double smallestDist_1, smallestDist_2;
                        double pos12[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1], radius[0], radius[1]);
                        double pos13[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[2][0], posReceiver[2][1], radius[0], radius[2]);
                        final double distS1_1 = Math.sqrt(Math.pow(pos12[0][0] - pos13[0][0], 2) + Math.pow(pos12[0][1] - pos13[0][1], 2));
                        final double distS2_1 = Math.sqrt(Math.pow(pos12[1][0] - pos13[0][0], 2) + Math.pow(pos12[1][1] - pos13[0][1], 2));
                        final double distS1_2 = Math.sqrt(Math.pow(pos12[0][0] - pos13[1][0], 2) + Math.pow(pos12[0][1] - pos13[1][1], 2));
                        final double distS2_2 = Math.sqrt(Math.pow(pos12[1][0] - pos13[1][0], 2) + Math.pow(pos12[1][1] - pos13[1][1], 2));
                        if (distS1_1 <= distS2_1)
                        {
                            p1[0] = pos12[0][0]; //x-P1
                            p1[1] = pos12[0][1]; //y-P1
                            smallestDist_1 = distS1_1;
                        }
                        else
                        {
                            p1[0] = pos12[1][0]; //x-P1
                            p1[1] = pos12[1][1]; //y-P1
                            smallestDist_1 = distS2_1;
                        }
                        if (distS1_2 <= distS2_2)
                        {
                            p2[0] = pos12[0][0]; //x-P1
                            p2[1] = pos12[0][1]; //y-P1
                            smallestDist_2 = distS1_2;
                        }
                        else
                        {
                            p2[0] = pos12[1][0]; //x-P1
                            p2[1] = pos12[1][1]; //y-P1
                            smallestDist_2 = distS2_2;
                        }
                        if (smallestDist_1 <= smallestDist_2)
                        {
                            xPos_func = p1[0] + 0.5 * (pos13[0][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos13[0][1] - p1[1]);
                        }
                        else
                        {
                            xPos_func = p2[0] + 0.5 * (pos13[1][0] - p2[0]);
                            yPos_func = p2[1] + 0.5 * (pos13[1][1] - p2[1]);
                        }
                    }
                    else if ((dist13 > (radius[0] + radius[2])))
                    {
                        double p1[] = new double[2];	//x
                        double p2[] = new double[2];	//y
                        //Kreisschnittpunkte
                        double smallestDist_1, smallestDist_2;
                        double pos12[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1], radius[0], radius[1]);
                        double pos23[][] = circlepoints2(posReceiver[1][0], posReceiver[1][1], posReceiver[2][0], posReceiver[2][1], radius[1], radius[2]);
                        final double distS1_1 = Math.sqrt(Math.pow(pos12[0][0] - pos23[0][0], 2) + Math.pow(pos12[0][1] - pos23[0][1], 2));
                        final double distS2_1 = Math.sqrt(Math.pow(pos12[1][0] - pos23[0][0], 2) + Math.pow(pos12[1][1] - pos23[0][1], 2));
                        final double distS1_2 = Math.sqrt(Math.pow(pos12[0][0] - pos23[1][0], 2) + Math.pow(pos12[0][1] - pos23[1][1], 2));
                        final double distS2_2 = Math.sqrt(Math.pow(pos12[1][0] - pos23[1][0], 2) + Math.pow(pos12[1][1] - pos23[1][1], 2));
                        if (distS1_1 <= distS2_1)
                        {
                            p1[0] = pos12[0][0]; //x-P1
                            p1[1] = pos12[0][1]; //y-P1
                            smallestDist_1 = distS1_1;
                        }
                        else
                        {
                            p1[0] = pos12[1][0]; //x-P1
                            p1[1] = pos12[1][1]; //y-P1
                            smallestDist_1 = distS2_1;
                        }
                        if (distS1_2 <= distS2_2)
                        {
                            p2[0] = pos12[0][0]; //x-P1
                            p2[1] = pos12[0][1]; //y-P1
                            smallestDist_2 = distS1_2;
                        }
                        else
                        {
                            p2[0] = pos12[1][0]; //x-P1
                            p2[1] = pos12[1][1]; //y-P1
                            smallestDist_2 = distS2_2;
                        }
                        if (smallestDist_1 <= smallestDist_2)
                        {
                            xPos_func = p1[0] + 0.5 * (pos23[0][0] - p1[0]);
                            yPos_func = p1[1] + 0.5 * (pos23[0][1] - p1[1]);
                        }
                        else
                        {
                            xPos_func = p2[0] + 0.5 * (pos23[1][0] - p2[0]);
                            yPos_func = p2[1] + 0.5 * (pos23[1][1] - p2[1]);
                        }
                    }
                    else
                    {
                        double p1_1[]= new double[2];
                        double p2_1[]= new double[2];
                        double p1_2[]= new double[2];
                        double p2_2[]= new double[2];
                        double A[]= new double[2], B[]= new double[2], C[]= new double[2];
                        //Kreisschnittpunkte
                        double smallestDist_1a, smallestDist_2a, smallestDist_1b, smallestDist_2b;
                        double pos12[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1], radius[0], radius[1]);
                        double pos23[][] = circlepoints2(posReceiver[1][0], posReceiver[1][1], posReceiver[2][0], posReceiver[2][1], radius[1], radius[2]);
                        double pos13[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[2][0], posReceiver[2][1], radius[0], radius[2]);
                        final double distS1_1a = norm(pos12[0][0], pos12[0][1], pos23[0][0], pos23[0][1]);
                        final double distS2_1a = norm(pos12[1][0], pos12[1][1], pos23[0][0], pos23[0][1]);
                        final double distS1_1b = Math.sqrt(Math.pow(pos13[0][0] - pos23[0][0], 2) + Math.pow(pos13[0][1] - pos23[0][1], 2));
                        final double distS2_1b = Math.sqrt(Math.pow(pos13[1][0] - pos23[0][0], 2) + Math.pow(pos13[1][1] - pos23[0][1], 2));
                        final double distS1_2a = Math.sqrt(Math.pow(pos12[0][0] - pos23[1][0], 2) + Math.pow(pos12[0][1] - pos23[1][1], 2));
                        final double distS2_2a = Math.sqrt(Math.pow(pos12[1][0] - pos23[1][0], 2) + Math.pow(pos12[1][1] - pos23[1][1], 2));
                        final double distS1_2b = Math.sqrt(Math.pow(pos13[0][0] - pos23[1][0], 2) + Math.pow(pos12[0][1] - pos23[1][1], 2));
                        final double distS2_2b = Math.sqrt(Math.pow(pos13[1][0] - pos23[1][0], 2) + Math.pow(pos12[1][1] - pos23[1][1], 2));

                        if (distS1_1a <= distS2_1a)
                        {
                            p1_1[0] = pos12[0][0]; //x-P1
                            p1_1[1] = pos12[0][1]; //y-P1
                            smallestDist_1a = distS1_1a;
                        }
                        else
                        {
                            p1_1[0] = pos12[1][0]; //x-P1
                            p1_1[1] = pos12[1][1]; //y-P1
                            smallestDist_1a = distS2_1a;
                        }
                        if (distS1_1b <= distS2_1b)
                        {
                            p2_1[0] = pos13[0][0]; //x-P1
                            p2_1[1] = pos13[0][1]; //y-P1
                            smallestDist_1b = distS1_1b;
                        }
                        else
                        {
                            p2_1[0] = pos13[1][0]; //x-P1
                            p2_1[1] = pos13[1][1]; //y-P1
                            smallestDist_1b = distS2_1b;
                        }
                        if (distS1_2a <= distS2_2a)
                        {
                            p1_2[0] = pos12[0][0]; //x-P1
                            p1_2[1] = pos12[0][1]; //y-P1
                            smallestDist_2a = distS1_2a;
                        }
                        else
                        {
                            p1_2[0] = pos12[1][0]; //x-P1
                            p1_2[1] = pos12[1][1]; //y-P1
                            smallestDist_2a = distS2_2a;
                        }
                        if (distS1_2b <= distS2_2b)
                        {
                            p2_2[0] = pos13[0][0]; //x-P2
                            p2_2[1] = pos13[0][1]; //y-P2
                            smallestDist_2b = distS1_2b;
                        }
                        else
                        {
                            p2_2[0] = pos13[1][0]; //x-P2
                            p2_2[1] = pos13[1][1]; //y-P2
                            smallestDist_2b = distS2_2b;
                        }
                        if (smallestDist_1a + smallestDist_1b <= smallestDist_2a + smallestDist_2b)
                        {
                            A[0] = pos23[0][0];
                            A[1] = pos23[0][1];
                            B[0] = p1_1[0];
                            B[1] = p1_1[1];
                            C[0] = p2_1[0];
                            C[1] = p2_1[1];
                        }
                        else
                        {
                            A[0] = pos23[1][0];
                            A[1] = pos23[1][1];
                            B[0] = p1_2[0];
                            B[1] = p1_2[1];
                            C[0] = p2_2[0];
                            C[1] = p2_2[1];
                        }

                        final double a_p = Math.sqrt(A[0]*A[0] + A[1]*A[1]);
                        final double b_p = Math.sqrt(B[0]* B[0] + B[1]* B[1]);
                        final double c_p = Math.sqrt(C[0]* C[0] + C[1]* C[1]);
                        final double denominator = 2 * ((A[0] * B[1] - A[1] * B[0]) + (B[0] * C[1] - B[1] * C[0]) + (C[0] * A[1] - C[1] * A[0]));
                        xPos_func = (1.0/denominator)*(((b_p*b_p) - (c_p*c_p))*(-A[1]) + ((c_p*c_p) - (a_p*a_p))* (-B[1]) + ((a_p*a_p) - (b_p*b_p))*(-C[1]));
                        yPos_func = (1.0/denominator)*(((b_p*b_p) - (c_p*c_p))*A[0] + ((c_p*c_p) - (a_p*a_p))* B[1] + ((a_p*a_p) - (b_p*b_p))*C[0]);
                        errRad_func = norm(xPos_func, yPos_func, A[0], A[1]);
                    }
                }
                break;
            case 4: break;
            default: break;
        }
        double[] result = {xPos_func,yPos_func,errRad_func};
        return result;
    }

    private double[][] circlepoints2(double x1, double y1, double x2, double y2, double rad1, double rad2){
        double[][] pos = new double[2][2]; //Kreisschnittpunkte;

        double t1 = (y1 - y2) / (x2 - x1);
        double t2 = (x2 * x2 - x1 * x1 + y2 * y2 - y1 * y1 + rad1 * rad1 - rad2 * rad2) /(2.0 * x2 - 2.0 * x1);
        double a_circ = (t1*t1 + 1.0);
        double b_circ = 2.0 * ((t1 * t2 - t1 * x1) - y1);
        double c_circ = (t2*t2 - 2*t2*x1 + x1*x1 + y1*y1 - rad1*rad1);

        //y-Werte
        pos[0][1] = (-b_circ + Math.sqrt(b_circ*b_circ - 4 * a_circ*c_circ)) / (2 * a_circ);
        pos[1][1] = (-b_circ - Math.sqrt(b_circ*b_circ - 4 * a_circ*c_circ)) / (2 * a_circ);

        pos[0][0] = t1 * pos[0][1] + t2; //x-Werte
        pos[1][0] = t1 * pos[1][1] + t2; //x-Werte
        return pos;
    }

    double norm(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }


    public static Trilateration getInstance(){
        if (trilaterationRef==null){
            trilaterationRef = new Trilateration();
        }
        return  trilaterationRef;
    }

}
