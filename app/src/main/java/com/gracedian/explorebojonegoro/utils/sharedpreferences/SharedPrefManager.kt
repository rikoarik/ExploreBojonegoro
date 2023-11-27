import android.content.Context

object SharedPrefManager {
    private const val PREF_NAME = "MyPrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_FIRST_INSTALL = "isFirstInstall"

    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setFirstInstall(context: Context, isFirstInstall: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_FIRST_INSTALL, isFirstInstall)
        editor.apply()
    }

    fun isFirstInstall(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_FIRST_INSTALL, true)
    }
    fun logout(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.apply()
    }
    fun saveFilterPreferences(context: Context, category: String, rating: Float, jarakMax: Int) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selectedCategory", category)
        editor.putFloat("selectedRating", rating)
        editor.putInt("selectedJarakMax", jarakMax)
        editor.apply()
    }
    fun getFilterPreferences(context: Context): Triple<String, Float, Int> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val category = sharedPreferences.getString("selectedCategory", "Wisata Alam") ?: "Wisata Alam"
        val rating = sharedPreferences.getFloat("selectedRating", 0.0F)
        val jarakMax = sharedPreferences.getInt("selectedJarakMax", 0)
        return Triple(category, rating, jarakMax)
    }


}
