package app.endershrooms.inboxforreddit3.models.reddit

class ConversationViewInfo(message: Message) {
    val parentName = message.parentMessageName
    val correspondent = message.getCorrespondent()
    val subject = message.subject
}