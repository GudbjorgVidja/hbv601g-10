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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;

/**
 * Service class for recipe list API calls
 */
public class RecipeListService extends Service {
    private NetworkingService mNetworkingService;
    private long mUid;

    public RecipeListService(NetworkingService networkingService, long uid) {
        mNetworkingService = networkingService;
        mUid = uid;
    }

    /**
     *  TODO: Breyta útfærslu í bakenda, líklega ekki gott að nota requestParams fyrir allt. //
     *        nota frekar requestBody?
     * Creates a recipe list and adds it to the database
     * @param title       - title of the list, max 50 characters
     * @param description - description of the list, max 250 chars, may be null
     * @param isPrivate   - true if the list is private, otherwise false
     * @return RecipeList object added to the db, or null
     */
    public void createRecipeList(String title, String description, boolean isPrivate, CustomCallback<RecipeList> callback) {
        String url = String.format("list/new?uid=%s&title=%s&description=%s&isPrivate=%b", mUid, title, description, isPrivate);

        mNetworkingService.postRequest(url, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Gson gson = new GsonBuilder().create();
                    callback.onSuccess(gson.fromJson(jsonElement, RecipeList.class));
                }
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to create recipe list");
                callback.onFailure(null);
            }
        });

    }


    /**
     * Makes a request to get all recipe lists for the current user, public and private
     *
     * @return all of the user's recipe lists
     */


    public void getUserRecipeLists(long uid, CustomCallback<List<RecipeList>> callback) {
        String url = "list/user/" + uid + "?uid=" + mUid;
        mNetworkingService.getRequest(url, new CustomCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Log.d("Callback", "onSuccess í service");
                    Gson gson = new GsonBuilder().create();
                    Type collectionType = new TypeToken<Collection<RecipeList>>() {}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                }
                else callback.onSuccess(new ArrayList<>());
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Callback", "onFailure í service");
                callback.onFailure(new ArrayList<>());
            }
        });

    }


    // TODO: á þessi að skila listanum???
    /**
     * Adds the given recipe to the given recipe list.
     * @param recipeId - id of the recipe to add
     * @param listId - id of the list to be added to
     * @param callback - callback returning the new list size on success, or null on failure
     */
    public void addRecipeToList(long recipeId, long listId, CustomCallback<Integer> callback) {
        String url = String.format("list/addRecipe?recipeID=%s&listID=%s&uid=%s", recipeId, listId, mUid);

        mNetworkingService.putRequest(url, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Gson gson = new GsonBuilder().create();
                    RecipeList recipeList = gson.fromJson(jsonElement, RecipeList.class);
                    callback.onSuccess(recipeList.getRecipes().size());
                }
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to add recipe to list");
                callback.onFailure(null);
            }
        });

    }

    /**
     * Fetches a recipe list by its id
     * @param lid - id of the recipe list.
     * @param callback - callback returning the recipeList on success, or null on failure.
     */
    public void getListById(long lid, CustomCallback<RecipeList> callback){
        String url = String.format("list/id/%s?uid=%s", lid, mUid);

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null) {
                    Gson gson = new GsonBuilder().create();
                    callback.onSuccess(gson.fromJson(jsonElement, RecipeList.class));
                }
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking exception", "Failed to fetch recipe list");
                callback.onFailure(null);
            }
        });

    }

    // TODO: ath hvort það eigi að nota þetta einhvers staðar annars staðar
    /**
     * Fetches all recipes from the recipe list with the Id 'lid'.
     * @param lid - id of the recipe list.
     * @return All recipes from the corresponding recipe list.
     */
    public void getRecipesFromList(long lid, CustomCallback<List<Recipe>> callback){
        String url = String.format("list/id/%s/recipe?uid=%s", lid, mUid);

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null) {
                    Gson gson = new GsonBuilder().create();
                    Type collectionType = new TypeToken<Collection<Recipe>>() {}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                }
                else {
                    callback.onFailure(new ArrayList<>());
                    //throw new NullPointerException("List recipes are null");
                }
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to fetch list recipes");
                callback.onFailure(new ArrayList<>());

            }
        });


    }

    /**
     * Sends a patch request to the API with a new title for the recipe list.
     * @param id - Id of the recipe list being renamed
     * @param newTitle - New title of the recipe list
     */
    public void updateRecipeListTitle(long id, String newTitle){
        String url = String.format("list/updateTitle/%s?uid=%s", id, mUid);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title",newTitle);
        String data = gson.toJson(requestBody);

        try {
            mJsonElement = mNetworkingService.patchRequest(url, data);
            Log.d("API", "Updated recipe list title: " + mJsonElement);
        } catch(IOException e) {
            Log.d("Networking exception", "Failed to update list title");
        }

        if(mJsonElement == null){
            throw new NullPointerException("Renamed recipe list is null");
        }
    }


    /**
     * Deletes the given recipe list from database
     * @param lid - id of the recipe list
     */
    public void deleteRecipeList(long lid){
        String url = String.format("list/id/%s/delete?uid=%s", lid, mUid);
        try {
            mNetworkingService.deleteRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "Delete recipe list failed");
        }

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
