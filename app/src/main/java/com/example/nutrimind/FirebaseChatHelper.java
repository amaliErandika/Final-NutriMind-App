package com.example.nutrimind;

// Firebase Firestore is used to store chat messages
import com.google.firebase.firestore.FirebaseFirestore;

// Java classes used to store message data
import java.util.HashMap;
import java.util.Map;


/*
 * This helper class is used to save chat messages
 * into Firebase Firestore.
 * It keeps the Firebase saving code separate from
 * the main chat activity.
 */
public class FirebaseChatHelper {

    // Reference to the Firebase Firestore database
    private final FirebaseFirestore db;


    /*
     * Constructor of the helper class.
     * It connects the application to
     * the Firebase Firestore database.
     */
    public FirebaseChatHelper() {

        // Get the Firestore database instance
        db = FirebaseFirestore.getInstance();
    }


    /*
     * This method saves one chat message to Firebase.
     *
     * Parameters:
     * userId    - ID of the logged-in user
     * sender    - Who sent the message (user or AI)
     * text      - The actual message
     * timestamp - Time when the message was created
     * sessionId - ID of the current conversation
     */
    public void saveMessage(
            String userId,
            String sender,
            String text,
            long timestamp,
            String sessionId
    ) {

        /*
         * Create a Map to store the message data.
         *
         * A Map stores information using
         * a key and a value.
         */
        Map<String, Object> message =
                new HashMap<>();

        // Store who sent the message
        message.put("sender", sender);

        // Store the message text
        message.put("text", text);

        // Store the time when the message was created
        message.put("timestamp", timestamp);

        // Store the conversation ID
        message.put("sessionId", sessionId);


        /*
         * Save the message in Firestore.
         * The data is stored using this structure:
         * users
         * userId
         *chatHistory
         * message
         * Each message is added as a new document.
         */
        db.collection("users")
                .document(userId)
                .collection("chatHistory")
                .add(message);
    }
}

