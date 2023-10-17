package com.example.puertacovid;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.example.puertacovid.Common.Common;
import com.example.puertacovid.Listener.IFirebaseLoadFailed;
import com.example.puertacovid.Listener.ILoadTimeFromFirebaseListener;
import com.example.puertacovid.Model.ChatInfoModel;
import com.example.puertacovid.Model.ChatMessageModel;
import com.example.puertacovid.ViewHolders.ChatPictureHolder;
import com.example.puertacovid.ViewHolders.ChatPictureReceiveHolder;
import com.example.puertacovid.ViewHolders.ChatTextHolder;
import com.example.puertacovid.ViewHolders.ChatTextReceiveHolder;
import com.example.puertacovid.sdktest.JoinVideoCall;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ChatActivity extends AppCompatActivity implements ILoadTimeFromFirebaseListener, IFirebaseLoadFailed {
private static final int MY_CAMERA_REQUEST_CODE=7171;
    private static final int MY_RESULT_LOAD_IMAGE=7172;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.img_camera)
    ImageView img_camera;


    @BindView(R.id.edt_chat)
    AppCompatEditText edt_chat;
    @BindView(R.id.img_send)
    ImageView img_send;
    @BindView(R.id.recycler_chat)
    RecyclerView recycler_chat;


    @BindView(R.id.img_preview)
    ImageView img_preview;

    @BindView(R.id.img_avatar)
    ImageView img_avatar;

    @BindView(R.id.img_call)
    ImageView img_call;
    @BindView(R.id.txt_name)
    TextView txt_name;


FirebaseDatabase database;
DatabaseReference chatRef,offsetRef;
ILoadTimeFromFirebaseListener listener;
IFirebaseLoadFailed errorListener;
FirebaseRecyclerAdapter<ChatMessageModel,RecyclerView.ViewHolder> adapter;
    FirebaseRecyclerOptions<ChatMessageModel>options;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    Uri fileUri;
    StorageReference storageReference;
    LinearLayoutManager layoutManager;

    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                ... other events
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    // Example for handling different JitsiMeetSDK events
    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Timber.i("Conference Joined with url%s", event.getData().get("url"));
                    break;
                case PARTICIPANT_JOINED:
                    Timber.i("Participant joined%s", event.getData().get("name"));
                    break;
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);

    }
    @OnClick(R.id.img_call)
    void onClickCall(){
        // Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // When using JaaS, set the obtained JWT here
                //.setToken("MyJWT")
                // Different features flags can be set
                // .setFeatureFlag("toolbox.enabled", false)
                // .setFeatureFlag("filmstrip.enabled", false)
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        registerForBroadcastMessages();


        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom("Reunion Admins")
                // Settings for audio and video
                //.setAudioMuted(true)
                //.setVideoMuted(true)
                .build();
        // Launch the new activity with the given options. The launch() method takes care
        // of creating the required Intent and passing the options.
        JitsiMeetActivity.launch(this, options);

    }



    @OnClick(R.id.img_camera)
    void onCaptureImageClick(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"From your Camera");
        fileUri=getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values
        );
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
        startActivityForResult(intent,MY_CAMERA_REQUEST_CODE);
    }
    @OnClick(R.id.img_send)
    void onSubmitChatClick(){
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
long offset = snapshot.getValue(Long.class);
long estimatedServerTimeInMs = System.currentTimeMillis()+offset;
listener.onLoadOnlyTimeSuccess(estimatedServerTimeInMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            errorListener.onError(error.getMessage());
            }
        });
    }
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
if(requestCode==MY_CAMERA_REQUEST_CODE){

    if(resultCode==RESULT_OK) {


        try {
            Bitmap thumbnail=MediaStore.Images.Media
                    .getBitmap(
                            getContentResolver(),
                            fileUri
                    );

            img_preview.setImageBitmap(thumbnail);
            img_preview.setVisibility(View.VISIBLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
        else if(requestCode==MY_RESULT_LOAD_IMAGE){
if(requestCode==RESULT_OK){
try {
    final Uri imageUri=data.getData();
    InputStream inputStream = getContentResolver()
            .openInputStream(imageUri);
    Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
    img_preview.setImageBitmap(selectedImage);
    img_preview.setVisibility(View.VISIBLE);
    fileUri=imageUri;
} catch (FileNotFoundException e) {
    e.printStackTrace();
}
}
        }

        else
            Toast.makeText(this, "Please Choose an image", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        if(adapter!=null)adapter.startListening();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
adapter.startListening();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initViews();
        loadChatContent();

    }

    private void loadChatContent() {
    String receiverId=FirebaseAuth.getInstance()
            .getCurrentUser().getUid();
    adapter=new FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder>(options) {
        @Override
        public int getItemViewType(int position) {
            if(!adapter.getItem(position).getSenderId()
            .equals(receiverId))
                return !adapter.getItem(position).isPicture()?2:3;
            else
                return !adapter.getItem(position).isPicture()?0:1;
        }

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull @NotNull ChatMessageModel model) {
        if(holder instanceof ChatTextHolder) {
        ChatTextHolder chatTextHolder= (ChatTextHolder)holder;
        chatTextHolder.txt_chat_message.setText(model.getContent());
        chatTextHolder.txt_time.setText(
                DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                        Calendar.getInstance().getTimeInMillis(),0)
                .toString());
            }
        else
            if(holder instanceof ChatTextReceiveHolder){
                ChatTextReceiveHolder chatTextHolder= (ChatTextReceiveHolder)holder;
                chatTextHolder.txt_chat_message.setText(model.getContent());
                chatTextHolder.txt_time.setText(
                        DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                Calendar.getInstance().getTimeInMillis(),0)
                                .toString());
            }
            else if(holder instanceof ChatPictureHolder){
                ChatPictureHolder chatPictureHolder=(ChatPictureHolder)holder;
                chatPictureHolder.txt_chat_message.setText(model.getContent());
                chatPictureHolder.txt_time.setText(
                        DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                Calendar.getInstance().getTimeInMillis(),0)
                                .toString());
                Glide.with(ChatActivity.this)
                        .load(model.getPictureLink())
                        .into(chatPictureHolder.img_preview);
            }
    else if(holder instanceof ChatPictureReceiveHolder){
                ChatPictureReceiveHolder chatPictureHolder=(ChatPictureReceiveHolder)holder;
                chatPictureHolder.txt_chat_message.setText(model.getContent());
                chatPictureHolder.txt_time.setText(
                        DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                Calendar.getInstance().getTimeInMillis(),0)
                                .toString());
                Glide.with(ChatActivity.this)
                        .load(model.getPictureLink())
                        .into(chatPictureHolder.img_preview);
            }

        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view;
            if(viewType==0) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_text_own,parent,false);
                return new ChatTextReceiveHolder(view);
            }
            else if(viewType==1)
            {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_picture_friend,parent,false);
                return new ChatPictureReceiveHolder(view);

            }
            else if(viewType==2)
            {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_text_friend,parent,false);
                return new ChatTextHolder(view);

            }
            else{
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_picture_own,parent,false);
                return new ChatPictureHolder(view);

            }
        }
    };
adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        int friendlyMessageCount = adapter.getItemCount();
        int lastVisiblePosition=layoutManager.findLastVisibleItemPosition();
        if(lastVisiblePosition==-1 ||
                (positionStart >=(friendlyMessageCount-1)&&
                        lastVisiblePosition==(positionStart -1))){
            recycler_chat.scrollToPosition(positionStart);
        }
    }
});
recycler_chat.setAdapter(adapter);
    }

    private void initViews() {
    listener=this;
    errorListener=this;
    database=FirebaseDatabase.getInstance();
    chatRef=database.getReference(Common.CHAT_REFERENCE);
    offsetRef=database.getReference(".info/serverTimeOffset");
        Query query = chatRef.child(Common.generateChatRoomId(
                Common.chatUser.getUid(), FirebaseAuth.getInstance().getCurrentUser().getUid()
        )).child(Common.CHAT_DETAIL_REFERENCE);

        options = new FirebaseRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();
        ButterKnife.bind(this);
        layoutManager=new LinearLayoutManager(this);
        recycler_chat.setLayoutManager(layoutManager);
        ColorGenerator generator=ColorGenerator.MATERIAL;
        int color =generator.getColor(Common.chatUser.getUid());
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .round();
        TextDrawable drawable = builder.build(Common.chatUser.getFirstname().substring(0,1),color);
        img_avatar.setImageDrawable(drawable);
        txt_name.setText(Common.getName(Common.chatUser));


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMs) {
    ChatMessageModel chatMessageModel = new ChatMessageModel();
    chatMessageModel.setName(Common.getName(Common.currentUser));
    chatMessageModel.setContent(edt_chat.getText().toString());
    chatMessageModel.setTimeStamp(estimateTimeInMs);
    chatMessageModel.setSenderId(FirebaseAuth.getInstance().getCurrentUser().getUid());

if(fileUri==null){
    chatMessageModel.setPicture(false);
    submitChatToFirebase(chatMessageModel,chatMessageModel.isPicture(),estimateTimeInMs);

}
else{
    uploadPicture(fileUri,chatMessageModel,estimateTimeInMs);
}

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadPicture(Uri fileUri, ChatMessageModel chatMessageModel, long estimateTimeInMs) {
        AlertDialog dialog = new AlertDialog.Builder(ChatActivity.this)
        .setCancelable(false)
                .setMessage("Please wait....")
                .create();
        dialog.show();

        String filename = Common.getFileName(getContentResolver(),fileUri);
        String path =new StringBuilder(Common.chatUser.getUid())
                .append("/")
                .append(filename)
                .toString();

        storageReference= FirebaseStorage.getInstance()
                .getReference()
                .child(path);

        UploadTask uploadTask=storageReference.putFile(fileUri);
        Task<Uri> task=uploadTask.continueWithTask(task1->{
           if(!task1.isSuccessful())
               Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show();
                return storageReference.getDownloadUrl();

        }).addOnCompleteListener(task12->{
            if(task12.isSuccessful()){
                String url=task12.getResult().toString();
                dialog.dismiss();
                chatMessageModel.setPicture(true);
                chatMessageModel.setPictureLink(url);
                submitChatToFirebase(chatMessageModel,chatMessageModel.isPicture(),estimateTimeInMs);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitChatToFirebase(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
    chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
            FirebaseAuth.getInstance().getCurrentUser().getUid()))
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
    if(snapshot.exists())
        appendChat(chatMessageModel,isPicture,estimateTimeInMs);
    else
        createChat(chatMessageModel,isPicture,estimateTimeInMs);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
}

    private void appendChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        Map<String,Object> update_data = new HashMap<>();
        update_data.put("lastUpdate",estimateTimeInMs);
        if(isPicture)
            update_data.put("lastMessage","<Image>");
        else
            update_data.put("lastMessage",chatMessageModel.getContent());



        FirebaseDatabase.getInstance()
                .getReference(Common.CHAT_LIST_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Common.chatUser.getUid())
                .updateChildren(update_data)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance()
                            .getReference(Common.CHAT_LIST_REFERENCE)
                            .child(Common.chatUser.getUid())
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(update_data)
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(aVoid1 -> {
                                chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
                                        FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                        .child(Common.CHAT_DETAIL_REFERENCE)
                                        .push()
                                        .setValue(chatMessageModel)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    edt_chat.setText("");
                                                    edt_chat.requestFocus();
                                                    if(adapter !=null){
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                    if(isPicture){
                                                        fileUri=null;
                                                        img_preview.setVisibility(View.GONE);

                                                    }

                                                }

                                            }
                                        });
                            });
                });
}

    private void createChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        ChatInfoModel chatInfoModel = new ChatInfoModel();
        chatInfoModel.setCreateId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        chatInfoModel.setFriendName(Common.getName(Common.chatUser));
        chatInfoModel.setFriendId(Common.chatUser.getUid());
        chatInfoModel.setCreateName(Common.getName(Common.currentUser));
        if(isPicture)
            chatInfoModel.setLastMessage("<Image>");
        else
            chatInfoModel.setLastMessage(chatMessageModel.getContent());
        chatInfoModel.setLastUpdate(estimateTimeInMs);
        chatInfoModel.setCreateDate(estimateTimeInMs);


        FirebaseDatabase.getInstance()
                .getReference(Common.CHAT_LIST_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Common.chatUser.getUid())
                .setValue(chatInfoModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
        .addOnSuccessListener(aVoid -> {
            FirebaseDatabase.getInstance()
                    .getReference(Common.CHAT_LIST_REFERENCE)
                    .child(Common.chatUser.getUid())
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(chatInfoModel)
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(aVoid1 -> {
                        chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
                                FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                .child(Common.CHAT_DETAIL_REFERENCE)
                                .push()
                                .setValue(chatMessageModel)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                })
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                edt_chat.setText("");
                                edt_chat.requestFocus();
                                if(adapter !=null){
                                    adapter.notifyDataSetChanged();

                                }
                                if(isPicture){
                                    fileUri=null;
                                    img_preview.setVisibility(View.GONE);

                                }

                            }

                            }
                        });
                    });
        });













    }

@Override
    public void onError(String message){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}