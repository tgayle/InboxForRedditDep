package app.endershrooms.inboxforreddit3.adapters;

import android.support.v7.view.ActionMode;
import java.util.List;

/**
 * An interface to represent an adapter with items that can be selected for instances such as
 * class{@link ActionMode}.
 * @param <T> The type of item that is selectable.
 */
public interface SelectableItems<Key, T> {

  int getNumberOfItemsSelected();

  List<T> getSelectedItems();

  void removeSelectedItemByKey(Key key);

  void removeSelectedItem(T item);

  void clearSelectedItems();

  void markItemSelected(Key key, T item);

  void enableItemSelection();

  void disableItemSelection();
}
