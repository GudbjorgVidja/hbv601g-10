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
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.networking.NetworkingService;

public class IngredientService extends Service {

    private NetworkingService mNetworkingService;
    private long mUid;
    private JsonElement mElement;

    public IngredientService(NetworkingService networkingService, long uid){
        this.mNetworkingService = networkingService;
        this.mUid = uid;
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

    public Ingredient createIngredient(String title, double quantity, Unit unit, double price, String store, String brand, boolean isPrivate){
        String url = "ingredient/created?uid=" + mUid;

        Ingredient ingredient = new Ingredient(title, unit, quantity, price, store, brand );
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        String data = gson.toJson(ingredient, Ingredient.class);
        try {
        mElement = mNetworkingService.postRequest(url, data);
        } catch (IOException e) {
            Log.d("Networking exception", "create ingredient failed");
            mElement = null;
            //throw new RuntimeException(e);
        }

        Log.d("Ingredient", "createIngredient");

        if(mElement != null){
            //Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            if(!mElement.isJsonObject()) return null;

            ingredient = gson.fromJson(mElement, Ingredient.class);

        }
        else return null;

        return ingredient;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


