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

    public RecipeListService(NetworkingService networkingService, long uid){
        mNetworkingService = networkingService;
        mUid = uid;
    }

    /**
     * // TODO: Breyta útfærslu í bakenda, líklega ekki gott að nota requestParams fyrir allt.
     *         // nota frekar requestBody?
     * Creates a recipe list and adds it to the database
     * @param title - title of the list, max 50 characters
     * @param description - description of the list, max 250 chars, may be null
     * @param isPrivate - true if the list is private, otherwise false
     * @return RecipeList object added to the db, or null
     */
    public RecipeList createRecipeList(String title, String description, boolean isPrivate){
        String url = String.format("list/new?uid=%s&title=%s&description=%s&isPrivate=%b", mUid,title,description,isPrivate);

        RecipeList recipeList;
        mJsonElement = null;
        try{
            mJsonElement = mNetworkingService.postRequest(url, "");
        }catch (IOException e){
            Log.d("Networking exception", "Failed to create recipe list");
        }

        if(mJsonElement != null && mJsonElement.isJsonObject()){
            Gson gson = new GsonBuilder().create();
            recipeList = gson.fromJson(mJsonElement, RecipeList.class);
        }
        else throw new NullPointerException("Failed to create recipe list");

        return recipeList;
    }

    public List<RecipeList> getUserRecipeLists(long uid){
        String url = String.format("list/user/%s?uid=%s", uid, mUid);
        List<RecipeList> recipeLists = new ArrayList<>();
        mJsonElement = null;
        try{
            mJsonElement = mNetworkingService.getRequest(url);
            Log.d("API", "Recipe lists: " + mJsonElement);
            Log.d("API", "Request url: " + url);
        } catch (IOException e){
            Log.d("Networking exception", "Failed to fetch recipe lists");
        }
        if(mJsonElement != null && !mJsonElement.isJsonNull()){
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            if(!mJsonElement.isJsonArray()){
                return null;
            }

            JsonArray arr = mJsonElement.getAsJsonArray();

            Type collectionType = new TypeToken<Collection<RecipeList>>(){}.getType();
            recipeLists = gson.fromJson(arr, collectionType);
        } else {
            throw new NullPointerException(("Recipe lists are null"));
        }

        return recipeLists;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
