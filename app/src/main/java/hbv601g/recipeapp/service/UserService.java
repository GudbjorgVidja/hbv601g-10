package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;

public class UserService extends Service {
    private NetworkingService networkingService;
    private JsonElement mElement;

    public UserService(NetworkingService networkingService){
        this.networkingService=networkingService;
    }

    /**
     * Athugar hvort notandi með gefið notandanafn og lykilorð sé í gagnagrunninum,
     * skilar honum ef hann er til en annars null
     * @param username notandanafn til að skrá inn
     * @param password lykilorð notanda
     * @return User object ef innskráning tókst, annars null
     */
    public User logIn(String username, String password){
        String url = "user/login";
        url += String.format("?username=%s&password=%s",username, password);

        try {
            mElement = networkingService.getRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "Login failed");
            //throw new RuntimeException(e);
        }

        User user = null;
        if(mElement != null){
            Gson gson = new Gson();
            user = gson.fromJson(mElement, User.class);
            Log.d("API", "user object, name:" + user.getUsername());
        }

        return user;
    }

    public User signup(String username, String password){
        String url = "user/signup";
        String params = String.format("?username=%s&password=%s",username, password);

        User user = null;
        try {
            mElement = networkingService.postRequest(url + params, null);
        } catch (IOException e) {
            Log.d("Networking exception", "Signup failed");
        }

        if(mElement != null){
            Gson gson = new Gson();
            user = gson.fromJson(mElement, User.class);
            Log.d("API", "user object, name:" + user.getUsername());
        }

        return user;
    }

    /**
     * Skilar pantry hjá notanda með gefið user id
     *
     * @param uid user id
     * @return Lista af IngredientMeasurements
     */
    public List<IngredientMeasurement> getUserPantry(long uid) {
        String url = "user/pantry";
        String params ="?uid=" + uid;
        ArrayList<IngredientMeasurement> pantry = new ArrayList<>();

        /*if(uid == 0){
            return pantry;
        }*/

        try {
            mElement = networkingService.getRequest(url + params);
            Log.d("UserService", "fetched element is: " + mElement);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to get user pantry");
            return pantry;
        }

        if(mElement != null && !mElement.isJsonNull()){
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            if(!mElement.isJsonArray()) return null;
            Log.d("UserService", "element: " + mElement);

            JsonArray arr = mElement.getAsJsonArray();


            Type collectionType = new TypeToken<Collection<IngredientMeasurement>>(){}.getType();
            pantry = gson.fromJson(arr, collectionType);
        }
        return pantry;
    }

    /**
     * API call to remove an ingredient from the users pantry.
     * @param uid - User ID
     * @param iid - Ingredient ID
     * @return True if the API call returns an empty response, else false.
     */
    public boolean removeIngredientFromPantry(long uid, long iid){
        String url = "user/pantry/delete";
        String params = String.format("?iid=%s&uid=%s", iid, uid);


        try {
            mElement = networkingService.putRequest(url + params, null);
            Log.d("API", "remove pantry response: " + mElement);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to delete item from pantry");
        }

        boolean itemDeleted = false;

        if(mElement == null || mElement.isJsonNull()){
            itemDeleted = true;
            Log.d("API", "item deleted: " + itemDeleted);
        }
        return itemDeleted;
    }

    /**
     *
     * @param uid - User ID
     * @param iid - Ingredient ID
     * @param unit - Unit
     * @param qty - Quantity
     * @return IngredientMeasurement that was added to the pantry
     */
    public IngredientMeasurement addIngredientToPantry(long uid, long iid, Unit unit, double qty){
        String url = "user/pantry/add";
        String params = String.format("?iid=%s&unit=%s&qty=%s&uid=%s", iid, unit.toString().toUpperCase(), qty, uid);

        IngredientMeasurement ingredient = null;

        try{
            mElement = networkingService.putRequest(url + params, null);
            Log.d("API", "Ingredient added: " + mElement);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to add ingredient to pantry");
        }

        if(mElement != null && !mElement.isJsonNull()){
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            JsonElement res = mElement.getAsJsonObject();
            ingredient = gson.fromJson(res, IngredientMeasurement.class);
        }
        return ingredient;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
