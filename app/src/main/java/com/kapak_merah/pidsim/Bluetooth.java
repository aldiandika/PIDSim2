package com.kapak_merah.pidsim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

//import com.example.bttest.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.DialogInterface;
public class Bluetooth extends AppCompatActivity
{
    public double ambil;
    //widgets
    Button btnPaired;
    ListView devicelist, pairedlist, lvBayangan;
    //Bluetooth
    /*private*/ public static BluetoothAdapter myBluetooth = null;
    BroadcastReceiver scanBT;
    private Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayAdapter<BluetoothDevice> btArrayDevice;
    public static String EXTRA_ADDRESS = "device_address";
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    EditText input;
    Button btnOn, btnOff, btnDis, scan;
    SeekBar brightness;
    IntentFilter filter1;
    TextView status, dataSerial;
    String address = null;
    public String btname;
    private ProgressDialog progress;
    public static BluetoothSocket btSocket = null;
    public static boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //Calling widgets
        btnPaired = (Button)findViewById(R.id.connect);
        scan = (Button) findViewById(R.id.scan);
        btnDis = (Button) findViewById(R.id.disconnect);
        devicelist = (ListView)findViewById(R.id.listDevices);
        pairedlist = (ListView) findViewById(R.id.pairedlist);
        lvBayangan = new ListView(this);
        status = (TextView) findViewById(R.id.status);
        dataSerial = (TextView) findViewById(R.id.infoblutut);
        btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        btArrayDevice = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1);
        devicelist.setAdapter(btArrayAdapter);
        lvBayangan.setAdapter(btArrayDevice);
        input = (EditText)findViewById(R.id.input);
        btnDis.setEnabled(false);


        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList();
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan.setEnabled(false);
                btArrayAdapter.clear();
                myBluetooth.startDiscovery();
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
                btnPaired.setEnabled(true);
                btnDis.setEnabled(false);
            }
        });

        devicelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bt = (BluetoothDevice) lvBayangan.getItemAtPosition(position);
                new PairingBT(bt).execute();
            }
        });

        CheckBlueToothState();
//        filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(ActionFoundReceiver,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
//        registerReceiver(ActionFoundReceiver,filter1);


    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        registerReceiver(ActionFoundReceiver,
//                new IntentFilter(BluetoothDevice.ACTION_FOUND));
//        registerReceiver(ActionFoundReceiver,filter1);
//        myBluetooth.getProfileProxy(this, mProfileListener, BluetoothProfile.A2DP);
//    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == 1){
            CheckBlueToothState();
        }
    }


    BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {

                BluetoothA2dp btA2dp = (BluetoothA2dp) proxy;
                List<BluetoothDevice> a2dpConnectedDevices = btA2dp.getConnectedDevices();
                if (a2dpConnectedDevices.size() != 0) {
                    for (BluetoothDevice device : a2dpConnectedDevices) {
                        btname = device.getName();
                           status.setText("Connected to " +btname);


                    }
                }
//                if (!deviceConnected) {
//                    Toast.makeText(getActivity(), "DEVICE NOT CONNECTED", Toast.LENGTH_SHORT).show();
//                }
                myBluetooth.closeProfileProxy(BluetoothProfile.A2DP, btA2dp);
            }
        }

        public void onServiceDisconnected(int profile) {
            // TODO
        }
    };


    private boolean pairDevice(BluetoothDevice device) {
//        Boolean returnValue;
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            Boolean returnValue = (Boolean) method.invoke(device);
            return returnValue.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void CheckBlueToothState(){
        if (myBluetooth == null){
            status.setText("Bluetooth NOT support");
        }else{
            if (myBluetooth.isEnabled()){
                if(myBluetooth.isDiscovering()){
                    status.setText("Bluetooth is currently in device discovery process.");
                }else{
                    if(isBtConnected){
//                        status.setText("Connected to " +btname);
//                        filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
//                        registerReceiver(ActionFoundReceiver,filter1);
//                        myBluetooth.getProfileProxy(this, mProfileListener, BluetoothProfile.A2DP);
                        btnDis.setEnabled(true);
                        btnPaired.setEnabled(false);
                        status.setText("Connected");
                    }
                    else{
                        status.setText("Bluetooth is not connected");
                    }
//                    btnScanDevice.setEnabled(true);
                }
            }else{
                status.setText("Bluetooth is NOT Enabled!");
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, 1);
            }
        }
    }



    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                btArrayDevice.add(device);
                btArrayAdapter.notifyDataSetChanged();
                btArrayDevice.notifyDataSetChanged();
                scan.setEnabled(true);
            }
//            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.ACTION_ACL_CONNECTED);
//                btname = device.getName();//Device is now connected
//            }
        }
    };

    private void pairedDevicesList()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        pairedlist.setAdapter(adapter);
        pairedlist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            address = info.substring(info.length() - 17);
            btname = info.substring(0,info.length() - 18);

            new ConnectBT().execute();

        }
    };

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
                msg("Disconnected");
                status.setText("Disconnected");
                isBtConnected = false;
            }
            catch (IOException e)
            { msg("btSocket not closed");}
        }
//        finish(); //return to the first layout

    }

    private   void sendData(String s)
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(s.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    public void kirimKirim(View view){
        sendData(input.getText().toString());
        input.getText().clear();
    }

    private void getData()
    {
        if (btSocket!=null)
        {
            try
            {
//                if (btSocket.getInputStream().available();== ) {
//                    btSocket.getInputStream().av
//                }
                mmInputStream = btSocket.getInputStream();
                final Handler handler = new Handler();
                final byte delimiter = 10; //This is the ASCII code for a newline character

                stopWorker = false;
                readBufferPosition = 0;
                readBuffer = new byte[1024];
                workerThread = new Thread(new Runnable()
                {
                    public void run()
                    {
                        while(!Thread.currentThread().isInterrupted() && !stopWorker)
                        {
                            try
                            {
                                int bytesAvailable = mmInputStream.available();
                                if(bytesAvailable > 0)
                                {
                                    byte[] packetBytes = new byte[bytesAvailable];
                                    mmInputStream.read(packetBytes);
                                    for(int i=0;i<bytesAvailable;i++)
                                    {
                                        byte b = packetBytes[i];
                                        if(b == delimiter)
                                        {
                                            byte[] encodedBytes = new byte[readBufferPosition];
                                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                            final String data = new String(encodedBytes, "US-ASCII");
                                            readBufferPosition = 0;

                                            handler.post(new Runnable()
                                            {
                                                public void run()
                                                {
                                                    ambil = Double.parseDouble(data);
                                                    dataSerial.setText("data Serial :\n" + data);
                                                    Log.d("dataLangsung= ", ""+ambil);
                                                }
                                            });
                                        }
                                        else
                                        {
                                            readBuffer[readBufferPosition++] = b;
                                        }
                                    }
                                }
                            }
                            catch (IOException ex)
                            {
                                stopWorker = true;
                            }
                        }
                    }
                });

                workerThread.start();
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class PairingBT extends AsyncTask<Void, Void, Void>
    {
        BluetoothDevice bd;
        boolean flag;
        PairingBT(BluetoothDevice bt){
            this.bd = bt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    status.setText("Pairing...Please wait!!!");
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
//            flag = pairDevice(bd);
            try {
                Method method = bd.getClass().getMethod("createBond", (Class[]) null);
                method.invoke(bd, (Object[]) null);
//                Boolean returnValue = (Boolean) method.invoke(bd);
//                flag =  returnValue.booleanValue();
//                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (flag) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        status.setText("Pairing...Success!!!");
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        status.setText("Pairing...Failed!!!");
                    }
                });

            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
//            progress = ProgressDialog.show(Bluetooth.this, "Connecting...", "Please wait!!!");  //show a progress dialog
            status.setText("Connecting...Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Please Try again.");
                finish();
            }
            else
            {
                btnPaired.setEnabled(false);
                btnDis.setEnabled(true);
                msg("Connected.");
                status.setText("Connected to "+btname);
                isBtConnected = true;
//                getData();
            }
//            progress.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(ActionFoundReceiver);
//        moveTaskToBack(true);
    }
}