package hbv601g.recipeapp.exceptions;

/**
 * Custom RuntimeException for failed delete requests to the API
 */
public class DeleteFailedException extends RuntimeException {
    public DeleteFailedException(String message) {
        super(message);
    }
    public DeleteFailedException() {
        super();
    }
}
