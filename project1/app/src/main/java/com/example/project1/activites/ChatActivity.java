package com.example.project1.activites;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project1.R;
import com.example.project1.model.Message;
import com.example.project1.model.MessageAdapter;
import com.example.project1.sessionmanagement.UserSharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    UserSharedPreference sh = new UserSharedPreference(this);
    private EditText editTextMessage;
    private Button buttonSend;
    private List<Message> messages;
    private MessageAdapter adapter;
    private DatabaseReference messagesRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages);

        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        messagesRef = FirebaseDatabase.getInstance().getReference().child("messages");

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }


    private void sendMessageToGroup(String groupId, String messageText,String senderName) {
        String userId = mAuth.getCurrentUser().getUid();
        Log.d("firbase", "sendMessageToGroup: "+userId);
        String messageId = messagesRef.push().getKey();
        Log.d("firbase", "sendMessageToGroup: "+messageId);
        Message message = new Message(messageText, userId,senderName); // Now sending userId as sender instead of "Me"
//        Log.d("", "sendMessageToGroup: "SenderName);
        if (messageId != null) {
            messagesRef.child(groupId).child(messageId).setValue(message)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Message sent successfully
                            Log.d("firbase", "onSuccess: sent");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("firbase", e.toString());
                            // Failed to send message
                        }
                    });
            editTextMessage.getText().clear();
        }
    }
    private void retrieveMessagesForCurrentUser(String groupId) {
        // Reference to the messages for the current group
        DatabaseReference groupMessagesRef = FirebaseDatabase.getInstance().getReference().child("messages").child(groupId);

        // Add a ValueEventListener to listen for changes in the messages
        groupMessagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing messages list
                messages.clear();

                // Iterate through the dataSnapshot to retrieve messages
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
//                        Log.d("chatmsg", "onDataChange: "+message.getText().toString());
                        messages.add(message);
                    }
                }

                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged();

                // Scroll to the bottom of the RecyclerView
                scrollToBottom();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e("ChatActivity", "Failed to retrieve messages: " + databaseError.getMessage());
            }
        });
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            recyclerViewMessages.scrollToPosition(messages.size() - 1);
        }
    }

    // Call this method when the send button is clicked
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {

            sendMessageToGroup(sh.getTaskId(), messageText,sh.getFname());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("resume", "onResume: ");

        retrieveMessagesForCurrentUser(sh.getTaskId()
        );

    }
}
