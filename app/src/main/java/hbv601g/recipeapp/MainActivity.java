package hbv601g.recipeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import hbv601g.recipeapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // creating constant keys for shared preferences.
    public static final String SHARED_PREFS = "shared_prefs";

    // key for storing email.
    public static final String USERNAME_KEY = "username_key";

    // key for storing password.
    public static final String PASSWORD_KEY = "password_key";

    // variable for shared preferences.
    SharedPreferences sharedpreferences;
    String username, password;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // setur contentið eftir bottom nav bar vali

        // findViewById(${id}) finnur bottom navigation bar hér
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_recipes, R.id.navigation_ingredients, R.id.navigation_user)
                .build();

        // setur navController sem view-ið fyrir fragmentið í activity_main
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Setur breytingar á actionBar (banner uppi, t.d. titill) þegar destination breytist
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Sýnir á bottomNavigationBar hvað er valið
        NavigationUI.setupWithNavController(binding.navView, navController);


        // Sækir sharedPreferences upplýsingar, basically session
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

    }

    public void updateUser(String username, String password){
        // Nær í allar upplýsingar geymdar í shared preferences
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(USERNAME_KEY, username);
        editor.putString(PASSWORD_KEY, password);
        editor.apply();
    }

    public void removeUser(){
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.apply();
    }

    public String getUserName(){
        return sharedpreferences.getString(USERNAME_KEY, null);
    }


}