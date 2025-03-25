package hbv601g.recipeapp.networking;

public interface CustomCallback<T> {
    void onSuccess(T t);
    void onFailure(T t);
}
