package com.example.nutrimind.adapter;

// Android classes used to create and display the chat items
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

// RecyclerView classes used to display the chat list
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Project resources and Message model
import com.example.nutrimind.R;
import com.example.nutrimind.model.Message;

import java.util.ArrayList;


/*
 * This adapter is used to display chat messages
 * inside the RecyclerView.
 *
 * It displays user messages and AI messages
 * using two different layouts.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // List containing all chat messages
    private final ArrayList<Message> messages;

    /*
     * These values identify the type of message.
     *
     * USER = message sent by the user
     * AI   = message sent by the AI
     */
    private static final int USER = 1;
    private static final int AI = 2;


    /*
     * Constructor of the ChatAdapter.
     *
     * It receives the list of chat messages
     * that needs to be displayed.
     */
    public ChatAdapter(ArrayList<Message> messages) {

        // Store the message list
        this.messages = messages;
    }


    /*
     * This method checks who sent the message.
     *
     * If the sender is "user", it returns USER.
     * Otherwise, it returns AI.
     *
     * This helps the RecyclerView decide which
     * layout should be used for the message.
     */
    @Override
    public int getItemViewType(int position) {

        // Get the sender of the current message
        if (messages.get(position).getSender().equals("user")) {

            // The message was sent by the user
            return USER;
        }

        // If it is not from the user, treat it as an AI message
        return AI;
    }


    /*
     * This method creates the layout for each message.
     *
     * User messages use item_user.xml.
     * AI messages use item_ai.xml.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        /*
         * Check whether the message is
         * a user message.
         */
        if (viewType == USER) {

            // Load the user message layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);

            // Create and return a UserHolder
            return new UserHolder(view);

        } else {

            // Load the AI message layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ai, parent, false);

            // Create and return an AIHolder
            return new AIHolder(view);
        }

    }


    /*
     * This method puts the message text
     * into the correct TextView.
     *
     * It checks whether the ViewHolder belongs
     * to the user or AI message layout.
     */
    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position) {

        // Get the message at the current position
        Message message = messages.get(position);


        /*
         * If the ViewHolder is for a user message,
         * display the message using UserHolder.
         */
        if (holder instanceof UserHolder) {

            ((UserHolder) holder).txtMessage.setText(message.getText());

        } else {

            /*
             * Otherwise, display the message
             * using AIHolder.
             */
            ((AIHolder) holder).txtMessage.setText(message.getText());

        }

    }


    /*
     * This method tells the RecyclerView
     * how many messages are in the list.
     */
    @Override
    public int getItemCount() {

        // Return the total number of chat messages
        return messages.size();
    }


    /*
     * UserHolder represents one user message.
     *
     * It stores the TextView used to display
     * the user's message.
     */
    static class UserHolder extends RecyclerView.ViewHolder {

        // TextView used to display the user message
        TextView txtMessage;


        /*
         * Constructor for UserHolder.
         *
         * It connects txtMessage with the
         * TextView from item_user.xml.
         */
        public UserHolder(@NonNull View itemView) {

            super(itemView);

            // Find the message TextView from the layout
            txtMessage = itemView.findViewById(R.id.txtMessage);

        }

    }


    /*
     * AIHolder represents one AI message.
     *
     * It stores the TextView used to display
     * the AI's response.
     */
    static class AIHolder extends RecyclerView.ViewHolder {

        // TextView used to display the AI message
        TextView txtMessage;


        /*
         * Constructor for AIHolder.
         *
         * It connects txtMessage with the
         * TextView from item_ai.xml.
         */
        public AIHolder(@NonNull View itemView) {

            super(itemView);

            // Find the message TextView from the layout
            txtMessage = itemView.findViewById(R.id.txtMessage);

        }

    }

}
