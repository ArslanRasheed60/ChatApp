package com.example.chatapp.conversation;

import static com.example.chatapp.Globals.CHAT_DB;
import static com.example.chatapp.Globals.Full_Name;
import static com.example.chatapp.Globals.MESSAGE_SENDER;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.Globals;
import com.example.chatapp.IChatInterface;
import com.example.chatapp.firebaseDb.ChatFirebaseDAO;
import com.example.chatapp.sqliteDB.ChatDbDAO;
import com.example.chatapp.R;
import com.example.chatapp.RandomText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Message> SRMessages;
    private EditText editText;
    RecyclerView recyclerViewMessageLists;
    private MessageAdaptor mAdaptor;

    //
    private String receiverId , receiverName;
    int recyclerViewItemId;
//    MyDataBaseHelper myDB;
    IChatInterface dao;
    long timeStamp;
    String Sender_Name;

    Bundle savedInstanceStateThis;
    MessageViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        dao = Globals.dao;

        dao = new ChatFirebaseDAO(new ChatFirebaseDAO.DataObserver() {
            @Override
            public void update() {
                refresh();
            }
        });

        //data passed from parent
        Intent intent = getIntent();
        receiverId = intent.getStringExtra("id");
        receiverName = intent.getStringExtra("name");
        recyclerViewItemId = intent.getIntExtra("recyclerViewItemId", -1);

        //view model
        vm = new ViewModelProvider(this).get(MessageViewModel.class);
        vm.setDao(dao);
        savedInstanceStateThis = savedInstanceState;
        SRMessages = vm.getMessages(savedInstanceState, "data", receiverId);

        recyclerViewMessageLists = (RecyclerView) findViewById(R.id.messageLists);
        recyclerViewMessageLists.setHasFixedSize(true);

        //linear layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessageLists.setLayoutManager(linearLayoutManager);

        //set id of edit text
        editText = findViewById(R.id.sender1Text);

        //adaptor
        mAdaptor = new MessageAdaptor(SRMessages);
        recyclerViewMessageLists.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerViewMessageLists.setAdapter(mAdaptor);


        timeStamp = -1;
        //getting sender name
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference reference = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(Full_Name);
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String fullName = dataSnapshot.getValue(String.class);
//                // do something with the fullName value
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // handle the error
//            }
//        });
    }


    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleOnClick(View v) {

        String convert_editText = editText.getText().toString();

        if(v.getId() == R.id.sender1TextBtn && !convert_editText.equals("")){

            timeStamp = System.currentTimeMillis();

            Message newMessage = new Message(MESSAGE_SENDER, convert_editText, timeStamp,0,dao );
            SRMessages.add(newMessage);
            newMessage.save(receiverId);
            mAdaptor.notifyDataSetChanged();
            editText.setText("");

//            //receiver
//            Message newMessage2 = new Message(receiverName, RandomText.generateRandomText(80), formattedTime,1,dao );
//            SRMessages.add(newMessage2);
//            newMessage2.save(receiverId);
//            mAdaptor.notifyDataSetChanged();

            //add message to last index of recycler view
            recyclerViewMessageLists.scrollToPosition(mAdaptor.getItemCount() - 1);
        }
        else{
            Toast toast = Toast.makeText(this,"Field is empty",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("id", receiverId); // put the data you want to send back in the intent
        String lastMessage = SRMessages.size() == 0 ? "" : SRMessages.get(SRMessages.size() - 1).getMessage();
        intent.putExtra("lastMessage", lastMessage);
        intent.putExtra("timeStamp", timeStamp);
        intent.putExtra("recyclerViewItemId", recyclerViewItemId);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    public void refresh(){
        SRMessages = Message.load(dao,receiverId);
        if(SRMessages != null){
            mAdaptor.updateData(SRMessages);
        }
    }

}