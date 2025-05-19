package sv.edu.ues.fia.eisi.notificacion;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "StepAppPrefs";
    private static final String KEY_DAILY_GOAL = "daily_goal";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SharedPreferencesManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void guardarObjetivoPasos(int goal) {
        editor.putInt(KEY_DAILY_GOAL, goal);
        editor.apply();
    }

    public int obtenerObjetivoPasos() {
        return prefs.getInt(KEY_DAILY_GOAL, 5000); // Valor predeterminado de 5000 pasos
    }
}