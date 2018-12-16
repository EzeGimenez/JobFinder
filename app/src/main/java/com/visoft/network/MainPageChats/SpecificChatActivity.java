package com.visoft.network.MainPageChats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.visoft.network.Objects.Message;
import com.visoft.network.Objects.User;
import com.visoft.network.Objects.ViewHolderChats;
import com.visoft.network.Profiles.ProfileActivity;
import com.visoft.network.R;
import com.visoft.network.Util.Constants;
import com.visoft.network.Util.Database;
import com.visoft.network.Util.GlideApp;
import com.visoft.network.Util.Gsoner;
import com.visoft.network.Util.Messenger;

import de.hdodenhof.circleimageview.CircleImageView;


public class SpecificChatActivity extends AppCompatActivity {
    public static boolean isRunning;
    private String chatID;
    private User receiver;
    private FirebaseRecyclerAdapter<String, ViewHolderChats> recyclerViewAdapter;
    private DatabaseReference messagesRef;

    private int currentCantOfMessages;

    //Componentes gráficas
    private RecyclerView listView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specific_chat_activity);

        chatID = getIntent().getStringExtra("chatid");
        receiver = (User) getIntent().getSerializableExtra("receiver");
        DatabaseReference database = Database.getDatabase().getReference();

        listView = findViewById(R.id.listViewSpecificChat);
        ((TextView) findViewById(R.id.tvReceiver)).setText(receiver.getUsername());
        CircleImageView ivPic = findViewById(R.id.ivPic);

        findViewById(R.id.buttonBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.ContainerReceiver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpecificChatActivity.this, ProfileActivity.class);
                intent.putExtra("user", receiver);
                startActivity(intent);
            }
        });

        if (receiver.getHasPic()) {
            StorageReference storage = FirebaseStorage.getInstance().getReference();

            StorageReference userRef = storage.child(Constants.FIREBASE_USERS_CONTAINER_NAME + "/" + receiver.getUid() + receiver.getImgVersion() + ".jpg");
            GlideApp.with(this)
                    .load(userRef)
                    .into(ivPic);
        } else {
            ivPic.setImageDrawable(getResources().getDrawable(R.drawable.profile_pic));
        }

        new Messenger(this, FirebaseAuth.getInstance().getCurrentUser().getUid(), receiver.getUid(), (ViewGroup) findViewById(R.id.rootView), listView, database);

        messagesRef = Database
                .getDatabase()
                .getReference(Constants.FIREBASE_MESSAGES_CONTAINER_NAME)
                .child(chatID);
        final Query query = messagesRef.limitToLast(10);

        currentCantOfMessages = 10;

        recyclerViewAdapter = new ListViewChatsAdapter(String.class, 0, ViewHolderChats.class, query);
        listView.setLayoutManager(new LinearLayoutManager(SpecificChatActivity.this));
        listView.setAdapter(recyclerViewAdapter);

        // Scroll to bottom on new messages
        recyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                listView.smoothScrollToPosition(recyclerViewAdapter.getItemCount());
            }
        });

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(-1)) {
                    currentCantOfMessages += 10;
                    Query q = messagesRef.limitToLast(currentCantOfMessages);
                    recyclerViewAdapter = new ListViewChatsAdapter(String.class, 0, ViewHolderChats.class, q);
                    listView.setAdapter(recyclerViewAdapter);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private class ListViewChatsAdapter extends FirebaseRecyclerAdapter<String, ViewHolderChats> {
        private Gson gson;
        private LayoutInflater inflater;

        public ListViewChatsAdapter(Class<String> modelClass, int modelLayout, Class<ViewHolderChats> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
            gson = Gsoner.getGson();

        }

        @NonNull
        @Override
        public ViewHolderChats onCreateViewHolder(ViewGroup parent, int viewType) {
            inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case 0: //received message, previous one same person
                    return new ViewHolderChats(inflater.inflate(R.layout.message_received_cont, parent, false));
                case 1: //received message, previous one other person
                    return new ViewHolderChats(inflater.inflate(R.layout.message_received_change, parent, false));
                case 2: //sent message, previous one same person
                    return new ViewHolderChats(inflater.inflate(R.layout.message_sent_cont, parent, false));
                case 3: //sent message, previous one is user
                    return new ViewHolderChats(inflater.inflate(R.layout.message_sent_change, parent, false));
                default:
                    return null;
            }
        }

        @Override
        protected void populateViewHolder(ViewHolderChats holder, String str, int position) {
            Message msg = gson.fromJson(str, Message.class);

            msg.fillHolder(inflater.getContext(), holder);

            holder.setTimeStamp(msg.getTimeStamp());
        }

        @Override
        public int getItemViewType(int position) {
            Message msg = gson.fromJson(getItem(position), Message.class);

            String authorUID = msg.getAuthor();
            if (authorUID.equals(receiver.getUid())) { //Received message
                if (position > 0 && gson.fromJson(getItem(position - 1), Message.class).getAuthor().equals(receiver.getUid())) {
                    return 0;
                } else {
                    return 1;
                }
            } else { //Sent Message
                if (position > 0 && !gson.fromJson(getItem(position - 1), Message.class).getAuthor().equals(receiver.getUid())) {
                    return 2;
                } else {
                    return 3;
                }
            }
        }
    }
}