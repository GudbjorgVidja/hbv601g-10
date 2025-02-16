package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;

import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;

public class UserService extends Service {
    private NetworkingService networkingService;
    private JsonElement element;

    public UserService(NetworkingService networkingService){
        this.networkingService=networkingService;
    }

    // TODO: skila boolean (virkar?) eða user (ekki null?)
    public void logIn(String username, String password){
        String url = "user/login";

        //?username=${username}&password=${password}
        String params = String.format("?username=%s&password=%s",username, password);
        User user;
        try {
            element = networkingService.getRequest(url + params);
        } catch (IOException e) {
            Log.d("Networking exception", "Login failed");
            //throw new RuntimeException(e);
        }


        if(element != null){
            Gson gson = new Gson();
            user = gson.fromJson(element, User.class);
            Log.d("API", "user object, name:" + user.getUsername());
        }

        // return user;

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
