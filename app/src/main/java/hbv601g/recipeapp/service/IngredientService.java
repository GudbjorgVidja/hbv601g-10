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

import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;

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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


