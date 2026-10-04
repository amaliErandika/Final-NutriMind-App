package com.example.nutrimind;

// Android classes used for opening activities, displaying messages, and logging
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

// Classes used to create the activity and display the history list
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Custom classes from the NutriMind project
import com.example.nutrimind.adapter.HistoryAdapter;
import com.example.nutrimind.model.Message;

// Firebase classes used to get the logged-in user and chat history
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

// Java classes used for dates and storing unique session IDs
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


/*
 * This activity displays the user's previous conversations.
 *
 * The activity:
 * - Gets chat history from Firebase
 * - Shows one item for each conversation
 * - Displays the date and a short message preview
 * - Opens the selected conversation when clicked
 */
public class HistoryActivity extends AppCompatActivity {

    // Tag used to identify error messages in Logcat
    private static final String TAG =
            "HistoryActivity";

    // RecyclerView used to display previous conversations
    private RecyclerView recyclerHistory;

    // List that stores the conversation history
    private ArrayList<Message> historySessions;

    // Adapter connects the history data to the RecyclerView
    private HistoryAdapter adapter;

    // Reference to the Firebase Firestore database
    private FirebaseFirestore db;


    /*
     * This method runs when the History screen is opened.
     *
     * It:
     * - Loads the history screen layout
     * - Connects the RecyclerView
     * - Creates the history list
     * - Sets up the HistoryAdapter
     * - Connects to Firebase
     * - Loads the user's history
     */
    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        // Load the History screen layout
        setContentView(
                R.layout.activity_history
        );

        // Connect the RecyclerView with its XML view
        recyclerHistory =
                findViewById(
                        R.id.recyclerHistory
                );

        // Create an empty list for conversation history
        historySessions =
                new ArrayList<>();

        /*
         * Create the adapter.
         *
         * When the user clicks a history item,
         * the selected conversation will be opened.
         */
        adapter =
                new HistoryAdapter(
                        historySessions,
                        message -> {

                            /*
                             * The sender field is used to store
                             * the conversation's session ID.
                             */
                            String sessionId =
                                    message.getSender();

                            // Check whether the session ID is valid
                            if (sessionId == null
                                    || sessionId.isEmpty()) {

                                // Show an error if the ID is missing
                                Toast.makeText(
                                        HistoryActivity.this,
                                        "Invalid conversation.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            /*
                             * Open the StressRelief activity
                             * to display the selected conversation.
                             */
                            Intent intent =
                                    new Intent(
                                            HistoryActivity.this,
                                            StressRelief.class
                                    );

                            // Tell StressRelief that this is old history
                            intent.putExtra(
                                    "is_history_mode",
                                    true
                            );

                            // Send the selected session ID
                            intent.putExtra(
                                    "session_id",
                                    sessionId
                            );

                            // Open the conversation
                            startActivity(intent);
                        }
                );

        // Set a vertical layout for the history list
        recyclerHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // Connect the adapter to the RecyclerView
        recyclerHistory.setAdapter(
                adapter
        );

        // Get the Firebase Firestore database
        db =
                FirebaseFirestore.getInstance();

        // Load the user's previous conversations
        loadHistory();
    }


    /*
     * ============================================================
     * LOAD HISTORY
     * ============================================================
     *
     * This method gets the user's previous chat messages
     * from Firebase Firestore.
     *
     * The messages are sorted from newest to oldest.
     * Only one item is shown for each conversation.
     */
    private void loadHistory() {

        // Get the currently logged-in Firebase user
        FirebaseUser user =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        /*
         * Check whether a user is logged in.
         *
         * Without a logged-in user, there is no history
         * that can be loaded.
         */
        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Open the current user's chat history.
         *
         * The messages are ordered by timestamp,
         * with the newest messages shown first.
         */
        db.collection("users")
                .document(user.getUid())
                .collection("chatHistory")
                .orderBy(
                        "timestamp",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            // Clear the old list before loading fresh data
                            historySessions.clear();

                            /*
                             * This Set stores session IDs that have
                             * already been added to the history list.
                             *
                             * It prevents the same conversation
                             * from appearing multiple times.
                             */
                            Set<String> processedSessions =
                                    new HashSet<>();

                            /*
                             * Go through every document returned
                             * from Firebase.
                             */
                            for (
                                    DocumentSnapshot document :
                                    querySnapshot.getDocuments()
                            ) {

                                // Get the conversation ID
                                String sessionId =
                                        document.getString(
                                                "sessionId"
                                        );

                                // Get the message text
                                String text =
                                        document.getString(
                                                "text"
                                        );

                                // Get the message timestamp
                                Long timestamp =
                                        document.getLong(
                                                "timestamp"
                                        );

                                /*
                                 * Ignore records that do not contain
                                 * the required information.
                                 *
                                 * This also protects the app from
                                 * older records without sessionId.
                                 */
                                if (sessionId == null
                                        || text == null
                                        || timestamp == null) {

                                    continue;
                                }

                                /*
                                 * Only show one history item
                                 * for each conversation.
                                 *
                                 * If this session was already added,
                                 * skip the current message.
                                 */
                                if (processedSessions.contains(
                                        sessionId
                                )) {

                                    continue;
                                }

                                // Mark this conversation as processed
                                processedSessions.add(
                                        sessionId
                                );

                                /*
                                 * Create a date format that is easy
                                 * for the user to read.
                                 *
                                 * Example:
                                 * Oct 04, 2026 - 02:30 PM
                                 */
                                SimpleDateFormat dateFormat =
                                        new SimpleDateFormat(
                                                "MMM dd, yyyy - hh:mm a",
                                                Locale.getDefault()
                                        );

                                // Convert the timestamp into a readable date
                                String dateLabel =
                                        dateFormat.format(
                                                new Date(timestamp)
                                        );

                                // Create a short preview of the message
                                String preview =
                                        createPreview(text);

                                /*
                                 * Combine the date and message preview.
                                 *
                                 * The date is shown on the first line
                                 * and the message preview on the second line.
                                 */
                                String displayText =
                                        dateLabel
                                                + "\n"
                                                + preview;

                                /*
                                 * Create a Message object for the history list.
                                 *
                                 * sender = session ID
                                 * text = date and message preview
                                 * timestamp = original message time
                                 */
                                historySessions.add(
                                        new Message(
                                                sessionId,
                                                displayText,
                                                timestamp
                                        )
                                );
                            }

                            // Refresh the RecyclerView with the new history data
                            adapter.notifyDataSetChanged();

                        }
                )
                .addOnFailureListener(e -> {

                    /*
                     * Write the Firebase error to Logcat.
                     * This helps the developer find the problem.
                     */
                    Log.e(
                            TAG,
                            "Firestore query failed",
                            e
                    );

                    // Show an error message to the user
                    Toast.makeText(
                            HistoryActivity.this,
                            "Error loading history: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    /*
     * ============================================================
     * CREATE PREVIEW
     * ============================================================
     *
     * This method creates a short preview of a chat message.
     *
     * Long messages are shortened to 80 characters so that
     * the history screen stays clean and easy to read.
     */
    private String createPreview(
            String text
    ) {

        // Return an empty string if the message is null
        if (text == null) {
            return "";
        }

        /*
         * Remove line breaks and extra spaces.
         *
         * This makes the preview appear on one line.
         */
        String cleanedText =
                text.replace(
                        "\n",
                        " "
                ).trim();

        /*
         * If the message is longer than 80 characters,
         * show only the first 80 characters and add "...".
         */
        if (cleanedText.length() > 80) {

            return cleanedText.substring(
                    0,
                    80
            ) + "...";
        }

        // Return the complete text if it is short enough
        return cleanedText;
    }


    /*
     * ============================================================
     * RELOAD HISTORY
     * ============================================================
     *
     * This method runs whenever the HistoryActivity
     * becomes visible again.
     *
     * It reloads the history so that any new conversations
     * are displayed automatically.
     */
    @Override
    protected void onResume() {

        super.onResume();

        /*
         * Check that the Firebase database has already
         * been initialized before loading the history.
         */
        if (db != null) {

            // Reload the latest conversation history
            loadHistory();
        }
    }
}

