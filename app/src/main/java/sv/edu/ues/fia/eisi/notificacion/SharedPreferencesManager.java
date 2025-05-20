package sv.edu.ues.fia.eisi.notificacion;

// En tu clase SharedPreferencesManager.java

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SharedPreferencesManager {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_OBJETIVO_PASOS = "objetivoPasos";
    private static final String KEY_FECHA_ULTIMA_NOTIFICACION_OBJETIVO = "fechaUltimaNotificacionObjetivo";
    // Podrías añadir una clave para el estado si solo quieres una vez al día sin importar la fecha
    // private static final String KEY_NOTIFICADO_OBJETIVO_HOY = "notificadoObjetivoHoy";


    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void guardarObjetivoPasos(int objetivo) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_OBJETIVO_PASOS, objetivo);
        editor.apply();
    }

    public int obtenerObjetivoPasos() {
        return sharedPreferences.getInt(KEY_OBJETIVO_PASOS, 5000); // Valor por defecto si no hay nada guardado
    }

    // Método para obtener la fecha actual en formato "yyyy-MM-dd"
    private String getHoy() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Comprueba si la notificación del objetivo ya se envió hoy
    public boolean haNotificadoObjetivoHoy() {
        String fechaGuardada = sharedPreferences.getString(KEY_FECHA_ULTIMA_NOTIFICACION_OBJETIVO, "");
        return getHoy().equals(fechaGuardada);
    }

    // Marca que la notificación del objetivo se ha enviado hoy
    public void marcarNotificacionObjetivoEnviadaHoy(boolean enviada) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (enviada) {
            editor.putString(KEY_FECHA_ULTIMA_NOTIFICACION_OBJETIVO, getHoy());
        } else {
            // Opcional: si quieres permitir resetearlo manualmente
            editor.remove(KEY_FECHA_ULTIMA_NOTIFICACION_OBJETIVO);
        }
        editor.apply();
    }
}