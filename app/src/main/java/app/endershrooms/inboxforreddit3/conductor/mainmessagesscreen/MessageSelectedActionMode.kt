package app.endershrooms.inboxforreddit3.conductor.mainmessagesscreen

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import app.endershrooms.inboxforreddit3.R
import app.endershrooms.inboxforreddit3.adapters.SelectableItems

class MessageSelectedActionMode<Key, Item>(val context: Context, private val itemAdapter: SelectableItems<Key, Item>,
                                           private val onDeleteMessages: (MutableList<Item>) -> Unit,
                                           private val onStart: () -> Unit,
                                           private val onFinish: () -> Unit) : ActionMode.Callback {
    var mode: ActionMode? = null

    override fun onCreateActionMode(localActionModel: ActionMode, menu: Menu): Boolean {
        localActionModel.menuInflater.inflate(R.menu.menu_context_conversation_action, menu)
        itemAdapter.enableItemSelection()
        onStart()
        mode = localActionModel
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.delete_conversation -> {
                val selectedMessages = itemAdapter.selectedItems
                if (selectedMessages.size > 0) {
                    AlertDialog.Builder(context)
                            .setTitle("Delete these $selectedMessages.size messages?")
                            .setMessage("After deleting, these messages can still be found in the deleted tab to the left.")
                            .setPositiveButton("Delete") { _, _ ->
                                onDeleteMessages(itemAdapter.selectedItems)
                                Log.d("ActionMode", "Finish should have happened.")
                            }
                            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                            .setOnDismissListener { mode!!.finish() }
                            .show()
                }
                return true
            }
            else -> return false
        }
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        mode = null
        onFinish()
        itemAdapter.disableItemSelection()
        itemAdapter.clearSelectedItems()
    }
}