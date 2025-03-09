package hbv601g.recipeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import hbv601g.recipeapp.databinding.ActivityMainBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.ui.recipes.AddIngredientMeasurementFragment;
import hbv601g.recipeapp.ui.recipes.NewRecipeFragment;
import hbv601g.recipeapp.ui.recipes.RecipesFragment;

public class MainActivity extends AppCompatActivity {

    // Lyklar fyrir shared preferences (basically session)
    public static final String SHARED_PREFS = "shared_prefs";
    public static final String USERNAME_KEY = "username_key";
    public static final String PASSWORD_KEY = "password_key";
    public static final String USER_ID_KEY = "uid_key";

    // breyta fyrir shared preferences.
    SharedPreferences sharedpreferences;

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

    /**
     * Setur upplýsingar í shared preferences sem gefinn user,
     * eða hreinsar ef user er null
     * @param user innskráður notandi
     */
    public void updateCurrentUser(User user){
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        if(user != null){
            editor.putString(USERNAME_KEY, user.getUsername());
            editor.putString(PASSWORD_KEY, user.getPassword());
            editor.putLong(USER_ID_KEY, user.getId());
        }
        else editor.clear();

        editor.apply();
    }


    /**
     * Fjarlægir user upplýsingar úr shared preferences til að logga út
     */
    public void removeCurrentUser(){
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void makeToast(int message, int length){
        Toast.makeText(MainActivity.this, message, length).show();
    }

    public String getUserName(){
        return sharedpreferences.getString(USERNAME_KEY, null);
    }
    public long getUserId(){
        return sharedpreferences.getLong(USER_ID_KEY, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //Title bar back press triggers onBackPressed()
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Both navigation bar back press and title bar back press will trigger this method
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ) {
            getFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }
}