package hbv601g.recipeapp.networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import hbv601g.recipeapp.exceptions.DeleteFailedException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.*;

/**
 * A service class which makes requests to the external API.
 * cite your sources:
 * <a href="https://www.baeldung.com/guide-to-okhttp">...</a>
 */
public class NetworkingService extends Service {
    private final String mBaseURL;
    private JsonElement mJsonElement;
    private int mResponseCode;

    public NetworkingService() {
        mBaseURL = "https://hbv501g-26.onrender.com/";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Makes a get request to the API
     *
     * @param reqURL the path to use, which is appended to the base url
     * @param callback a callback returning the response body from the API on success
     */
    public void getRequest(String reqURL, CustomCallback<JsonElement> callback) {
        Request request = new Request.Builder().url(mBaseURL +reqURL).build();
        callAPI(request, callback);
    }


    /**
     * Makes a Post Request to the external API
     *
     * @param reqURL a string containing the URL for the API call
     * @param data a string containing the data to add to the request body of the call
     * @param callback a callback returning the response body returned
     */
    public void postRequest(String reqURL, String data, CustomCallback<JsonElement> callback) {
        RequestBody requestBody = RequestBody.create(data == null ? "" : data,
                MediaType.parse("application/json"));
        Request request = new Request.Builder().url(mBaseURL +reqURL).post(requestBody).build();
        callAPI(request, callback);
    }


    /**
     * Makes a patch request using the given url and data, calls the API and interprets the result
     * into a JsonElement to return
     *
     * @param reqURL the url of the request
     * @param data data to include in the request body
     * @param callback a callback returning the response body returned
     */
    public void patchRequest(String reqURL, String data, CustomCallback<JsonElement> callback) {
        RequestBody requestBody = RequestBody.create(data == null ? "" : data,
                MediaType.parse("application/json"));
        Request request = new Request.Builder().url(mBaseURL +reqURL).patch(requestBody).build();
        callAPI(request, callback);
    }


    /**
     * Makes a delete request using the given url and calls the API Throws a DeleteFailedException
     * if the responseCode is not 200
     *
     * @param reqURL the url to the endpoint that is being called
     * @param callback a callback returning the response body returned
     */
    public void deleteRequest(String reqURL, CustomCallback<JsonElement> callback){
        Request request = new Request.Builder().url(mBaseURL +reqURL).delete().build();
        callAPI(request,callback);
    }


    /**
     * Makes a put request to the API and returns the result of the call
     *
     * @param reqURL endpoint url
     * @param data data for the request body
     * @param callback a callback returning the response body returned
     */
    public void putRequest(String reqURL, String data, CustomCallback<JsonElement> callback)  {
        RequestBody requestBody = RequestBody.create(data == null ? "" : data,
                MediaType.parse("application/json"));
        Request request = new Request.Builder().url(mBaseURL + reqURL).put(requestBody).build();
        callAPI(request, callback);
    }


    /**
     * Method that calls the API, using the given request
     *
     * @param request the request to be used
     * @param callback a callback which returns the response body on success or null on
     *         failure (when response code is not 200)
     */
    private void callAPI(Request request, CustomCallback<JsonElement> callback){
        OkHttpClient client = new OkHttpClient();

        Call call = client.newCall(request);


        call.enqueue(new Callback() {
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {

                assert response.body() != null;
                String ret = response.body().string();
                mResponseCode = response.code();

                mJsonElement = JsonParser.parseString(ret);

                if(mResponseCode != 200)
                    callback.onFailure(null);
                else if ( mJsonElement == null || mJsonElement.isJsonNull())
                    callback.onSuccess(null);
                else
                    callback.onSuccess(mJsonElement);

                response.close();
            }

            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("API", "onFailure");
                call.cancel();
                callback.onFailure(null);
            }
        });

    }

}
