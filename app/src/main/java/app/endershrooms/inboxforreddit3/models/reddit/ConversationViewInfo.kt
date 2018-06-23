package app.endershrooms.inboxforreddit3.models.reddit

class ConversationViewInfo(message: Message) {
    val parentName : String = message.parentMessageName
    val correspondent : String  = message.getCorrespondent()
    val subject : String = message.subject
}