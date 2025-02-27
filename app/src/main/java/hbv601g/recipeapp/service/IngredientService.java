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
import hbv601g.recipeapp.networking.NetworkingService;

public class IngredientService extends Service {

    private NetworkingService networkingService;
    private long uid;
    private JsonElement element;

    public IngredientService(NetworkingService networkingService, long uid){
        this.networkingService = networkingService;
        this.uid = uid;
    }


    public List<Ingredient> getAllIngredients(){
        String url = "ingredient/all";
        url += "?uid="+uid;

        try {
            element = networkingService.getRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "get ingredients failed");
            //throw new RuntimeException(e);
        }

        ArrayList<Ingredient> ingredients = null;
        if(element != null){
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            if(!element.isJsonArray()) return null;

            JsonArray array = element.getAsJsonArray();

            Type collectionType = new TypeToken<Collection<Ingredient>>(){}.getType();
            ingredients = gson.fromJson(array, collectionType);
        }

        return ingredients;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


