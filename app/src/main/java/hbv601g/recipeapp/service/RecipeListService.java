package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
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

import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;

/**
 * A service class for interacting with the networking service in order to make requests related to
 * the RecipeList entity to the external API
 */
public class RecipeListService extends Service {
    private NetworkingService mNetworkingService;
    private long mUid;

    public RecipeListService(NetworkingService networkingService, long uid) {
        mNetworkingService = networkingService;
        mUid = uid;
    }

    /**
     * Creates a recipe list and adds it to the database
     *
     * @param title title of the list, max 50 characters
     * @param description description of the list, max 250 chars, may be null
     * @param isPrivate true if the list is private, otherwise false
     * @param callback a callback that returns the new recipe list on success or null on
     *         failure
     */
    public void createRecipeList(String title, String description, boolean isPrivate,
                                 CustomCallback<RecipeList> callback) {

        String url = Uri.parse("list/new").buildUpon()
                .appendQueryParameter("uid", "" + mUid)
                .appendQueryParameter("title", title)
                .appendQueryParameter("description", description)
                .appendQueryParameter("isPrivate", "" + isPrivate)
                .build().toString();

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
     * @param uid user id
     * @param callback returns all of the user's recipe lists on success, or an empty list
     *         on failure
     */
    public void getUserRecipeLists(long uid, CustomCallback<List<RecipeList>> callback) {
        String url = "list/user/" + uid + "?uid=" + mUid;
        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Log.d("Callback", "onSuccess í service");
                    Gson gson = new GsonBuilder().create();
                    Type collectionType = new TypeToken<Collection<RecipeList>>() {}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                }
                else callback.onFailure(new ArrayList<>());
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Callback", "onFailure í service");
                callback.onFailure(new ArrayList<>());
            }
        });

    }


    /**
     * Adds the given recipe to the given recipe list.
     *
     * @param recipeId id of the recipe to add
     * @param listId id of the list to be added to
     * @param callback callback returning the new list size on success, or null on failure
     */
    public void addRecipeToList(long recipeId, long listId, CustomCallback<Integer> callback) {
        String url = String.format("list/addRecipe?recipeID=%s&listID=%s&uid=%s",
                recipeId, listId, mUid);

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
     * Fetches a recipe list by its id.
     *
     * @param lid id of the recipe list.
     * @param callback callback returning the recipeList on success, or null on failure.
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

    /**
     * Fetches all recipes from the recipe list with the id 'lid'.
     *
     * @param lid id of the recipe list.
     * @param callback a callback returning the recipes from the list that are accessible to
     *         the user making the request on success, or an empty list on failure
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
     * Removes the given recipe from the given recipe list, if the recipe is in it
     *
     * @param list The RecipeList the recipe should be removed from.
     * @param recipe The Recipe that should be removed from the list.
     * @param callback A callback to the fragment, returning the updated list on success
     *                 or the original list on failure
     */
    public void removeRecipeFromList(RecipeList list, Recipe recipe,
                                     CustomCallback<RecipeList> callback) {

        String url = String.format("list/id/%s/recipe/%s/remove?uid=%s",
                list.getId(), recipe.getId(), mUid);

        int originalSize = list.getRecipes().size();
        mNetworkingService.patchRequest(url, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new GsonBuilder().create();
                    RecipeList returnedList = gson.fromJson(jsonElement, RecipeList.class);
                    Log.d("Recipe list", "removeRecipeFromList: " + jsonElement);

                    if(returnedList.getRecipes().size() != originalSize)
                        callback.onSuccess(returnedList);
                    else onFailure(null);
                }
                else onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking exception", "Failed to remove recipe from list");
                callback.onFailure(list);
            }
        });

    }

    /**
     * Sends a patch request to the API with a new title for the recipe list.
     *
     * @param id the id of the recipe list being renamed
     * @param newTitle New title of the recipe list
     * @param callback onSuccess if the title was changed, otherwise onFailure
     */
    public void updateRecipeListTitle(long id, String newTitle, CustomCallback<Boolean> callback) {
        String url = String.format("list/updateTitle/%s?uid=%s", id, mUid);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", newTitle);
        String data = gson.toJson(requestBody);

        mNetworkingService.patchRequest(url, data, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                Log.d("API", "Updated recipe list: " + jsonElement);

                if(jsonElement != null) callback.onSuccess(null);
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to update list title");
                callback.onFailure(null);
            }
        });

    }

    /**
     * Deletes the given recipe list
     *
     * @param lid id of the recipe list
     * @param callback a callback that always returns null, but calls onSuccess if the recipe
     * list was successfully deleted or onFailure if it was not
     */
    public void deleteRecipeList(long lid, CustomCallback<Boolean> callback) {
        String url = String.format("list/id/%s/delete?uid=%s", lid, mUid);

        mNetworkingService.deleteRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Delete recipe list failed");
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
