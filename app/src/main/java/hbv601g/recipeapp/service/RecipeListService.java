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
import hbv601g.recipeapp.networking.NetworkingService;

/**
 * Service class for recipe list API calls
 */
public class RecipeListService extends Service {
    private NetworkingService mNetworkingService;
    private JsonElement mJsonElement;
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
    public RecipeList createRecipeList(String title, String description, boolean isPrivate) {
        String url = String.format("list/new?uid=%s&title=%s&description=%s&isPrivate=%b", mUid, title, description, isPrivate);

        RecipeList recipeList;
        mJsonElement = null;
        try {
            mJsonElement = mNetworkingService.postRequest(url, "");
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to create recipe list");
        }

        if (mJsonElement != null && mJsonElement.isJsonObject()) {
            Gson gson = new GsonBuilder().create();
            recipeList = gson.fromJson(mJsonElement, RecipeList.class);
        }
        else throw new NullPointerException("Failed to create recipe list");

        return recipeList;
    }


    /**
     * Makes a request to get all recipe lists for the current user, public and private
     *
     * @return all of the user's recipe lists
     */
    public List<RecipeList> getUserRecipeLists(long uid) {
        String url = "list/user/" + uid + "?uid=" + mUid;

        mJsonElement = null;
        try {
            mJsonElement = mNetworkingService.getRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to get recipe lists for user");
        }

        List<RecipeList> recipeLists = new ArrayList<>();
        if (mJsonElement != null ) {
            Gson gson = new GsonBuilder().create();

            Type collectionType = new TypeToken<Collection<RecipeList>>() {}.getType();
            recipeLists = gson.fromJson(mJsonElement, collectionType);
        }
        else throw new NullPointerException("User recipe lists are null");

        return recipeLists;
    }


    /**
     * Adds the given recipe to the given recipe list.
     * @param recipeId - id of the recipe to add
     * @param listId - id of the list to be added to
     */
    public void addRecipeToList(long recipeId, long listId) {
        String url = String.format("list/addRecipe?recipeID=%s&listID=%s&uid=%s", recipeId, listId, mUid);
        mJsonElement = null;
        try {
            mJsonElement = mNetworkingService.putRequest(url, "");
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to add recipe to list");
        }

        RecipeList recipeList;
        if (mJsonElement != null ) {
            Gson gson = new GsonBuilder().create();

            recipeList = gson.fromJson(mJsonElement, RecipeList.class);
        }
        else throw new NullPointerException("Recipe list is null");

    }

    /**
     * Fetches a recipe list by it's Id.
     * @param lid - id of the recipe list.
     * @return Recipe list with the Id 'lid'.
     */
    public RecipeList getListById(long lid){
        String url = String.format("list/id/%s?uid=%s", lid, mUid);

        try {
            mJsonElement = mNetworkingService.getRequest(url);
        } catch(IOException e) {
            Log.d("Networking exception", "Failed to fetch recipe list");
        }

        RecipeList recipeList = null;
        if(mJsonElement != null) {
            Gson gson = new GsonBuilder().create();

            recipeList = gson.fromJson(mJsonElement, RecipeList.class);
        } else {
            throw new NullPointerException("Recipe list is null");
        }

        return recipeList;
    }

    /**
     * Fetches all recipes from the recipe list with the Id 'lid'.
     * @param lid - id of the recipe list.
     * @return All recipes from the corresponding recipe list.
     */
    public List<Recipe> getRecipesFromList(long lid){
        String url = String.format("list/id/%s/recipe?uid=%s", lid, mUid);

        try {
            mJsonElement = mNetworkingService.getRequest(url);
        } catch(IOException e) {
            Log.d("Networking exception", "Failed to fetch list recipes");
        }

        List<Recipe> listRecipes = new ArrayList<>();

        if(mJsonElement != null) {
            Gson gson = new GsonBuilder().create();

            Type collectionType = new TypeToken<Collection<Recipe>>() {}.getType();
            listRecipes = gson.fromJson(mJsonElement, collectionType);
        } else {
            throw new NullPointerException("List recipes are null");
        }
        return listRecipes;
    }

    /**
     * This function removes the recipe from the recipe list if and only if recipe is in the
     * list
     *
     * @param list   : RecipeList value, is the list that is being removed from.
     * @param recipe : Is the recipe that is being removed from list.
     *
     * @return if recipe was removed from the list then true else false
     */
    public boolean removeRecipeFromList(RecipeList list, Recipe recipe){
        String url = "list/id/" + list.getId() + "/recipe/" + recipe.getId() +
                "/remove?uid=" + mUid;

        try {
            mJsonElement = mNetworkingService.patchRequest(url, null);
        }
        catch (IOException e){
            throw new NullPointerException("List recipes are null");
        }

        boolean res = false;
        if(mJsonElement != null){
            res = mJsonElement.isJsonObject();
            Log.d("API", "recipe removed: " + res);
        }

        return res;
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
