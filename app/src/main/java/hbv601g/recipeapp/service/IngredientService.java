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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;

/**
 * Service class for ingredients
 */
public class IngredientService extends Service {

    private NetworkingService mNetworkingService;
    private long mUid;
    private JsonElement mElement;

    public IngredientService(NetworkingService networkingService, long uid){
        this.mNetworkingService = networkingService;
        this.mUid = uid;
    }

    /**
     * makes a delete request to send to the external API, to try to delete an ingredient
     * @param iid - the id of the ingredient to delete
     * @return boolean value indicating success
     */
    public boolean deleteIngredient(long iid){
        String url = String.format("ingredient/delete/%s?uid=%s",iid, mUid);
        try {
            mElement = mNetworkingService.deleteRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "Delete ingredient failed");
        }

        boolean ingredientDeleted = false;
        if(mElement != null){
            ingredientDeleted = mElement.getAsBoolean();
            Log.d("API", "ingredient deleted: " + ingredientDeleted);
        }
        return ingredientDeleted;
    }

    /**
     * Makes a patch request to change the title of an ingredient
     * @param iid - the id of the ingredient to be renamed
     * @param newTitle - the new title of the ingredient
     * @return - the ingredient with the updated title
     */
    public Ingredient changeIngredientTitle(long iid, String newTitle){
        String url = String.format("ingredient/updateTitle/%s?uid=%s",iid,mUid);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title",newTitle);
        String data = gson.toJson(requestBody);

        try {
            mElement = mNetworkingService.patchRequest(url, data);
        } catch (IOException e) {
            Log.d("Networking exception", "rename ingredient failed");
            mElement = null;
        }

        Ingredient ingredient = null;
        if(mElement != null){
            ingredient = gson.fromJson(mElement, Ingredient.class);
        }
        return ingredient;
    }

    public List<Ingredient> getAllIngredients(){
        String url = "ingredient/all";
        url += "?uid="+ mUid;

        try {
            mElement = mNetworkingService.getRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "get ingredients failed");
        }

        ArrayList<Ingredient> ingredients = new ArrayList<>();
        if(mElement != null){
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            if(!mElement.isJsonArray()) return null;

            JsonArray array = mElement.getAsJsonArray();

            Type collectionType = new TypeToken<Collection<Ingredient>>(){}.getType();
            ingredients = gson.fromJson(array, collectionType);
        }
        else throw new NullPointerException("Ingredient list is null");

        return ingredients;
    }

    /**
     * Makes a get request to get all ingredients visible to the user.
     * @param callback - a callback to return the result
     */
    public void getAllIngredients(CustomCallback<List<Ingredient>> callback){
        String url = "ingredient/all";
        url += "?uid="+ mUid;
        mElement = null;

        CustomCallback<JsonElement> customCallback = new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                Log.d("Callback", "onSuccess í service");
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                JsonArray array = jsonElement.getAsJsonArray();

                Type collectionType = new TypeToken<Collection<Ingredient>>(){}.getType();
                callback.onSuccess(gson.fromJson(array, collectionType));
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Callback", "onFailure í service");
                callback.onFailure(new ArrayList<>());
            }
        };

        mNetworkingService.getRequest(url, customCallback);
    }


    /**
     * makes a post request which is sent to the API, to create an
     * ingredient with the specified attributes.
     * @param title - title of the new ingredient
     * @param quantity - quantity in a package of the ingredient
     * @param unit - the unit of measure for the ingredient
     * @param price - the price of this ingredient
     * @param store - store name, can be empty or null
     * @param brand - brand name, can be empty or null
     * @param isPrivate - if the ingredient should be visible to only the creator
     * @return the ingredient object
     */
    public Ingredient createIngredient(String title, double quantity, Unit unit, double price, String store, String brand, boolean isPrivate){
        String url = "ingredient/created?uid=" + mUid;

        if(store.trim().isEmpty()) store = null;
        if(brand.trim().isEmpty()) brand = null;
        Ingredient ingredient = new Ingredient(title, unit, quantity, price, store, brand);
        ingredient.setPrivate(isPrivate);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        String data = gson.toJson(ingredient, Ingredient.class);
        try {
        mElement = mNetworkingService.postRequest(url, data);
        } catch (IOException e) {
            Log.d("Networking exception", "create ingredient failed");
            mElement = null;
        }

        if(mElement != null && mElement.isJsonObject()){
            ingredient = gson.fromJson(mElement, Ingredient.class);
        }
        else throw new NullPointerException("Failed to create ingredient");

        return ingredient;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


