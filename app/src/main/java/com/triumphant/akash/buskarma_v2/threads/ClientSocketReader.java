package com.triumphant.akash.buskarma_v2.threads;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by Akash on 4/25/2016.
 */
//reads data from the server pass it to mainThread
public class ClientSocketReader extends Thread{
    BufferedReader inFromServer;
    Message msg;
    String message;
    Handler referenceHandler;
    Bundle bundle;
    Context mainActivityContext;
    public ClientSocketReader(BufferedReader inFromServer, Handler handler, Context context) {
        this.inFromServer = inFromServer;
        this.referenceHandler = handler;
        mainActivityContext = context;
    }
    public void run(){
        Looper.prepare();

        try {
            System.out.println("client reader thread is up and running");
            while(true){
                Thread.sleep(1000);
                msg = Message.obtain();
                message = inFromServer.readLine();	//a new message is arrived
                if(message == null){
                    Toast.makeText(mainActivityContext, "server went down", Toast.LENGTH_LONG).show();
                    break;
                }
                bundle = new Bundle();
                bundle.putString("msg" ,message+"\n");
                msg.setData(bundle);
                referenceHandler.sendMessage(msg);
                Log.i("****TAG****", "SERVER: " + message + "\n");
            }
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Looper.loop();
    }
}
