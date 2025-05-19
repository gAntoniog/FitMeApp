package sv.edu.ues.fia.eisi.notificacion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class StepCounter implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor stepDetectorSensor;
    private StepListener listener;
    private long totalStepsSinceReboot = 0;
    private long previousTotalSteps = -1;
    private long stepsToday = 0;

    public interface StepListener {
        void onStepDetected(long steps);
    }

    public StepCounter(Context context, StepListener listener) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.listener = listener;
        findStepSensors();
    }

    private void findStepSensors() {
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

            if (stepCounterSensor != null) {
                Log.i("StepCounter", "Sensor TYPE_STEP_COUNTER encontrado.");
            } else {
                Log.w("StepCounter", "Sensor TYPE_STEP_COUNTER no encontrado.");
            }

            if (stepDetectorSensor != null) {
                Log.i("StepCounter", "Sensor TYPE_STEP_DETECTOR encontrado.");
            } else {
                Log.w("StepCounter", "Sensor TYPE_STEP_DETECTOR no encontrado.");
            }
        }
    }

    public void startListening() {
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == stepCounterSensor) {
            long currentTotalSteps = (long) event.values[0];
            if (previousTotalSteps == -1) {
                previousTotalSteps = currentTotalSteps;
            }
            stepsToday = currentTotalSteps - previousTotalSteps;
            if (listener != null) {
                listener.onStepDetected(stepsToday);
            }
        } else if (event.sensor == stepDetectorSensor) {
            stepsToday++;
            if (listener != null) {
                listener.onStepDetected(stepsToday);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("StepCounter", "Precisión del sensor (" + sensor.getName() + ") cambiada: " + accuracy);
    }

    public boolean isStepCounterAvailable() {
        return stepCounterSensor != null || stepDetectorSensor != null;
    }



}
