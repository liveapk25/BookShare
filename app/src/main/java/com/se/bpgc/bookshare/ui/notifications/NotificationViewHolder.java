package com.se.bpgc.bookshare.ui.notifications;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.se.bpgc.bookshare.R;

class NotificationViewHolder extends RecyclerView.ViewHolder{
    TextView requestText;

    ImageView cover;
    TextView title;
    TextView author;

    View acceptDeclineContainer;
    Button accept;
    Button decline;

    View contactContainer;
    FloatingActionButton phone;
    FloatingActionButton sms;
    FloatingActionButton email;
    FloatingActionButton delete;

    View infoContainer;
    FloatingActionButton detail;
    FloatingActionButton cancel;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        requestText = itemView.findViewById(R.id.request_text_notif);
        acceptDeclineContainer = itemView.findViewById(R.id.accept_decline_container_notif);
        accept = itemView.findViewById(R.id.accept_button_notif);
        decline = itemView.findViewById(R.id.decline_button_notif);

        cover = itemView.findViewById(R.id.cover_notif);
        title = itemView.findViewById(R.id.title_notif);
        author = itemView.findViewById(R.id.author_notif);

        contactContainer = itemView.findViewById(R.id.contact_container_notif);
        phone = itemView.findViewById(R.id.phone_button_notif);
        sms = itemView.findViewById(R.id.sms_button_notif);
        email = itemView.findViewById(R.id.email_button_notif);
        delete = itemView.findViewById(R.id.delete_button_notif);

        infoContainer = itemView.findViewById(R.id.info_container_notif);
        detail = itemView.findViewById(R.id.detail_button_notif);
        cancel = itemView.findViewById(R.id.cancel_button_notif);

    }
}
