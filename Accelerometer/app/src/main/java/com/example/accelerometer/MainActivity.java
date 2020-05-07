package com.example.accelerometer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.classifiers.meta.FilteredClassifier;
import static weka.core.SerializationHelper.read;


public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    Sensor accelerometer;
    long starttime = 0;
    double[] mag = new double[50];
    int i = 0;
    double min = 0;
    double max = 0;
    double var = 0;
    double std = 0;
    int interval = 2; // 2 seconds
    TextView text1;
    List<List<String>> rows = new LinkedList<>();
    boolean activityRunning;
    int secondPassed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text1 = (TextView) findViewById(R.id.textview1);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (activityRunning) {
            double magnitude = new Double(0);
            List<String> row = new LinkedList<>();
            if (sensorEvent.values.length == 1) {
                magnitude =
                        Math.sqrt(sensorEvent.values[0] * sensorEvent.values[0]);
            } else if (sensorEvent.values.length == 2) {
                magnitude =
                        Math.sqrt(sensorEvent.values[0] * sensorEvent.values[0] + sensorEvent.values[1] * sensorEvent.values[1]);
            } else if (sensorEvent.values.length == 3) {
                magnitude =
                        Math.sqrt(sensorEvent.values[0] * sensorEvent.values[0] + sensorEvent.values[1] * sensorEvent.values[1] + sensorEvent.values[2] *
                                sensorEvent.values[2]);
            }
            sensorEvent.
            long millis = System.currentTimeMillis() - starttime;
            int seconds = (int) (millis / 1000);
            seconds = seconds % 60;
            secondPassed = secondPassed + seconds;
            if (seconds % interval == 0 || i == 50) {
                min = minimum(mag);
                max = maximum(mag);
                var = variance(mag);
                std = standardDeviation(mag);
                Arrays.fill(mag, 0.0);
                row.add(String.valueOf(seconds));
                row.add(String.valueOf(min));
                row.add(String.valueOf(max));
                row.add(String.valueOf(std));
                row.add(String.valueOf(var));
                text1.setText("Features:\n\n min " + min + " \n max " + max + " \nvar " + var + " \nstd " + std + " ");
                rows.add(row);
                i = 0;
            } else {
                mag[i] = magnitude;
                i++;
                text1.setText("Features:\n\n min " + min + " \n max " + max + " \nvar " + var + " \nstd " + std + " ");
            }
            if (secondPassed >= 300) {
                createCSV(rows);
            }
        }
    }

    public static double maximum(double data[]) {
        if (data == null || data.length == 0) return 0.0;
        int length = data.length;
        double MAX = data[0];
        for (int i = 1; i < length; i++) {
            MAX = data[i] > MAX ? data[i] : MAX;
        }
        return MAX;
    }

    public static double minimum(double data[]) {
        if (data == null || data.length == 0) return 0.0;
        int length = data.length;
        double MIN = data[0];
        for (int i = 1; i < length; i++) {
            MIN = data[i] < MIN ? data[i] : MIN;
        }
        return MIN;
    }

    public static double variance(double data[]) {
        if (data == null || data.length == 0) return 0.0;
        int length = data.length;
        double average = 0, s = 0, sum = 0;
        for (int i = 0; i < length; i++) {
            sum = sum + data[i];
        }
        average = sum / length;
        for (int i = 0; i < length; i++) {
            s = s + Math.pow(data[i] - average, 2);
        }
        s = s / length;
        return s;
    }

    public static double standardDeviation(double
                                                   data[]) {
        if (data == null || data.length == 0) return 0.0;
        double s = variance(data);
        s = Math.sqrt(s);
        return s;
    }

    public static double mean(double data[]) {
        if (data == null || data.length == 0) return
                0.0;
        int length = data.length;
        double Sum = 0;
        for (int i = 0; i < length; i++)
            Sum = Sum + data[i];
        return Sum / length;
    }

    public static double zeroCrossingRate(double
                                                  data[]) {
        int length = data.length;
        double num = 0;
        for (int i = 0; i < length - 1; i++) {
            if (data[i] * data[i + 1] < 0) {
                num++;
            }
        }
        return num / length;
    }

    public void createCSV(List<List<String>> rows) {
        try {
            File path = getApplicationContext().getFilesDir();
            File file = new File(path, "newFile"+secondPassed+".csv");
            System.out.println(file.getCanonicalPath());
            FileWriter csvWriter = new FileWriter(file);
            csvWriter.write("Second");
            csvWriter.write(",");
            csvWriter.write("Min");
            csvWriter.write(",");
            csvWriter.write("Max");
            csvWriter.write(",");
            csvWriter.write("Std");
            csvWriter.write(",");
            csvWriter.write("Var");
            csvWriter.write("\n");

            for (List<String> rowData : rows) {
                for(String rd :rowData){
                    csvWriter.write(rd);
                    csvWriter.write(",");
                }
                csvWriter.write("\n");
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static void onButtonClick(View v){

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this);
    }

}
