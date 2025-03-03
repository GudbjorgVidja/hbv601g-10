package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;

public class RecipeService extends Service {
    private NetworkingService networkingService;
    private JsonElement element;
    private long mUid;

    public RecipeService(NetworkingService networkingService, long uid){
        this.networkingService = networkingService;
        this.mUid = uid;
    }

    /**
     * This fuction takes in 6 parameters, String, User, 
     * @param title
     * @param author
     * @param ingredients
     * @param unit
     * @param quantity
     * @return
     */
    public Recipe createRecipe(
            String title, User author, Ingredient[] ingredients, Unit[] unit, double[] quantity
                ){
        //String url = "Recipe/create";
        //url += String.format("?title=%s&author=%s", title, author);

        Gson gson = new Gson();
        Recipe rep = new Recipe(title, author);
        List<IngredientMeasurement> ingredList = new ArrayList<>();

        for(int i = 0; i < ingredients.length; i++){
            IngredientMeasurement t = new IngredientMeasurement(ingredients[i], unit[i], quantity[i]);
            ingredList.add(t);
        }

        rep.setIngredientMeasurements(ingredList);

        String url = "recipe/new";
        String data = gson.toJson(rep);

        try {
            element = networkingService.postRequest(url, data);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to create recipe");
            //throw new RuntimeException(e);
        }

        if(element != null){
            rep = gson.fromJson(element, Recipe.class);
            Log.d("API", "recipe object, title:" + rep.getTitle());
        }

        return rep;


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
