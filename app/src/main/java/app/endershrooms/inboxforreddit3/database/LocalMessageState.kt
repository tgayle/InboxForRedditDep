package app.endershrooms.inboxforreddit3.database

class LocalMessageState(var inInbox: Boolean, var inDeleted: Boolean, var isHidden: Boolean, var isPending: Boolean) {

    fun removeAllStatesExceptOne(stateToBeLeft: LocalMessageStates) {
        when (stateToBeLeft) {
            LocalMessageStates.INBOX -> {
                inDeleted = false
                isHidden = false
                isPending = false
                inInbox = true
            }
            LocalMessageStates.DELETED -> {
                inInbox = false
                isHidden = false
                isPending = false
                inDeleted = true
            }
            LocalMessageStates.HIDDEN -> {
                inInbox = false
                inDeleted = false
                isPending = false
                isHidden = true
            }
            LocalMessageStates.PENDING -> {
                inInbox = false
                inDeleted = false
                isHidden = false
                isPending = true
            }
        }
    }
}
