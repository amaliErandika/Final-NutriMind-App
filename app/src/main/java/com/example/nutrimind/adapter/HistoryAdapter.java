package com.example.nutrimind.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrimind.R;
import com.example.nutrimind.model.Message;

import java.util.ArrayList;

public class HistoryAdapter
        extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final ArrayList<Message> messages;

    private final OnItemClickListener listener;

    public interface OnItemClickListener {

        void onItemClick(Message message);
    }

    public HistoryAdapter(
            ArrayList<Message> messages,
            OnItemClickListener listener
    ) {

        this.messages = messages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_history,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Message message =
                messages.get(position);

        String displayText =
                message.getText();

        /*
         * displayText contains:
         *
         * Date
         * Preview
         */
        if (displayText != null
                && displayText.contains("\n")) {

            String[] parts =
                    displayText.split(
                            "\n",
                            2
                    );

            holder.txtSessionDate.setText(
                    parts[0]
            );

            if (parts.length > 1) {

                holder.txtSessionPreview.setText(
                        parts[1]
                );

            } else {

                holder.txtSessionPreview.setText(
                        ""
                );
            }

        } else {

            holder.txtSessionDate.setText(
                    ""
            );

            holder.txtSessionPreview.setText(
                    displayText
            );
        }

        /*
         * Handle history click.
         */
        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onItemClick(
                                message
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {

        return messages.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtSessionDate;
        TextView txtSessionPreview;

        ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            txtSessionDate =
                    itemView.findViewById(
                            R.id.txtSessionDate
                    );

            txtSessionPreview =
                    itemView.findViewById(
                            R.id.txtSessionPreview
                    );
        }
    }
}