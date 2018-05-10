package app.endershrooms.inboxforreddit3.models.reddit;

/**
 * Created by Travis on 5/8/2018.
 */

public class ResponseWithError<T, E> {

  private T data;
  private E error;

  public ResponseWithError(T data, E error) {
    this.data = data;
    this.error = error;
  }

  public ResponseWithError() {
  }

  public void setData(T data) {
    this.data = data;
  }

  public void setError(E error) {
    this.error = error;
  }

  public T getData() {
    return data;
  }

  public E getError() {
    return error;
  }
}
