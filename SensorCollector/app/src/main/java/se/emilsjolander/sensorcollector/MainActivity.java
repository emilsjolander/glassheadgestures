package se.emilsjolander.sensorcollector;

import com.google.android.glass.widget.CardBuilder;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

    static class Classification {
        String label;
        List<float[]> gyroData = new LinkedList<float[]>();
        List<float[]> magnetometerData = new LinkedList<float[]>();
        List<float[]> accelerometerData = new LinkedList<float[]>();
    }

    static class Data {
        List<Classification> classifications = new LinkedList<Classification>();
        long timestamp;
    }

    private SensorManager sensorManager;
    private Sensor gyro;
    private Sensor magnetometer;
    private Sensor accelerometer;
    private Data data;
    private boolean active = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setText("Get ready");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        data = new Data();
        data.timestamp = System.currentTimeMillis();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (data.classifications.size() >= 20) {
                    setText("Thank you");
                    active = false;
                    sensorManager.unregisterListener(MainActivity.this);
                    try {
                        saveCSV();
                    } catch (IOException e) {
                        e.printStackTrace();
                        setText("Something went wrong");
                    }
                    setText("Data saved");
                } else {
                    active = true;
                    Classification c = new Classification();
                    c.label = data.classifications.size() % 2 == 0 ? "yes" : "no";
                    data.classifications.add(c);
                    setText(c.label);
                    h.postDelayed(this, 2000);
                }
            }
        }, 5000);
    }

    private void saveCSV() throws IOException {
        File topDir = new File(Environment.getExternalStorageDirectory(), "gesture_data");
        topDir.mkdir();
        File dir = new File(topDir, ""+data.timestamp);
        dir.mkdir();

        for (Classification c : data.classifications) {
            File f = new File(dir, c.label + "_" + Math.random() + ".csv");
            FileWriter w = new FileWriter(f);
            w.write("gyrX,gyrY,gyrZ,magX,magY,magZ,accX,accY,accZ\n");
            int minSize = Math.min(c.gyroData.size(), Math.min(c.magnetometerData.size(), c.accelerometerData.size()));
            for (int i = 0; i < minSize; i++) {
                float[] g = c.gyroData.get(i);
                float[] m = c.magnetometerData.get(i);
                float[] a = c.accelerometerData.get(i);
                w.write(String.format("%f,%f,%f,%f,%f,%f,%f,%f,%f\n",
                        g[0], g[1], g[2], m[0], m[1], m[2], a[0], a[1], a[2]));
            }
            w.close();
        }
    }

    private void setText(String text) {
        // TODO put text in middle
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.MENU);
        card.setText(text);
        setContentView(card.getView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (active) {
            Classification c = data.classifications.get(data.classifications.size() - 1);
            if (event.sensor == gyro) {
                c.gyroData.add(event.values.clone());
            } else if (event.sensor == magnetometer) {
                c.magnetometerData.add(event.values.clone());
            } else if (event.sensor == accelerometer) {
                c.accelerometerData.add(event.values.clone());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // noop
    }

}
