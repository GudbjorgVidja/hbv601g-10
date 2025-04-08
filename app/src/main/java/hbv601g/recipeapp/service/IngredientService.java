package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

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
 * A service class for interacting with the networking service in order to make requests related to
 * the Ingredient entity to the external API
 */
public class IngredientService extends Service {
    private NetworkingService mNetworkingService;
    private long mUid;

    public IngredientService(NetworkingService networkingService, long uid) {
        this.mNetworkingService = networkingService;
        this.mUid = uid;
    }

    /**
     * makes a delete request to send to the external API, to try to delete an ingredient
     *
     * @param iid the id of the ingredient to delete
     * @param callback a callback to the fragment
     */
    public void deleteIngredient(long iid, CustomCallback<Ingredient> callback) {
        String url = String.format("ingredient/delete/%s?uid=%s", iid, mUid);

        mNetworkingService.deleteRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Delete ingredient failed");
                callback.onFailure(null);
            }
        });
    }

    /**
     * Makes a patch request to change the title of an ingredient
     *
     * @param iid the id of the ingredient to be renamed
     * @param newTitle the new title of the ingredient
     * @param callback - returns the ingredient with the updated title on success,
     *                   or null on failure
     */
    public void changeIngredientTitle(long iid, String newTitle, CustomCallback<Ingredient> callback) {
        String url = String.format("ingredient/updateTitle/%s?uid=%s", iid, mUid);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", newTitle);
        String data = gson.toJson(requestBody);

        mNetworkingService.patchRequest(url, data, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null)
                    callback.onSuccess(gson.fromJson(jsonElement, Ingredient.class));
                else
                    callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "rename ingredient failed");
                callback.onFailure(null);
            }
        });

    }

    /**
     * Makes a request to get all ingredients accessible to the current user from the external API
     *
     * @param callback - a callback returning a list of the ingredients on success,
     *                   or an empty list on failure
     */
    public void getAllIngredients(CustomCallback<List<Ingredient>> callback) {
        String url = "ingredient/all?uid=" + mUid;

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Type collectionType = new TypeToken<Collection<Ingredient>>() {}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                }
                else callback.onFailure(new ArrayList<>());

            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                callback.onFailure(new ArrayList<>());
            }
        });

    }

    /**
     * makes a post request which is sent to the API, to create an ingredient with the specified
     * attributes.
     *
     * @param title title of the new ingredient
     * @param quantity quantity in a package of the ingredient
     * @param unit the unit of measure for the ingredient
     * @param price the price of this ingredient
     * @param store store name, can be empty or null
     * @param brand brand name, can be empty or null
     * @param isPrivate if the ingredient should be visible to only the creator
     * @param callback - a callback returning the ingredient object on success,
     *                    or null on failure
     */
    public void createIngredient(String title, double quantity, Unit unit, double price,
                                 String store, String brand, boolean isPrivate,
                                 CustomCallback<Ingredient> callback) {

        String url = "ingredient/created?uid=" + mUid;

        if (store.trim().isEmpty()) store = null;
        if (brand.trim().isEmpty()) brand = null;
        Ingredient ingredient = new Ingredient(title, unit, quantity, price, store, brand);
        ingredient.setPrivate(isPrivate);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        String data = gson.toJson(ingredient, Ingredient.class);

        mNetworkingService.postRequest(url, data, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null)
                    callback.onSuccess(gson.fromJson(jsonElement, Ingredient.class));
                else
                    callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                callback.onFailure(null);
            }
        });

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


