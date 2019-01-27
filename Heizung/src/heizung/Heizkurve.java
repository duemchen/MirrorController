/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heizung;

import de.horatio.common.HoraFile;
import de.horatio.common.HoraIni;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
public class Heizkurve {

    public static PolynomialFunction getPolynomialFit(List<Point> pList) {
        PolynomialFunction result = null;
        if (pList == null) {
            return result;
        }
        try {

            final WeightedObservedPoints obs = new WeightedObservedPoints();
            for (Point p : pList) {
                obs.add(p.getX(), p.getY());
            }

            final ParametricUnivariateFunction function = new PolynomialFunction.Parametric();
            // Start fit from initial guesses that are far from the optimal
            // values.
            // final SimpleCurveFitter fitter =
            // SimpleCurveFitter.create(function,
            // new double[] { -1e20, 3e15, -5e25 });
            final SimpleCurveFitter fitter = SimpleCurveFitter.create(function, new double[]{-2e20, 1e15, -1e25});
            // 2e2 ist 2*10^2 = 2*100
            final double[] best = fitter.fit(obs.toList());
            // System.out.println("Parameters: " + best.length);
            // funktion ausgeben
            result = new PolynomialFunction(best);
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("PolynomialFunction: " + e);
        }
        return result;
    }

    private static PolynomialFunction getHeizKurve(List<Point> list) {
        // Kurvenfunktion auf Basis der Messwerte ermitteln
        PolynomialFunction f = Heizkurve.getPolynomialFit(list);
        return f;
    }

    public static PolynomialFunction getHeizkurve(String inifile) {
        PolynomialFunction result = null;
        try {
            // lesei
            System.out.println("inidatei: " + HoraFile.getCanonicalPath(inifile));
            String standard = "[{'x':20.0,'y':20.0},{'x':10,'y':35},{'x':0.0,'y':55}]";
            String s = HoraIni.LeseIniString(inifile, "Einstellung", "param", standard, true);
            JSONArray ja = new JSONArray(s);

            System.out.println(ja);
            List<Point> list = new ArrayList<Point>();
            for (int i = 0;
                    i < ja.length();
                    i++) {
                JSONObject jo = ja.getJSONObject(i);
                System.out.println(jo.getDouble("x") + ", " + jo.getDouble("y"));
                list.add(new Point(jo.getDouble("x"), jo.getDouble("y")));
            }

            //
            if (list.size() >= 3) {
                result = getHeizKurve(list);
                for (int i = 0; i <= 20; i++) {
                    System.out.println("x: " + i + ",  y:" + result.value(i));
                }
                double i = 10.56;
                System.out.println("x: " + i + ",  y:" + result.value(i));
            } else {
                System.out.println("Fehler. Mindestens 3 Eichpunkte nötig.");
                System.out.println("param: " + s);
            }

        } catch (JSONException ex) {
            Logger.getLogger(Heizkurve.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ex");
        }
        return result;
    }

    public static void main(String[] args) {
        PolynomialFunction f = getHeizkurve("heizkurve.ini");
        System.out.println(f.value(10));
        System.out.println(f.value(0));
    }

}
