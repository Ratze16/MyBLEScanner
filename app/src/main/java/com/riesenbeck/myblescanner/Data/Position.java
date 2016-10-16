package com.riesenbeck.myblescanner.Data;

/**
 * Created by michael on 13.10.2016.
 * @Author: Michael Riesenebck
 * @Date: 17.10.2016
 */

public class Position {

    private double xPos_func, yPos_func, errRad_func;
    private double[][]posReceiver;
    private double[] radius;
    private double[] mLastPosition;

    public Position(double[][] posReceiver, double[] radius, int numRec){
        getCurrentPosition(posReceiver,radius,numRec);
    }

    public double[] getLastPosition(){
        return mLastPosition;
    }

    public double[] getCurrentPosition(double[][] posReceiver, double[] radius, int numEmp){
        this.posReceiver = posReceiver;
        this.radius = radius;
        switch(numEmp){
            case 1: //Todo: Calculate Positionsbestimmung bei einem Beacon
                break;
            case 2: //Berechne Distanz zwischen zwei Empfängern. Befinden sich in 1. und 2. Stelle im Array
                dist2Pos2Beacons();
                break;
            case 3:
                dist2Pos3Beacons();
                break;
            case 4: break;
            default: break;
        }
        mLastPosition = new double[]{xPos_func,yPos_func,errRad_func};
        return mLastPosition;
    }
    /*
		*	Berechnet Schnittpunkte zwischen zwei Kreisen
		*/
    private double[][] circlepoints2(double x1, double y1, double x2, double y2, double rad1, double rad2)
    {
        double[][]pos = new double[2][2];

        double t1 = (y1 - y2) / (x2 - x1);
        double t2 = (((((x2 * x2 - x1 * x1) + y2 * y2) - y1 * y1) + rad1 * rad1) - rad2 * rad2) /(2.0 * x2 - 2.0 * x1);
        double a_circ = (t1*t1 + 1.0);
        double b_circ = 2.0 * ((t1 * t2 - t1 * x1) - y1);
        double c_circ = (t2*t2 - 2*t2*x1 + x1*x1 + y1*y1 - rad1*rad1);

        //y-Werte
        pos[0][1] = (-b_circ + Math.sqrt(b_circ*b_circ - 4 * a_circ*c_circ)) / (2 * a_circ); //y1
        pos[1][1] = (-b_circ - Math.sqrt(b_circ*b_circ - 4 * a_circ*c_circ)) / (2 * a_circ); //y2

        //x-Werte
        pos[0][0] = t1 * pos[0][1] + t2; //x1
        pos[1][0] = t1 * pos[1][1] + t2; //x2

        return pos;
    }

    /*
    *	Berechnet Norm zwischen zwei Vektoren
    */
    private double norm(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    /*
    * Calculation of the position with 2 receiverpositions, with the RSSI-values calculatet radian [in meter].
    */
    private void dist2Pos2Beacons(){
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
        }
    }

    /*
    * Calculation of the position with 3 receiverpositions, with the RSSI-values calculatet radian [in meter].
    */
    private void dist2Pos3Beacons()
    {
        //Betrachtung bei 3 Empfaengern
        double dist12 = Math.sqrt(Math.pow(posReceiver[0][0] - posReceiver[1][0], 2) + Math.pow(posReceiver[0][1] - posReceiver[1][1], 2));
        double dist13 = Math.sqrt(Math.pow(posReceiver[0][0] - posReceiver[2][0], 2) + Math.pow(posReceiver[0][1] - posReceiver[2][1], 2));
        double dist23 = Math.sqrt(Math.pow(posReceiver[1][0] - posReceiver[2][0], 2) + Math.pow(posReceiver[1][1] - posReceiver[2][1], 2));

        boolean temp12 = Math.abs(radius[0] - radius[1]) > dist12;
        boolean temp23 = Math.abs(radius[1] - radius[2]) > dist23;
        boolean temp13 = Math.abs(radius[0] - radius[2]) > dist13;

        //Zwei Kreise innerhalb eines anderen Kreises
        if ((temp12 && temp23) || (temp12 && temp13) || (temp23 && temp13))
        {
            double f0 = (dist12 / radius[0] + dist13 / radius[0]) / 2;
            double f1 = (dist12 / radius[1] + dist23 / radius[1]) / 2;
            double f2 = (dist12 / radius[2] + dist13 / radius[2]) / 2;

            radius[0] = radius[0] * f0;
            radius[1] = radius[1] * f1;
            radius[2] = radius[2] * f2;
        }
        //Kreis 2 in Kreis 1
        if ((Math.abs(radius[0] - radius[1]) > dist12) && (radius[0] > radius[1]))
        {
            double[][] posRec = new double[][]{{posReceiver[0][0],posReceiver[0][1]},{posReceiver[1][0],posReceiver[1][1]},{posReceiver[2][0],posReceiver[2][1]}};
            double[] rad =new double[]{radius[0],radius[1],radius[2]};
            calcCircleInCircle(posRec, rad, dist13);
        }
        //Kreis 1 in Kreis 2
        else if ((Math.abs(radius[0] - radius[1]) > dist12) && (radius[1] > radius[0]))
        {
            double[][] posRec = new double[][]{{posReceiver[1][0],posReceiver[1][1]},{posReceiver[0][0],posReceiver[0][1]},{posReceiver[2][0],posReceiver[2][1]}};
            double[] rad =new double[]{radius[1],radius[0],radius[2]};
            calcCircleInCircle(posRec, rad, dist23);
        }
        //Kreis 3 in Kreis 1
        else if ((Math.abs(radius[0] - radius[2]) > dist13) && (radius[0] > radius[2]))//Kreis 3 in 1
        {
            double[][] posRec = new double[][]{{posReceiver[2][0],posReceiver[2][1]},{posReceiver[0][0],posReceiver[0][1]},{posReceiver[1][0],posReceiver[1][1]}};
            double[] rad =new double[]{radius[2],radius[0],radius[1]};
            calcCircleInCircle(posRec, rad, dist12);
        }
        //Kreis 1 in Kreis 3
        else if ((Math.abs(radius[0] - radius[2]) > dist13) && (radius[2] > radius[0]))//Kreis 1 in 3
        {
            double[][] posRec = new double[][]{{posReceiver[0][0],posReceiver[0][1]},{posReceiver[2][0],posReceiver[2][1]},{posReceiver[1][0],posReceiver[1][1]}};
            double[] rad =new double[]{radius[0],radius[2],radius[1]};
            calcCircleInCircle(posRec, rad, dist23);
        }
        //Kreis 3 in Kreis 2
        else if ((Math.abs(radius[1] - radius[2]) > dist23) && (radius[1] > radius[2]))//Kreis 3 in 2
        {
            double[][] posRec = new double[][]{{posReceiver[2][0],posReceiver[2][1]},{posReceiver[1][0],posReceiver[1][1]},{posReceiver[0][0],posReceiver[0][1]}};
            double[] rad =new double[]{radius[2],radius[1],radius[0]};
            calcCircleInCircle(posRec, rad, dist12);
        }
        //Kreis 2 in Kreis 3
        else if ((Math.abs(radius[1] - radius[2]) > dist23) && (radius[2] > radius[1]))//Kreis 2 ind 3
        {
            double[][] posRec = new double[][]{{posReceiver[1][0],posReceiver[1][1]},{posReceiver[2][0],posReceiver[2][1]},{posReceiver[0][0],posReceiver[0][1]}};
            double[] rad =new double[]{radius[1],radius[2],radius[0]};
            calcCircleInCircle(posRec, rad, dist13);
        }
        //Kein Kreis liegt innerhalb eines Anderen
        else
        {

            if ((dist12 > (radius[0] + radius[1])) && (dist13 > (radius[0] + radius[2])) && (dist23 > (radius[1] + radius[2])))
            {
                double p1[] = new double[2];	//x
                double p2[] = new double[2];	//y

                double norm12 = norm(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1]);
                // Verbindungspunkte Kreis 1 und 2
                p1[0] = posReceiver[0][0] + (radius[0]/ norm12) * (posReceiver[1][0] - posReceiver[0][0]); //x-P1
                p1[1] = posReceiver[0][1] + (radius[0]/ norm12) * (posReceiver[1][1] - posReceiver[0][1]); //y-P1
                p2[0] = posReceiver[1][0] + (radius[1]/ norm12) * (posReceiver[0][0] - posReceiver[1][0]); //x-P2
                p2[1] = posReceiver[1][1] + (radius[1]/ norm12) * (posReceiver[0][1] - posReceiver[1][1]); //y-P2
                double m12[] = new double[2];
                m12[0] = p1[0] + 0.5 * (p2[0] - p1[0]);	//x
                m12[1] = p1[1] + 0.5 * (p2[1] - p1[1]);	//y


                double norm23 = norm(posReceiver[1][0], posReceiver[1][1], posReceiver[2][0], posReceiver[2][1]);
                // Verbindungspunkte Kreis 2 und 3
                p1[0] = posReceiver[1][0] + (radius[1]/ norm23) * (posReceiver[2][0] - posReceiver[1][0]); //x-P1
                p1[1] = posReceiver[1][1] + (radius[1]/ norm23) * (posReceiver[2][1] - posReceiver[1][1]); //y-P1
                p2[0] = posReceiver[2][0] + (radius[2]/ norm23) * (posReceiver[1][0] - posReceiver[2][0]); //x-P2
                p2[1] = posReceiver[2][1] + (radius[2]/ norm23) * (posReceiver[1][1] - posReceiver[2][1]); //y-P2
                double m23[] = new double[2];
                m23[0] = p1[0] + 0.5 * (p2[0] - p1[0]);	//x
                m23[1] = p1[1] + 0.5 * (p2[1] - p1[1]);	//y


                double norm31 = norm(posReceiver[2][0], posReceiver[2][1], posReceiver[0][0], posReceiver[0][1]);
                // Verbindungspunkte Kreis 3 und 1
                p1[0] = posReceiver[2][0] + (radius[2]/ norm31) * (posReceiver[0][0] - posReceiver[2][0]); //x-P1
                p1[1] = posReceiver[2][1] + (radius[2]/ norm31) * (posReceiver[0][1] - posReceiver[2][1]); //y-P1
                p2[0] = posReceiver[0][0] + (radius[0]/ norm31) * (posReceiver[2][0] - posReceiver[0][0]); //x-P2
                p2[1] = posReceiver[0][1] + (radius[0]/ norm31) * (posReceiver[2][1] - posReceiver[0][1]); //y-P2
                double m31[] = new double[2];
                m31[0] = p1[0] + 0.5 * (p2[0] - p1[0]);	//x
                m31[1] = p1[1] + 0.5 * (p2[1] - p1[1]);	//y


                // Norm der drei Punkte berechnen
                double a_p = Math.sqrt((m12[0] * m12[0]) + (m12[1] * m12[1]));
                double b_p = Math.sqrt((m23[0] * m23[0]) + (m23[1] * m23[1]));
                double c_p = Math.sqrt((m31[0] * m31[0]) + (m31[1] * m31[1]));

                double denominator = 2 * (((m12[0] * m23[1]) - (m12[1] * m23[0])) + (m23[0] * m31[1] - m23[1] * m31[0]) + (m31[0] * m12[1] - m31[1] * m12[0]));
                xPos_func = (1/ denominator)*(((b_p * b_p - c_p * c_p)*(-m12[1]) + (c_p * c_p - a_p * a_p)* (-m23[1]) + (a_p * a_p - b_p * b_p)*(-m31[1])));
                yPos_func = (1/ denominator)*(((b_p * b_p - c_p * c_p)*m12[0] + (c_p * c_p - a_p * a_p)* m23[0] + (a_p * a_p - b_p * b_p)*m31[0]));
                errRad_func = norm(xPos_func, yPos_func, m12[0], m12[1]);

            }
            // Kreis 1 und 3 schneiden sich
            else if ((dist12 > (radius[0] + radius[1])) && (dist23 > (radius[1] + radius[2])))
            {
                double[][] posRec = new double[][]{{posReceiver[0][0],posReceiver[0][1]},{posReceiver[2][0],posReceiver[2][1]},{posReceiver[1][0],posReceiver[1][1]}};
                double[] rad = new double[]{radius[0],radius[2],radius[1]};
                calc2Circle(posRec,rad);
            }
            //Kreis 2 und 3 schneide sich
            else if ((dist12 > (radius[0] + radius[1])) && (dist13 > (radius[0] + radius[2])))
            {
                double[][] posRec = new double[][]{{posReceiver[1][0],posReceiver[1][1]},{posReceiver[2][0],posReceiver[2][1]},{posReceiver[0][0],posReceiver[0][1]}};
                double[] rad = new double[]{radius[1],radius[2],radius[0]};
                calc2Circle(posRec,rad);
            }
            //Kreis 1 und 2 schneiden sich
            else if ((dist13 > (radius[0] + radius[2])) && (dist23 > (radius[1] + radius[2])))
            {
                double[][] posRec = new double[][]{{posReceiver[0][0],posReceiver[0][1]},{posReceiver[1][0],posReceiver[1][1]},{posReceiver[2][0],posReceiver[2][1]}};
                double[] rad = new double[]{radius[0],radius[1],radius[2]};
                calc2Circle(posRec,rad);
            }
            else if ((dist12 > (radius[0] + radius[1])))
            {
                double[][] posRec = new double[][]{
                        {posReceiver[0][0],posReceiver[0][1]},
                        {posReceiver[1][0],posReceiver[1][1]},
                        {posReceiver[2][0],posReceiver[2][1]}};
                double[] rad = new double[]{radius[0],radius[1],radius[2]};
                calc1Circle(posRec,rad);
            }
            else if ((dist23 > (radius[1] + radius[2])))
            {
                double[][] posRec = new double[][]{
                        {posReceiver[1][0],posReceiver[1][1]},
                        {posReceiver[2][0],posReceiver[2][1]},
                        {posReceiver[0][0],posReceiver[0][1]}};
                double[] rad = new double[]{radius[1],radius[2],radius[0]};
                calc1Circle(posRec,rad);
            }
            else if ((dist13 > (radius[0] + radius[2])))
            {
                double[][] posRec = new double[][]{
                        {posReceiver[0][0],posReceiver[0][1]},
                        {posReceiver[2][0],posReceiver[2][1]},
                        {posReceiver[1][0],posReceiver[1][1]}};
                double[] rad = new double[]{radius[0],radius[2],radius[1]};
                calc1Circle(posRec,rad);
            }
            else
            {
                double x1 = 0, y1 = 0, x2 = 0, y2 = 0, x3 = 0, y3 = 0;
                //Kreisschnittpunkte
                //Kreisschnittpunkte
                //Kreisschnittpunkte
                double pos12[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1], radius[0], radius[1]);
                double pos23[][] = circlepoints2(posReceiver[1][0], posReceiver[1][1], posReceiver[2][0], posReceiver[2][1], radius[1], radius[2]);
                double pos13[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[2][0], posReceiver[2][1], radius[0], radius[2]);

                if(radius[0] >= norm(pos23[0][0],pos23[0][1], posReceiver[0][0],posReceiver[0][1]))
                {
                    x1 = pos23[0][0];
                    y1 = pos23[0][1];
                }
                else if(radius[0] >= norm(pos23[1][0], pos23[1][1], posReceiver[0][0],posReceiver[0][1]))
                {
                    x1 = pos23[1][0];
                    y1 = pos23[1][1];
                }

                if(radius[1] >= norm(pos13[0][0], pos13[0][1], posReceiver[1][0],posReceiver[1][1]))
                {
                    x2 = pos13[0][0];
                    y2 = pos13[0][1];
                }
                else if(radius[1] >= norm(pos13[1][0], pos13[1][1], posReceiver[1][0],posReceiver[1][1]))
                {
                    x2 = pos13[1][0];
                    y2 = pos13[1][1];
                }

                if(radius[2] >= norm(pos12[0][0], pos12[0][1], posReceiver[2][0],posReceiver[2][1]))
                {
                    x3 = pos12[0][0];
                    y3 = pos12[0][1];
                }
                else if(radius[2] >= norm(pos12[1][0], pos12[1][1], posReceiver[2][0],posReceiver[2][1]))
                {
                    x3 = pos12[1][0];
                    y3 = pos12[1][1];
                }

                double tempD = x1 * y2 + x2 * y3 + x3 * y1 - x1 * y3 - x2 * y1 - x3 * y2;

                xPos_func = (0.5*(
                        -y1*(y2*y2)
                        +y1*(y3*y3)
                        -y1*(x2*x2)
                        +y1*(x3*x3)

                        +y2*(y1*y1)
                        -y2*(y3*y3)
                        +y2*(x1*x1)
                        -y2*(x3*x3)

                        -y3*(y1*y1)
                        +y3*(y2*y2)
                        -y3*(x1*x1)
                        +y3*(x2*x2)
                ))/ tempD;

                yPos_func = (0.5*(
                        +x1*(x2*x2)
                        -x1*(x3*x3)
                        +x1*(y2*y2)
                        -x1*(y3*y3)

                        -x2*(x1*x1)
                        +x2*(x3*x3)
                        -x2*(y1*y1)
                        +x2*(y3*y3)

                        +x3*(x1*x1)
                        -x3*(x2*x2)
                        +x3*(y1*y1)
                        -x3*(y2*y2)
                ))/ tempD;

                errRad_func = norm(x1, y1, xPos_func, yPos_func);
            }
        }
    }

    //Kreis[1] in Kreis[0]
    private void calcCircleInCircle(double[][] posRec, double[] rad, double dist){
        double posReceiver[][] = posRec;
        double radius[] = rad;
        double distance = dist;

        double PSmallesDist[] = new double[2];
        PSmallesDist[0] = ((posReceiver[1][0] - posReceiver[0][0])*(1.0 / norm(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1])))*radius[0] + posReceiver[0][0];
        PSmallesDist[1] = ((posReceiver[1][1] - posReceiver[0][1])*(1.0 / norm(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1])))*radius[0] + posReceiver[0][1];

        if (distance > (radius[0] + radius[2]))
        {
            double p1[] = new double[2];
            double p2[] = new double[2];

            // Verbindungspunkte Kreis 3 und 1
            double norm31 = norm(posReceiver[2][0],posReceiver[2][1],posReceiver[0][0],posReceiver[0][1]);
            p1[0] = posReceiver[2][0] + (radius[2] / norm31) * (posReceiver[0][0] - posReceiver[2][0]); //x-P1
            p1[1] = posReceiver[2][1] + (radius[2] / norm31) * (posReceiver[0][1] - posReceiver[2][1]); //y-P1
            p2[0] = posReceiver[0][0] + (radius[0] / norm31) * (posReceiver[2][0] - posReceiver[0][0]); //x-P2
            p2[1] = posReceiver[0][1] + (radius[0] / norm31) * (posReceiver[2][1] - posReceiver[0][1]); //y-P2

            double m31[] = new double[2];
            m31[0] = p1[0] + 0.5 * (p2[0] - p1[0]);	//x
            m31[1] = p1[1] + 0.5 * (p2[1] - p1[1]);	//y

            xPos_func = (m31[0] + PSmallesDist[0])/2;
            yPos_func = (m31[1] + PSmallesDist[1])/2;
            errRad_func = norm(xPos_func, yPos_func, m31[0], m31[1]);
        }
        else
        {
            //Kreisschnittpunkte zwischen Kreis 1 und 3
            double pos13[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[2][0], posReceiver[2][1], radius[0], radius[2]);

            double x1 = PSmallesDist[0];
            double y1 = PSmallesDist[1];

            double x2 = pos13[0][0];
            double y2 = pos13[0][1];

            double x3 = pos13[1][0];
            double y3 = pos13[1][1];

            double a = Math.sqrt((x2 - x3)*(x2 - x3) + (y2 - y3)*(y2 - y3));
            double b = Math.sqrt((x3 - x1)*(x3 - x1) + (y3 - y1)*(y3 - y1));
            double c = Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));

            xPos_func = (x1*a + x2*b + x3*c) / (a + b + c);
            yPos_func = (y1*a + y2*b + y3*c) / (a + b + c);

            double s = (a + b + c) / 2;
            errRad_func = Math.sqrt(((s - a)*(s - b)*(s - c)) / s);
        }
    }

    private void calc2Circle(double[][] posRec, double[] rad){
        double posReceiver[][] = posRec;
        double radius[] = rad;

        double p1[] = new double[2];
        //Kreisschnittpunkte
        double pos[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[1][0], posReceiver[1][1], radius[0], radius[1]);
        //distS1 = Math.sqrt(Math.pow(posReceiver[1][0] - pos[0][0], 2) + Math.pow(posReceiver[1][1] - pos[0][1], 2));
        //distS2 = Math.sqrt(Math.pow(posReceiver[1][0] - pos[1][0], 2) + Math.pow(posReceiver[1][1] - pos[1][1], 2));
        double distS1 = Math.sqrt(Math.pow(posReceiver[2][0] - pos[0][0], 2) + Math.pow(posReceiver[2][1] - pos[0][1], 2));
        double distS2 = Math.sqrt(Math.pow(posReceiver[2][0] - pos[1][0], 2) + Math.pow(posReceiver[2][1] - pos[1][1], 2));
        if (distS1 <= distS2)
        {
            p1[0] = posReceiver[2][0] + (radius[2]/ distS1) * (pos[0][0] - posReceiver[2][0]); //x-P1
            p1[1] = posReceiver[2][1] + (radius[2]/ distS1) * (pos[0][1] - posReceiver[2][1]); //y-P1
            xPos_func = p1[0] + 0.5 * (pos[0][0] - p1[0]);
            yPos_func = p1[1] + 0.5 * (pos[0][1] - p1[1]);
            errRad_func = 0.5 * norm(pos[0][0], pos[0][1],p1[0], p1[1]);
        }
        else
        {
            p1[0] = posReceiver[1][0] + (radius[2]/ distS2) * (pos[1][0] - posReceiver[1][0]); //x-P1
            p1[1] = posReceiver[1][1] + (radius[2]/ distS2) * (pos[1][1] - posReceiver[1][1]); //y-P1
            xPos_func = p1[0] + 0.5 * (pos[1][0] - p1[0]);
            yPos_func = p1[1] + 0.5 * (pos[1][1] - p1[1]);
            errRad_func = 0.5 * norm(pos[1][0], pos[1][1], p1[0], p1[1]);
        }
    }
    private void calc1Circle(double[][] posRec, double[] rad) {
        double[][] posReceiver = posRec;
        double[] radius = rad;

        double p1[] = new double[2];    //x
        double p2[] = new double[2];    //y
        //Kreisschnittpunkte
        double pos23[][] = circlepoints2(posReceiver[1][0], posReceiver[1][1], posReceiver[2][0], posReceiver[2][1], radius[1], radius[2]);
        double pos13[][] = circlepoints2(posReceiver[0][0], posReceiver[0][1], posReceiver[2][0], posReceiver[2][1], radius[0], radius[2]);
        double smallestDist_1, smallestDist_2;
        double distS1_1 = Math.sqrt(Math.pow(pos23[0][0] - pos13[0][0], 2) + Math.pow(pos23[0][1] - pos13[0][1], 2));
        double distS2_1 = Math.sqrt(Math.pow(pos23[1][0] - pos13[0][0], 2) + Math.pow(pos23[1][1] - pos13[0][1], 2));
        double distS1_2 = Math.sqrt(Math.pow(pos23[0][0] - pos13[1][0], 2) + Math.pow(pos23[0][1] - pos13[1][1], 2));
        double distS2_2 = Math.sqrt(Math.pow(pos23[1][0] - pos13[1][0], 2) + Math.pow(pos23[1][1] - pos13[1][1], 2));
        if (distS1_1 <= distS2_1) {
            p1[0] = pos23[0][0]; //x-P1
            p1[1] = pos23[0][1]; //y-P1
            smallestDist_1 = distS1_1;
        } else {
            p1[0] = pos23[1][0]; //x-P1
            p1[1] = pos23[1][1]; //y-P1
            smallestDist_1 = distS2_1;
        }
        if (distS1_2 <= distS2_2) {
            p2[0] = pos23[0][0]; //x-P1
            p2[1] = pos23[0][1]; //y-P1
            smallestDist_2 = distS1_2;
        } else {
            p2[0] = pos23[1][0]; //x-P1
            p2[1] = pos23[1][1]; //y-P1
            smallestDist_2 = distS2_2;
        }
        if (smallestDist_1 <= smallestDist_2) {
            xPos_func = p1[0] + 0.5 * (pos13[0][0] - p1[0]);
            yPos_func = p1[1] + 0.5 * (pos13[0][1] - p1[1]);
        } else {
            xPos_func = p2[0] + 0.5 * (pos13[1][0] - p2[0]);
            yPos_func = p2[1] + 0.5 * (pos13[1][1] - p2[1]);
        }
    }
}
