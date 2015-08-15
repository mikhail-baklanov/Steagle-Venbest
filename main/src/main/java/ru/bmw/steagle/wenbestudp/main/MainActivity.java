package ru.bmw.steagle.wenbestudp.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class MainActivity extends ActionBarActivity {

    private EditText dstIP;
    private EditText dstPort;
    private EditText srcPort;
    private EditText requestField;
    private TextView responceField;
    private AsyncTask<RequestData, Void, String> asyncTask;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dstIP = (EditText) findViewById(R.id.dstIP);
        dstPort = (EditText) findViewById(R.id.dstPort);
        srcPort = (EditText) findViewById(R.id.srcPort);

        requestField = (EditText) findViewById(R.id.requestString);
        responceField = (TextView) findViewById(R.id.responceString);
        buttonSend = (Button) findViewById(R.id.send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s;

                s = dstPort.getText().toString();
                int port;
                try {
                    port = Integer.parseInt(s);
                } catch (Throwable e) {
                    port = 0;
                }

                s = srcPort.getText().toString();
                int sPort;
                try {
                    sPort = s == null || s.trim().length() == 0 ? 0 : Integer.parseInt(s);
                } catch (Throwable e) {
                    sPort = 0;
                }

                String ip = dstIP.getText().toString();
                String message = requestField.getText().toString();
                sendRequest(ip, port, sPort, message);
            }
        });

    }

    private static class RequestData {
        private String ip;
        private int port;
        private int srcPort;
        private String message;

        private RequestData(String ip, int port, int srcPort, String message) {
            this.ip = ip;
            this.port = port;
            this.srcPort = srcPort;
            this.message = message;
        }
    }

    @Override
    protected void onPause() {
        stopAsyncTask();
        super.onPause();
    }

    private void stopAsyncTask() {
        if (asyncTask != null) {
            asyncTask.cancel(true);
            asyncTask = null;
        }
    }

    private void sendRequest(String ip, int port, int srcPort, String message) {
        asyncTask = new AsyncTask<RequestData, Void, String>() {
            @Override
            protected String doInBackground(RequestData... params) {

                String request = params[0].message;
                request = request.replace("<cr>", "\r");
                request = request.replace("<lf>", "\n");
                String ip = params[0].ip;
                int port = params[0].port;
                int srcPort = params[0].srcPort;
                String resultString = null;
                DatagramSocket socket = null;
                try {
                    byte[] data = request.getBytes("UTF-8");
                    socket = srcPort == 0 ? new DatagramSocket() : new DatagramSocket(srcPort);
                    socket.setSoTimeout(5000);
                    DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);
                    socket.send(packet);

                    byte[] buf = new byte[1024];
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    socket.receive(p);
                    byte[] res = p.getData();
                    resultString = new String(res, 0, p.getLength(), "UTF-8");
                } catch (Exception e) {
                    resultString = "Ошибка: " + e.getMessage();
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }

                return resultString;
            }

            protected void onPostExecute(String result) {
                asyncTask = null;
                responceField.setText(result);
                buttonSend.setEnabled(true);
            }
        };
        buttonSend.setEnabled(false);
        responceField.setText("Отправка...");
        asyncTask.execute(new RequestData(ip, port, srcPort, message));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up buttonSend, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
