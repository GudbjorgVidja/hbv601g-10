package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.lang.reflect.Type;

import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.NetworkingService;

public class RecipeService extends Service {
    private NetworkingService mNetworkingService;

    private JsonElement mJsonElement;
    private long mUid;


    public RecipeService(NetworkingService networkingService, long uid){
        mNetworkingService=networkingService;
        mUid=uid;
    }

    public double getPersonalizedPurchaseCost(long rid){
        String url = String.format("recipe/id/%s/personal?uid=%s",rid,mUid);

        try{
            mJsonElement=mNetworkingService.getRequest(url);
        } catch (IOException e){
            Log.d("Networking exception", "Failed to get PPC");
        }

        double ppc = 0;

        if(mJsonElement!=null){
            ppc = mJsonElement.getAsDouble();
            Log.d("API", "PPC: " + ppc);
        }
        return ppc;
    }
    /**
     * Makes a get request for the external API, for the endpoint that gets all recipes
     * and turns it from a JsonElement to a List of Recipes
     * @return all recipes
     */
    public List<Recipe> getAllRecipes(){
        String url = "recipe/all?uid=" + mUid;

        try{
            mJsonElement = mNetworkingService.getRequest(url);
        } catch (IOException e){
            Log.d("Networking exception", "Failed to get all recipes");
        }

        List<Recipe> recipes = new ArrayList<>();
        if(mJsonElement != null){
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            if(!mJsonElement.isJsonArray()) return null;

            JsonArray array = mJsonElement.getAsJsonArray();

            Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
            recipes = gson.fromJson(array, collectionType);
        }
        else throw new NullPointerException("Recipe list is null");

        return recipes;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
