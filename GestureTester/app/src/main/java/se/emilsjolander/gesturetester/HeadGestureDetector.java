package se.emilsjolander.gesturetester;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussian;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussianFactory;

/**
 * Created by emilsjolander on 15/11/14.
 */
public class HeadGestureDetector implements SensorEventListener {

    public enum Gesture {
        NONE,
        NO,
        YES,
    }

    public interface Listener {
        void onGestureDetected(Gesture gesture);
    }

    private Listener listener;
    private Hmm<ObservationVector> nohmm;
    private Hmm<ObservationVector> yeshmm;
    private SensorManager sensorManager;
    private Sensor gyro;
    private List<float[]> gyroData = new LinkedList<float[]>();
    private Handler handler;

    public HeadGestureDetector(Context context) {
        nohmm = buildNoHmm();
        yeshmm = buildYesHmm();

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        handler = new Handler();
    }

    private Hmm<ObservationVector> buildNoHmm() {
        Hmm<ObservationVector> hmm = new Hmm<ObservationVector>(4, new OpdfMultiGaussianFactory(2));

        hmm.setPi(0, 0.054723849043553804);
        hmm.setPi(1, 0.15532792186550443);
        hmm.setPi(2, 0.27417415544323);
        hmm.setPi(3, 0.5157740736477119);

        hmm.setOpdf(0, new OpdfMultiGaussian(
                new double[]{-0.015, -0.023},
                new double[][] {{0.004108684898494467, -0.01936501982791085},
                                {-0.01936501982791085, 0.5502663230160408}}));

        hmm.setOpdf(1, new OpdfMultiGaussian(
                new double[]{-0.014, -0.007},
                new double[][] {{0.0015486272411397283, -0.001954152985776972},
                                {-0.001954152985776972, 0.07574162097416264}}));

        hmm.setOpdf(2, new OpdfMultiGaussian(
                new double[]{-0.032, -0.044},
                new double[][] {{0.09999176374519612, -0.10246782378534172},
                                {-0.10246782378534172, 1.3128780859704885}}));

        hmm.setOpdf(3, new OpdfMultiGaussian(
                new double[]{-0.013, -0.01},
                new double[][] {{7.905282773533901E-4, -8.820195431558636E-5},
                                {-8.820195431558636E-5, 0.012335915115798967}}));

        hmm.setAij(0, 0, 0.673);
        hmm.setAij(0, 1, 0.146);
        hmm.setAij(0, 2, 0.092);
        hmm.setAij(0, 3, 0.089);

        hmm.setAij(1, 0, 0.192);
        hmm.setAij(1, 1, 0.274);
        hmm.setAij(1, 2, 0.006);
        hmm.setAij(1, 3, 0.528);

        hmm.setAij(2, 0, 0.1);
        hmm.setAij(2, 1, 0.005);
        hmm.setAij(2, 2, 0.894);
        hmm.setAij(2, 3, 0.001);

        hmm.setAij(3, 0, 0.049);
        hmm.setAij(3, 1, 0.221);
        hmm.setAij(3, 2, 0.001);
        hmm.setAij(3, 3, 0.73);

        return hmm;
    }

    private Hmm<ObservationVector> buildYesHmm() {
        Hmm<ObservationVector> hmm = new Hmm<ObservationVector>(4, new OpdfMultiGaussianFactory(2));

        hmm.setPi(0, 0.019883898962603737);
        hmm.setPi(1, 0.08086629533836585);
        hmm.setPi(2, 0.30184580184435617);
        hmm.setPi(3, 0.5974040038546736);

        hmm.setOpdf(0, new OpdfMultiGaussian(
                new double[]{-0.064, -0.012},
                new double[][] {{0.4155301531079912, -0.01026286723024947},
                                {-0.01026286723024947, 0.005319758443365391}}));

        hmm.setOpdf(1, new OpdfMultiGaussian(
                new double[]{-0.006, -0.011},
                new double[][] {{0.04140818466217711, -6.471338955465316E-4},
                                {-6.471338955465316E-4, 0.001730349704948936}}));

        hmm.setOpdf(2, new OpdfMultiGaussian(
                new double[]{0.096, -0.009},
                new double[][] {{0.4235715118696063, -0.04506109071710214},
                                {-0.04506109071710214, 0.18946907977308686}}));

        hmm.setOpdf(3, new OpdfMultiGaussian(
                new double[]{-0.011, -0.009},
                new double[][] {{0.00421512266588428, 6.688852810667707E-5},
                                {6.688852810667707E-5, 6.048807030806242E-4}}));

        hmm.setAij(0, 0, 0.851);
        hmm.setAij(0, 1, 0.084);
        hmm.setAij(0, 2, 0.043);
        hmm.setAij(0, 3, 0.021);

        hmm.setAij(1, 0, 0.215);
        hmm.setAij(1, 1, 0.297);
        hmm.setAij(1, 2, 0.002);
        hmm.setAij(1, 3, 0.486);

        hmm.setAij(2, 0, 0.091);
        hmm.setAij(2, 1, 0.002);
        hmm.setAij(2, 2, 0.907);
        hmm.setAij(2, 3, 0.000);

        hmm.setAij(3, 0, 0.013);
        hmm.setAij(3, 1, 0.124);
        hmm.setAij(3, 2, 0.000);
        hmm.setAij(3, 3, 0.863);

        return hmm;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void start() {
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        handler.postDelayed(detect, 1000);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(detect);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == gyro) {
            gyroData.add(event.values.clone());
            if (gyroData.size() > 60) {
                gyroData.remove(0);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // noop
    }

    private final Runnable detect = new Runnable() {
        @Override
        public void run() {
            List<ObservationVector> seq = new ArrayList<ObservationVector>();
            for (int i = 0; i < gyroData.size(); i++) {
                seq.add(new ObservationVector(new double[]{gyroData.get(i)[0], gyroData.get(i)[1]}));
            }
            double ratio = yeshmm.probability(seq) / nohmm.probability(seq);
            Log.d("emil", "ratio = "+ratio);
            if (ratio < 1.0E-50) {
                listener.onGestureDetected(Gesture.NO);
            } else if (ratio > 1.0E50) {
                listener.onGestureDetected(Gesture.YES);
            } else {
                listener.onGestureDetected(Gesture.NONE);
            }
            handler.postDelayed(this, 500);
        }
    };

}
