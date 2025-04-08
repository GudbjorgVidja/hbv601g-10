package hbv601g.recipeapp;

import android.content.Context;
import android.content.Intent;
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
import hbv601g.recipeapp.entities.User;

/**
 * The main activity of the App. This is the only activity used
 */
public class MainActivity extends AppCompatActivity {

    // Keys for shared preferences (used for session management)
    public static final String SHARED_PREFS = "shared_prefs";
    public static final String USERNAME_KEY = "username_key";
    public static final String PASSWORD_KEY = "password_key";
    public static final String USER_ID_KEY = "uid_key";

    SharedPreferences sharedpreferences;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // setur contentið eftir bottom nav bar vali

        // findViewById(${id}) finds the bottom navigation bar
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_recipes, R.id.nav_ingredients, R.id.nav_user)
                .build();

        // sets navController as the view for the fragment in activity_main
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Sets changes for the action bar (the top banner, i.e. title) when the destination changes
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Shows on the bottomNavigationBar what has been selected
        NavigationUI.setupWithNavController(binding.navView, navController);

        menuSelectionListener(navController);

        // Gets sharedPreferences information
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

    }

    /**
     * Ensures that the correct menu items appear selected, and that a second selection of the
     * current menu item navigates to the first page of that menu item
     *
     * @param navController the NavController used to navigate between fragments
     */
    private void menuSelectionListener(NavController navController) {
        binding.navView.setOnItemSelectedListener(item -> {
            if (binding.navView.getSelectedItemId() == item.getItemId()) {
                navController.popBackStack(item.getItemId(), false);
            }
            NavigationUI.onNavDestinationSelected(item, navController);
            return true;
        });
    }

    /**
     * Sets information in shared preferences as the given user, or empties it if the user is null
     *
     * @param user the current user
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
     * Removes user information from shared preferences to log out, and makes a new
     * intent to start the activity again
     */
    public void removeCurrentUser(){
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    /**
     * Makes a toast using the current activity as well as the given message and length
     * @param message The resource id of the string resource to use. Can be formatted text.
     * @param length How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
     */
    public void makeToast(int message, int length){
        Toast.makeText(MainActivity.this, message, length).show();
    }

    /**
     * Gets the username of the user who is currently logged in, from the shared preferences
     * @return the username of the current user, or null if no user is logged in
     */
    public String getUserName(){
        return sharedpreferences.getString(USERNAME_KEY, null);
    }

    /**
     * Gets the userID of the user who is currently logged in, from the shared preferences
     * @return the id of the current user, or 0 if no user is logged in
     */
    public long getUserId(){
        return sharedpreferences.getLong(USER_ID_KEY, 0);
    }

    /**
     * handles the selection of an item from the options menu
     * @param item The menu item that was selected.
     *
     * @return  false to allow normal menu processing to proceed, true to consume it here
     */
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