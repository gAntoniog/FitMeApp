package sv.edu.ues.fia.eisi.notificacion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 101;
    private TextView pasosTextView;
    private Button objetivoButton;
    private Button notificacionButton;
    private BarChart barChartSemanal;
    private LineChart lineChartMensual;

    private StepCounter stepCounter;
    private SharedPreferencesManager prefsManager;
// private DatabaseHelper dbHelper; // Si usas base de datos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pasosTextView = findViewById(R.id.pasos_text_view);
        objetivoButton = findViewById(R.id.objetivo_button);
        notificacionButton = findViewById(R.id.notificacion_button);
        barChartSemanal = findViewById(R.id.barChartSemanal);
        lineChartMensual = findViewById(R.id.lineChartMensual);

        prefsManager = new SharedPreferencesManager(this);
        // dbHelper = new DatabaseHelper(this); // Inicializa si usas base de datos
        stepCounter = new StepCounter(this, this::actualizarPasosUI); // Usa lambda para el listener

        // Verificar y solicitar permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        } else {
            stepCounter.startListening();
        }

        objetivoButton.setOnClickListener(v -> mostrarDialogoObjetivo());
        notificacionButton.setOnClickListener(v -> enviarNotificacionBasica());

        mostrarGraficoSemanal();
        mostrarGraficoMensual();
        actualizarObjetivoUI();
    }

    private void actualizarPasosUI(long pasos) {
        pasosTextView.setText("Pasos: " + pasos);
        // Aquí podrías guardar los pasos diarios usando prefsManager o dbHelper
    }

    private void actualizarObjetivoUI() {
        int objetivo = prefsManager.obtenerObjetivoPasos();
        objetivoButton.setText("Objetivo: " + objetivo);
    }

    private void mostrarDialogoObjetivo() {
        // Implementa un diálogo para que el usuario configure el objetivo

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Establecer Objetivo de Pasos");

        // Configurar el input (EditText)
        final EditText input = new EditText(this);
        // Especificar el tipo de input que esperamos, en este caso, números
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        // Opcional: Mostrar el objetivo actual como pista o texto inicial
        input.setHint("Objetivo actual: " + prefsManager.obtenerObjetivoPasos());
        builder.setView(input);

        // Configurar los botones del diálogo
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String textoObjetivo = input.getText().toString();
            if (!textoObjetivo.isEmpty()) {
                try {
                    int nuevoObjetivo = Integer.parseInt(textoObjetivo);
                    if (nuevoObjetivo > 0) { // Asegurarse de que el objetivo sea positivo
                        prefsManager.guardarObjetivoPasos(nuevoObjetivo);
                        actualizarObjetivoUI();
                        Toast.makeText(this, "Objetivo guardado: " + nuevoObjetivo, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Por favor, introduce un número positivo.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Por favor, introduce un número válido.", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancelar", (dialog, which) -> {

        dialog.dismiss();
    })
            .show();



    }

    private void mostrarGraficoSemanal() {
        // Aquí obtendrías los datos de pasos de la semana desde tu almacenamiento local o Google Fit
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1f, 1500));
        entries.add(new BarEntry(2f, 2200));
        entries.add(new BarEntry(3f, 1800));
        entries.add(new BarEntry(4f, 2500));
        entries.add(new BarEntry(5f, 2000));
        entries.add(new BarEntry(6f, 2800));
        entries.add(new BarEntry(7f, 1900));

        BarDataSet dataSet = new BarDataSet(entries, "Pasos Diarios");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(dataSet);
        barChartSemanal.setData(barData);
        barChartSemanal.getDescription().setEnabled(false);
        barChartSemanal.invalidate();
    }

    private void mostrarGraficoMensual() {
        // Aquí obtendrías los datos de pasos del mes
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1f, 6000));
        entries.add(new Entry(5f, 8500));
        entries.add(new Entry(10f, 4500));
        entries.add(new Entry(15f, 7000));
        entries.add(new Entry(20f, 9000));
        entries.add(new Entry(25f, 5500));
        entries.add(new Entry(30f, 7800));

        LineDataSet dataSet = new LineDataSet(entries, "Pasos Diarios");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setValueTextColor(android.R.color.black);
        LineData lineData = new LineData(dataSet);
        lineChartMensual.setData(lineData);
        lineChartMensual.getDescription().setEnabled(false);
        lineChartMensual.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                stepCounter.startListening();
            } else {
                Toast.makeText(this, "Permiso de reconocimiento de actividad denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
            stepCounter.startListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stepCounter.stopListening();
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombre = "Pasos Diarios";
            String descripcion = "Notificaciones sobre tu progreso de pasos diarios";
            int importancia = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            android.app.NotificationChannel canal = new android.app.NotificationChannel(getPackageName(), nombre, importancia);
            canal.setDescription(descripcion);
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    private void enviarNotificacionBasica() {
        crearCanalNotificacion();
        Intent resultadoIntent = new Intent(this, MainActivity.class);
        androidx.core.app.TaskStackBuilder stackBuilder = androidx.core.app.TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultadoIntent);
        android.app.PendingIntent resultadoPendingIntent = stackBuilder.getPendingIntent(
                0,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, getPackageName())
                .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener este icono
                .setContentTitle("¡Progreso de Pasos!")
                .setContentText("Llevas"+pasosTextView.getText()+" pasos hoy.") // Reemplaza X con el número real
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultadoPendingIntent)
                .setAutoCancel(true);

        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

}