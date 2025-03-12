package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;

import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.NetworkingService;

public class RecipeListService extends Service {
    private NetworkingService mNetworkingService;

    private JsonElement mJsonElement;
    private long mUid;

    public RecipeListService(NetworkingService networkingService, long uid){
        mNetworkingService = networkingService;
        mUid = uid;
    }

    public RecipeList createRecipeList(String title, String description, boolean isPrivate){
        String url = String.format("list/new?uid=%d&title=%s&description=%s&is");
        return null;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
