package sv.edu.ues.fia.eisi.notificacion;

import android.Manifest;
import android.app.NotificationManager;
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
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 102;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS); // Define esta constante
            }
        }

        objetivoButton.setOnClickListener(v -> mostrarDialogoObjetivo());
        notificacionButton.setOnClickListener(v -> enviarNotificacionBasica());

        mostrarGraficoSemanal();
        mostrarGraficoMensual();
        actualizarObjetivoUI();
    }
    protected void onStepDetected(long pasos) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pasosTextView.setText("Pasos: " + pasos);
                // También podrías actualizar la barra de progreso aquí si tienes una
                int objetivo = prefsManager.obtenerObjetivoPasos();
                // Comprueba si se alcanzó el objetivo y si la notificación para hoy aún no se ha enviado
                if (pasos >= objetivo && !prefsManager.haNotificadoObjetivoHoy()) {
                    enviarNotificacionObjetivoAlcanzado(pasos, objetivo);
                    prefsManager.marcarNotificacionObjetivoEnviadaHoy(true); // Marcar que la notificación se envió
                }
            }
        });
    }
    private void actualizarPasosUI(long pasos) {
        pasosTextView.setText("Pasos: " + pasos);
        int objetivo = prefsManager.obtenerObjetivoPasos();
        // Comprueba si se alcanzó el objetivo y si la notificación para hoy aún no se ha enviado
        if (pasos >= objetivo && !prefsManager.haNotificadoObjetivoHoy()) {
            enviarNotificacionObjetivoAlcanzado(pasos, objetivo);
            prefsManager.marcarNotificacionObjetivoEnviadaHoy(true); // Marcar que la notificación se envió
        }

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

    //Manejo de permisos de actividad fisica para conteo de pasos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                Log.d("Permissions", "Permiso ACTIVITY_RECOGNITION concedido.");
                if (stepCounter != null && stepCounter.isStepCounterAvailable()) {
                    stepCounter.startListening();
                }
                // Actualizar UI o habilitar funcionalidad de conteo de pasos

            } else {
                // Permiso denegado
                Log.d("Permissions", "Permiso ACTIVITY_RECOGNITION denegado.");
                // Explicar por qué el permiso es necesario y cómo pueden habilitarlo manualmente

                // Actualizar UI para reflejar que la funcionalidad no está disponible
                pasosTextView.setText("Conteo de pasos deshabilitado. Se requiere permiso.");
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
            int importancia = NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel canal = new android.app.NotificationChannel(getPackageName(), nombre, importancia);
            canal.setDescription(descripcion);
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }
    //Envio de notificacion y manejo de solicitud de permiso para enviar notificaciones
    private void enviarNotificacionBasica() {
        crearCanalNotificacion();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Solicitar el permiso. La notificación no se enviará esta vez,
                // pero se podrá enviar la próxima si el usuario concede el permiso.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
                Toast.makeText(this, "Se necesita permiso para mostrar notificaciones.", Toast.LENGTH_LONG).show();
                return; // Salir para no intentar enviar la notificación sin permiso
            }
        }
        Intent resultadoIntent = new Intent(this, MainActivity.class);
        androidx.core.app.TaskStackBuilder stackBuilder = androidx.core.app.TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultadoIntent);
        android.app.PendingIntent resultadoPendingIntent = stackBuilder.getPendingIntent(
                0,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, getPackageName())
                .setSmallIcon(R.drawable.ic_notification) //icono de notificacion
                .setContentTitle("¡Progreso de Pasos!")
                .setContentText("Llevas"+pasosTextView.getText()+" pasos hoy.")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultadoPendingIntent)
                .setAutoCancel(true);

        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    //se envia notificacion automaticamente al alcanzar el objetivo diario de pasos
    private void enviarNotificacionObjetivoAlcanzado(long pasosAlcanzados, int objetivo) {
        crearCanalNotificacion(); //Se manda a llamar el metodo para crear el canal de notificacion

        Intent resultadoIntent = new Intent(this, MainActivity.class);
        // Flags para asegurar que la actividad se abra correctamente
        resultadoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        androidx.core.app.TaskStackBuilder stackBuilder = androidx.core.app.TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultadoIntent);
        android.app.PendingIntent resultadoPendingIntent = stackBuilder.getPendingIntent(
                (int) System.currentTimeMillis(), // Usar un requestCode único para evitar problemas con PendingIntents
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        String mensaje = "¡Felicidades! Alcanzaste tu objetivo diario de " + objetivo + " pasos. Sigue moviendote!!";

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, getPackageName())
                .setSmallIcon(R.drawable.logro)
                .setContentTitle("¡Objetivo de Pasos Alcanzado!")
                .setContentText(mensaje)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH) // Prioridad alta para notificaciones importantes
                .setContentIntent(resultadoPendingIntent)
                .setAutoCancel(true); // La notificación se cierra al tocarla

        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build()); // ID 2 para esta notificación específica
    }

}
