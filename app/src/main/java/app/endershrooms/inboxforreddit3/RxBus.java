package app.endershrooms.inboxforreddit3;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.support.annotation.IntDef;
import android.util.SparseArray;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;

;

/**
 * Created by Travis on 3/23/2018.
 */

public class RxBus {

  private static final RxBus ourInstance = new RxBus();
  public static RxBus get() {
    return ourInstance;
  }
  private RxBus() {
  }

  private static SparseArray<PublishSubject<Object>> sSubjectMap = new SparseArray<>();
  private static Map<Object, CompositeDisposable> sSubscriptionsMap = new HashMap<>();

  public static class Subjects {
    public static final int ON_ACCOUNT_UPDATE = 0;
    public static final int ON_ACCOUNT_ADDED  = 1;
  }


  @Retention(SOURCE)
  @IntDef({Subjects.ON_ACCOUNT_UPDATE, Subjects.ON_ACCOUNT_ADDED})
  @interface Subject {}

  /**
   * Get the subject or create it if it's not already in memory.
   */
  @NonNull
  private static PublishSubject<Object> getSubject(@Subject int subjectCode) {
    PublishSubject<Object> subject = sSubjectMap.get(subjectCode);
    if (subject == null) {
      subject = PublishSubject.create();
      subject.subscribeOn(AndroidSchedulers.mainThread());
      sSubjectMap.put(subjectCode, subject);
    }

    return subject;
  }

  /**
   * Get the CompositeDisposable or create it if it's not already in memory.
   */
  @NonNull
  private static CompositeDisposable getCompositeDisposable(@NonNull Object object) {
    CompositeDisposable compositeDisposable = sSubscriptionsMap.get(object);
    if (compositeDisposable == null) {
      compositeDisposable = new CompositeDisposable();
      sSubscriptionsMap.put(object, compositeDisposable);
    }

    return compositeDisposable;
  }

  /**
   * Subscribe to the specified subject and listen for updates on that subject. Pass in an object to associate
   * your registration with, so that you can unsubscribe later.
   * <br/><br/>
   * <b>Note:</b> Make sure to call {@link RxBus#unregister(Object)} to avoid memory leaks.
   */
  public static void subscribe(@Subject int subject, @NonNull Object lifecycle, @NonNull Consumer<? super Object> action) {
    Disposable disposable = getSubject(subject).subscribe(action);
    getCompositeDisposable(lifecycle).add(disposable);
  }

  /**
   * Unregisters this object from the bus, removing all subscriptions.
   * This should be called when the object is going to go out of memory.
   */
  public static void unregister(@NonNull Object lifecycle) {
    //We have to remove the composition from the map, because once you dispose it can't be used anymore
    CompositeDisposable compositeDisposable = sSubscriptionsMap.remove(lifecycle);
    if (compositeDisposable != null) {
      compositeDisposable.dispose();
    }
  }

  /**
   * Publish an object to the specified subject for all subscribers of that subject.
   */
  public static void publish(@Subject int subject, @NonNull Object message) {
    getSubject(subject).onNext(message);
  }

}
