package com.example.puertacovid.Common;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;

import androidx.annotation.RequiresApi;

import com.example.puertacovid.Model.UserModel;

import java.util.Random;

public class Common {
    public static final String CHAT_LIST_REFERENCE = "ChatList" ;
    public static final String CHAT_REFERENCE = "Chat";
    public static final String CHAT_DETAIL_REFERENCE = "Detail";
    public static UserModel currentUser = new UserModel();
    public static final String USER_REFERENCES="People";
    public static UserModel chatUser = new UserModel();
    public static String userid2;

    public static String generateChatRoomId(String a, String b) {
userid2=chatUser.getUid();
        if(!userid2.equals("PPTdrTm23Rfw5txqjoO4iF7SlyC3")) {
            if(a.compareTo(b)>0)
                return new StringBuilder(a).append(b).toString();
            else if(a.compareTo(b)<0)
                return new StringBuilder(b).append(a).toString();
            else return new StringBuilder("Chat_Your_Self_Error")
                        .append(new Random().nextInt()).toString();
        }else {
            return "Admin";
        }

    }

    public static String getName(UserModel chatUser) {
    return new StringBuilder(chatUser.getFirstname())
            .append(" ")
            .append(chatUser.getLastname())
            .toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
    String result=null;
    if(fileUri.getScheme().equals("content"))
    {
        Cursor cursor = contentResolver.query(fileUri,null,null,null);
        try {
            if(cursor!=null&&cursor.moveToFirst())
                result=cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));



        }finally {
            {
                cursor.close();
            }

        }

    }
    if(result==null){
        result=fileUri.getPath();
        int cut=result.lastIndexOf("/");
        if(cut!=-1)
            result=result.substring(cut+1);
    }
    return result;
    }

}
