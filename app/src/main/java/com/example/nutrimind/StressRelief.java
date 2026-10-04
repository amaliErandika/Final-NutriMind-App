package com.example.nutrimind;

// Android and UI related imports
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

// Classes used to manage chat messages and Firebase data
import com.example.nutrimind.adapter.ChatAdapter;
import com.example.nutrimind.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// Other required Java classes
import java.util.Collections;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

// OkHttp classes are used to communicate with the backend server
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/*
 * This activity is used for the Stress Relief chat screen.
 *
 * The user can:
 * - Send a text message
 * - Record a voice message
 * - Select an audio file
 * - Receive an AI response
 * - View previous conversations
 *
 * Firebase is used to save and load chat messages.
 */
public class StressRelief extends AppCompatActivity {

    // Button/ImageView controls used on the chat screen
    private ImageView btnMic;
    private ImageView btnPlus;
    private ImageView btnSend;

    // Text box where the user types a message
    private EditText etText;

    // Button used to open previous chat history
    private Button btnHistory;

    // RecyclerView displays the chat messages
    private RecyclerView recyclerChat;

    // List that stores all messages in the current chat
    private ArrayList<Message> messages;

    // Adapter connects the message list with the RecyclerView
    private ChatAdapter adapter;

    // Helper class used to save chat messages in Firebase
    private FirebaseChatHelper firebaseChatHelper;

    // Firebase Firestore database reference
    private FirebaseFirestore db;

    // Backend server URL used for text and audio processing
    private final String url = "https://erandika-nutrimind-backend.hf.space/transcribe";

    // Request codes used to identify different activity results
    private static final int PICK_AUDIO = 101;
    private static final int RECORD_AUDIO = 102;

    // Unique ID used to identify the current conversation
    private String sessionId;

    // True when the user is viewing an old conversation
    private boolean isHistoryMode = false;


    /*
     * This method runs when the StressRelief screen is opened.
     *
     * It:
     * - Loads the screen layout
     * - Connects the UI controls
     * - Sets up the chat RecyclerView
     * - Checks whether the user is opening a new or old conversation
     * - Adds click actions to the buttons
     */
    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        // Load the Stress Relief screen layout
        setContentView(
                R.layout.activity_stress_relief
        );

        // Create helper object for saving chat messages
        firebaseChatHelper = new FirebaseChatHelper();

        // Get the Firestore database
        db = FirebaseFirestore.getInstance();

        // Connect Java variables with the UI elements
        btnMic = findViewById(R.id.btnMic);

        btnPlus = findViewById(R.id.btnPlus);

        btnSend = findViewById(R.id.btnSendText);

        etText = findViewById(R.id.etFeeling);

        btnHistory = findViewById(R.id.btnHistory);

        recyclerChat = findViewById(R.id.recyclerChat);

        // Create an empty list for chat messages
        messages = new ArrayList<>();

        // Create the adapter for displaying messages
        adapter = new ChatAdapter(messages);

        // Set the layout manager for the chat list
        recyclerChat.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // Connect the adapter to the RecyclerView
        recyclerChat.setAdapter(adapter);

        // Check whether this screen was opened to view chat history
        isHistoryMode =
                getIntent().getBooleanExtra(
                        "is_history_mode",
                        false
                );

        /*
         * If history mode is enabled,
         * load the selected old conversation.
         */
        if (isHistoryMode) {

            // Get the conversation ID from the previous screen
            sessionId =
                    getIntent().getStringExtra(
                            "session_id"
                    );

            // Check whether a valid conversation ID was received
            if (sessionId != null
                    && !sessionId.isEmpty()) {

                // Load the old messages from Firebase
                loadHistoricalConversation(
                        sessionId
                );

            } else {

                // Show an error if the conversation ID is missing
                Toast.makeText(
                        this,
                        "Conversation ID not found.",
                        Toast.LENGTH_LONG
                ).show();
            }

            /*
             * History mode is read-only.
             *
             * The user cannot send, record,
             * or select new messages.
             */
            btnSend.setEnabled(false);
            btnMic.setEnabled(false);
            btnPlus.setEnabled(false);
            etText.setEnabled(false);

        } else {

            /*
             * This is a new conversation.
             *
             * Generate a unique ID for the conversation.
             */
            sessionId =
                    UUID.randomUUID().toString();

            /*
             * Show an initial greeting from the AI.
             */
            addMessage(
                    "ai",
                    "Hello 👋 How do you feel today?"
            );
        }


        /*
         * Open the HistoryActivity when
         * the History button is clicked.
         */
        btnHistory.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            StressRelief.this,
                            HistoryActivity.class
                    );

            startActivity(intent);
        });


        /*
         * Send a text message when
         * the send button is clicked.
         */
        btnSend.setOnClickListener(v -> {

            // Do nothing if the screen is in history mode
            if (isHistoryMode) {
                return;
            }

            // Get the text entered by the user
            String msg =
                    etText.getText()
                            .toString()
                            .trim();

            // Do not send an empty message
            if (msg.isEmpty()) {
                return;
            }

            // Show the user's message in the chat
            addMessage(
                    "user",
                    msg
            );

            // Clear the text box
            etText.setText("");

            // Send the message to the backend
            sendText(msg);
        });


        /*
         * Start voice recording when
         * the microphone button is clicked.
         */
        btnMic.setOnClickListener(v -> {

            // Do nothing in history mode
            if (isHistoryMode) {
                return;
            }

            // Temporarily show that a voice message was sent
            addMessage(
                    "user",
                    "🎤 Voice message"
            );

            // Open the audio recording screen
            Intent intent =
                    new Intent(
                            StressRelief.this,
                            RecordActivity.class
                    );

            // Open RecordActivity and wait for the audio result
            startActivityForResult(
                    intent,
                    RECORD_AUDIO
            );
        });


        /*
         * Open the phone's file picker when
         * the plus button is clicked.
         */
        btnPlus.setOnClickListener(v -> {

            // Do nothing in history mode
            if (isHistoryMode) {
                return;
            }

            // Create an intent for selecting a file
            Intent intent =
                    new Intent(
                            Intent.ACTION_GET_CONTENT
                    );

            // Only allow audio files to be selected
            intent.setType("audio/*");

            // Open the file picker
            startActivityForResult(
                    intent,
                    PICK_AUDIO
            );
        });
    }


    /*
     * ============================================================
     * ADD MESSAGE
     * ============================================================
     *
     * This method adds a new message to the chat.
     *
     * It:
     * - Creates a timestamp
     * - Adds the message to the message list
     * - Updates the RecyclerView
     * - Scrolls to the newest message
     * - Saves the message in Firebase
     */
    private void addMessage(
            String sender,
            String text
    ) {

        // Get the current time for the message
        long timestamp =
                System.currentTimeMillis();

        // Create and add a new Message object
        messages.add(
                new Message(
                        sender,
                        text,
                        timestamp
                )
        );

        // Tell the adapter that a new message was added
        adapter.notifyItemInserted(
                messages.size() - 1
        );

        // Move the chat list to the newest message
        recyclerChat.scrollToPosition(
                messages.size() - 1
        );

        /*
         * Do not save messages when the user
         * is only viewing an old conversation.
         */
        if (isHistoryMode) {
            return;
        }

        // Get the currently logged-in Firebase user
        FirebaseUser user =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        // Check whether a user is logged in
        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Save the message under the user's chat history
        firebaseChatHelper.saveMessage(
                user.getUid(),
                sender,
                text,
                timestamp,
                sessionId
        );
    }


    /*
     * ============================================================
     * LOAD HISTORICAL CONVERSATION
     * ============================================================
     *
     * This method loads an old conversation from Firebase.
     *
     * It finds all messages that belong to the selected
     * conversation ID and displays them in the chat.
     */
    private void loadHistoricalConversation(String selectedSessionId) {

        // Get the currently logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check whether the user is logged in
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         * Open the user's chat history collection.
         *
         * Only messages with the selected session ID
         * are loaded.
         */
        db.collection("users")
                .document(user.getUid())
                .collection("chatHistory")
                .whereEqualTo("sessionId", selectedSessionId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // Clear old messages before loading new ones
                    messages.clear();

                    // Go through every message returned by Firebase
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                        // Get the sender name
                        String sender = document.getString("sender");

                        // Get the message text
                        String text = document.getString("text");

                        // Get the message timestamp
                        Long timestampValue = document.getLong("timestamp");

                        // Use 0 if no timestamp is stored
                        long timestamp = timestampValue != null
                                ? timestampValue
                                : 0L;

                        // Add the message only if sender and text exist
                        if (sender != null && text != null) {
                            messages.add(
                                    new Message(
                                            sender,
                                            text,
                                            timestamp
                                    )
                            );
                        }
                    }

                    // Sort messages from oldest to newest
                    Collections.sort(messages, (m1, m2) ->
                            Long.compare(
                                    m1.getTimestamp(),
                                    m2.getTimestamp()
                            )
                    );

                    // Refresh the RecyclerView
                    adapter.notifyDataSetChanged();

                    // Tell the user if no messages were found
                    if (messages.isEmpty()) {
                        Toast.makeText(
                                StressRelief.this,
                                "No messages found for this conversation",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    // Scroll to the last message
                    recyclerChat.scrollToPosition(messages.size() - 1);
                })
                .addOnFailureListener(e -> {

                    // Show an error if Firebase fails to load the messages
                    Toast.makeText(
                            StressRelief.this,
                            "Failed to load conversation: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    /*
     * ============================================================
     * SEND TEXT TO BACKEND
     * ============================================================
     *
     * This method sends the user's text to the backend server.
     *
     * The backend processes the message and returns
     * information such as the user's mood and an AI response.
     */
    private void sendText(
            String msg
    ) {

        // Create an HTTP client
        OkHttpClient client =
                new OkHttpClient();

        /*
         * Create the request data.
         *
         * The user's message is sent using
         * a form field called "text".
         */
        RequestBody body =
                new MultipartBody.Builder()
                        .setType(
                                MultipartBody.FORM
                        )
                        .addFormDataPart(
                                "text",
                                msg
                        )
                        .build();

        // Create the HTTP request
        Request request =
                new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

        // Send the request in the background
        client.newCall(request)
                .enqueue(
                        new Callback() {

                            /*
                             * This method runs when the network request fails.
                             */
                            @Override
                            public void onFailure(
                                    @NonNull Call call,
                                    @NonNull java.io.IOException e
                            ) {

                                // Show a network error to the user
                                runOnUiThread(() ->
                                        Toast.makeText(
                                                StressRelief.this,
                                                "Network failure.",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                );
                            }


                            /*
                             * This method runs when the server
                             * successfully sends a response.
                             */
                            @Override
                            public void onResponse(
                                    @NonNull Call call,
                                    @NonNull Response response
                            ) throws java.io.IOException {

                                // Check whether the response contains data
                                if (response.body() != null) {

                                    // Read the server response as text
                                    String res =
                                            response.body()
                                                    .string();

                                    // Process the response on the UI thread
                                    runOnUiThread(() ->
                                            handle(res)
                                    );
                                }
                            }
                        }
                );
    }


    /*
     * ============================================================
     * HANDLE AI RESPONSE
     * ============================================================
     *
     * This method reads the JSON response received
     * from the backend.
     *
     * It gets:
     * - The detected mood
     * - The AI response
     *
     * Then it displays the AI message and saves
     * the latest mood locally.
     */
    private void handle(
            String json
    ) {

        try {

            // Convert the server response into a JSON object
            JSONObject obj =
                    new JSONObject(json);

            // Get the first result from the response
            JSONObject result =
                    obj.getJSONArray("results")
                            .getJSONObject(0);

            // Get the detected mood
            String mood =
                    result.getString("mood");

            // Get the AI's reply
            String response =
                    result.getString("response");

            // Display the AI response in the chat
            addMessage(
                    "ai",
                    response
            );

            /*
             * Save the latest detected mood in
             * Android SharedPreferences.
             */
            getSharedPreferences(
                    "NutriMindPrefs",
                    MODE_PRIVATE
            )
                    .edit()
                    .putString(
                            "latestMood",
                            mood
                    )
                    .apply();

        } catch (Exception e) {

            /*
             * If the JSON response cannot be read,
             * show a simple error message.
             */
            addMessage(
                    "ai",
                    "Sorry, I couldn't understand."
            );
        }
    }


    /*
     * ============================================================
     * ACTIVITY RESULT
     * ============================================================
     *
     * This method receives the result after the user:
     * - Selects an audio file
     * - Records a voice message
     */
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        // Stop if the user cancelled the action
        if (resultCode != RESULT_OK
                || data == null) {

            return;
        }

        /*
         * Handle an audio file selected
         * from the phone.
         */
        if (requestCode == PICK_AUDIO) {

            // Get the selected file location
            Uri uri =
                    data.getData();

            if (uri != null) {

                // Copy the selected file to the app cache
                File file =
                        copyFile(uri);

                // Upload the copied file if it exists
                if (file != null) {

                    uploadAudio(file);
                }
            }

            /*
             * Handle audio recorded using RecordActivity.
             */
        } else if (requestCode == RECORD_AUDIO) {

            // Get the path of the recorded audio file
            String path =
                    data.getStringExtra(
                            "audioPath"
                    );

            // Upload the recorded file
            if (path != null) {

                uploadAudio(
                        new File(path)
                );
            }
        }
    }


    /*
     * ============================================================
     * COPY AUDIO FILE
     * ============================================================
     *
     * This method copies the selected audio file
     * into the application's cache folder.
     *
     * This makes the file easier to send to the backend.
     */
    private File copyFile(
            Uri uri
    ) {

        try {

            // Get the app's temporary cache directory
            File outputDir =
                    getCacheDir();

            // Create a temporary audio file
            File outputFile =
                    File.createTempFile(
                            "upload_audio",
                            ".tmp",
                            outputDir
                    );

            /*
             * Open the selected file for reading
             * and the new file for writing.
             */
            try (
                    InputStream inputStream =
                            getContentResolver()
                                    .openInputStream(uri);

                    FileOutputStream outputStream =
                            new FileOutputStream(
                                    outputFile
                            )
            ) {

                // Stop if the selected file cannot be opened
                if (inputStream == null) {
                    return null;
                }

                // Create a small temporary memory buffer
                byte[] buffer =
                        new byte[4096];

                int bytesRead;

                /*
                 * Read the audio file in small pieces
                 * and write them into the new file.
                 */
                while (
                        (bytesRead =
                                inputStream.read(buffer))
                                != -1
                ) {

                    outputStream.write(
                            buffer,
                            0,
                            bytesRead
                    );
                }

                // Return the copied file
                return outputFile;
            }

        } catch (Exception e) {

            // Print the error for debugging
            e.printStackTrace();

            // Return null when copying fails
            return null;
        }
    }


    /*
     * ============================================================
     * UPLOAD AUDIO
     * ============================================================
     *
     * This method sends an audio file to the backend server.
     *
     * The backend processes the audio and returns
     * the detected mood and AI response.
     */
    private void uploadAudio(
            File file
    ) {

        // Create an HTTP client
        OkHttpClient client =
                new OkHttpClient();

        // Prepare the audio file for uploading
        RequestBody fileBody =
                RequestBody.create(
                        file,
                        MediaType.parse("audio/*")
                );

        /*
         * Create a multipart request.
         *
         * The audio file is added using the field name "file".
         */
        RequestBody body =
                new MultipartBody.Builder()
                        .setType(
                                MultipartBody.FORM
                        )
                        .addFormDataPart(
                                "file",
                                file.getName(),
                                fileBody
                        )
                        .build();

        // Create the request for the backend server
        Request request =
                new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

        // Upload the audio in the background
        client.newCall(request)
                .enqueue(
                        new Callback() {

                            /*
                             * Runs when the audio upload fails.
                             */
                            @Override
                            public void onFailure(
                                    @NonNull Call call,
                                    @NonNull java.io.IOException e
                            ) {

                                // Display an error message in the chat
                                runOnUiThread(() ->
                                        addMessage(
                                                "ai",
                                                "Failed to upload voice message."
                                        )
                                );
                            }


                            /*
                             * Runs when the server sends
                             * a successful response.
                             */
                            @Override
                            public void onResponse(
                                    @NonNull Call call,
                                    @NonNull Response response
                            ) throws java.io.IOException {

                                // Check that the server returned a response
                                if (response.body() != null) {

                                    // Read the response as text
                                    String res =
                                            response.body()
                                                    .string();

                                    // Process the AI response
                                    runOnUiThread(() ->
                                            handle(res)
                                    );
                                }
                            }
                        }
                );
    }
}

