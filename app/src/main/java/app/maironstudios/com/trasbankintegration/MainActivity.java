package app.maironstudios.com.trasbankintegration;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static posintegrado.ingenico.com.mposintegrado.mposLib.hexStringToByteArray;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ingenico.pclservice.PclService;
import com.ingenico.pclutilities.PclUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cl.transbank.pos.POS;
import cl.transbank.pos.exceptions.TransbankPortNotConfiguredException;
import cl.transbank.pos.responses.SaleResponse;
import posintegrado.ingenico.com.mposintegrado.mposLib;

public class MainActivity extends CommonActivity {
    /*Variable de control si ya se encontró un dispositivo*/
    boolean bFound = false;

    private ProgressBar progressBar;
    private Button retryButton;
    private TextView infoText;
    private VtexData vtexData;
    private PclUtilities mPclUtil;
    mposLib posLib;
    private String selectedDevice;
    private boolean isConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        retryButton = findViewById(R.id.button_retry);
        infoText = findViewById(R.id.infoText);

        retryButton.setOnClickListener(view -> {
            appendLog("Payment", "Retry");
            toogleRetry(false);
            sendSale();
        });

        appendLog("Service", "initService");
        initService();
        startPclService();

        mPclUtil = new PclUtilities(this, getPackageName(), "pairing_addr.txt");

        /*Se obtiene con esto la lista de los dipositivos ingenico paired con el
        terminal*/
        Set<PclUtilities.BluetoothCompanion> btComps = mPclUtil.GetPairedCompanions();

        ArrayList<PosBluetooth> btDevices = this.getDevices();


        if (!btDevices.isEmpty()) {
            // Loop through paired devices
            for (PosBluetooth device : btDevices) {
                Log.d(TAG, device.getAddress() + " - " + device.getName());

                if (device.isActivated() && isConnected) {
                    selectedDevice = device.getAddress();
                    connectDevice(selectedDevice);
                    return;
                }
            }

            selectedDevice = btDevices.get(0).getAddress();
            return;
        }

        this.makeToast(R.string.no_paired_device);
    }


        if (btComps != null && (btComps.size() > 0)) {
            /* Loop through paired devices*/
            for (PclUtilities.BluetoothCompanion comp : btComps) {
                /*Aca se revisa si el dipositivo esta activo y lo define como el actual*/
                if (comp.isActivated()) {
                    bFound = true;

                    mCurrentDevice = comp.getBluetoothDevice().getAddress() + " - " +
                            comp.getBluetoothDevice().getName();
                    appendLog("Device", "Found device -> "+mCurrentDevice);
                }
                else {
                    /*Se activa el dispositivo*/
                    mPclUtil.ActivateCompanion(comp.getBluetoothDevice().getAddress());
                    appendLog("Device", "Activate Companion");
                    return;
                }
            }
        }

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();

            String host = data.getHost();
            if (host.equals("payment") || host.equals("payment-reversal")) {
                appendLog("Payment", "Recieve " + host);
                getParamsFromIntent(data);
            } else if (host.equals(getString(R.string.host))) {
                Log.e("MAS","recieved callback");
            }
        }
        sendSale();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        appendLog("Service", "releaseService");
        stopPclService();
        releaseService();
        super.onDestroy();
    }

    private void startPclService()
    {
        appendLog("Service", "startPclService");
        if (!mServiceStarted)
        {
            SharedPreferences settings = getSharedPreferences("PCLSERVICE", MODE_PRIVATE);
            boolean enableLog = settings.getBoolean("ENABLE_LOG", true);
            Intent i = new Intent(this, PclService.class);
            i.putExtra("PACKAGE_NAME", getPackageName());
            i.putExtra("FILE_NAME", "pairing_addr.txt");
            i.putExtra("ENABLE_LOG", enableLog);
            if (getApplicationContext().startService(i) != null) mServiceStarted = true;

            Log.d("Service","startPclService - started: "+mServiceStarted);
            appendLog("Service", "startPclService - started: "+mServiceStarted);
        }
    }

    private void stopPclService()
    {
        appendLog("Service", "stopPclService");
        if (mServiceStarted)
        {
            Intent i = new Intent(this, PclService.class);
            if (getApplicationContext().stopService(i))
                mServiceStarted = false;
        }
    }

    @Override
    void onStateChanged(String state) {
        //
    }

    @Override
    void onPclServiceConnected() {
        //
        Log.i("MAS", "PclServiceConnected");

        infoText.append("Pcl Service connected\n");

        toogleRetry(true);
    }

    protected void getParamsFromIntent(Uri data) {
        Log.i("MAS","ReceiveVTEX.getParamsFromIntent();");

        showProgress(true);

        String action = null, queryString = null;

        vtexData = new VtexData();

        try {
            Log.i("ReceiveVTEX","try map data");
            action = data.getHost();
            queryString = data.getQuery();

            Log.i("ReceiveVTEX", "uri: "+data);
            Log.i("ReceiveVTEX", "query: "+queryString);

            JSONObject json = new JSONObject();
            json.put("recieveVTEX", queryString);

            if(data.getQueryParameter("action") != null && !data.getQueryParameter("action").isEmpty()) {
                action = data.getQueryParameter("action");
                vtexData.setAction(data.getQueryParameter("action"));
            }
            if(data.getQueryParameter("scheme") != null && !data.getQueryParameter("scheme").isEmpty())
                vtexData.setScheme(data.getQueryParameter("scheme"));

            if(data.getQueryParameter("paymentId") != null && !data.getQueryParameter("paymentId").isEmpty())
                vtexData.setPaymentId(data.getQueryParameter("paymentId"));
            if(data.getQueryParameter("paymentSystemName") != null && !data.getQueryParameter("paymentSystemName").isEmpty())
                vtexData.setPaymentDescription(data.getQueryParameter("paymentSystemName"));
            if(data.getQueryParameter("amount") != null && !data.getQueryParameter("amount").isEmpty())
                vtexData.setAmount(Double.parseDouble(data.getQueryParameter("amount")));
            if(data.getQueryParameter("paymentType") != null && !data.getQueryParameter("paymentType").isEmpty())
                vtexData.setPaymentType(data.getQueryParameter("paymentType"));
            if(data.getQueryParameter("installments") != null && !data.getQueryParameter("installments").isEmpty())
                vtexData.setInstallments(Integer.parseInt(data.getQueryParameter("installments")));

            if(data.getQueryParameter("payerIdentification") != null && !data.getQueryParameter("payerIdentification").isEmpty())
                vtexData.setPayerIdentification(Long.parseLong(data.getQueryParameter("payerIdentification")));
            if(data.getQueryParameter("payerEmail") != null && !data.getQueryParameter("payerEmail").isEmpty())
                vtexData.setPayerEmail(data.getQueryParameter("payerEmail"));

            if(data.getQueryParameter("acquirerId") != null && !data.getQueryParameter("acquirerId").isEmpty()) {
                vtexData.setAcquirerId(data.getQueryParameter("acquirerId"));
                vtexData.setTarget(vtexData.getAcquirerId().split("-")[2]);
            }
            if(data.getQueryParameter("acquirerSecret") != null && !data.getQueryParameter("acquirerSecret").isEmpty())
                vtexData.setAcquirerSecret(data.getQueryParameter("acquirerSecret"));
            if(data.getQueryParameter("acquirerFee") != null && !data.getQueryParameter("acquirerFee").isEmpty())
                vtexData.setAcquirerFee(Double.parseDouble(data.getQueryParameter("acquirerFee")));
            if(data.getQueryParameter("accessToken") != null && !data.getQueryParameter("accessToken").isEmpty())
                vtexData.setAquirerAccessToken(data.getQueryParameter("accessToken"));
            if(data.getQueryParameter("sellerName") != null && !data.getQueryParameter("sellerName").isEmpty())
                vtexData.setAccountName(data.getQueryParameter("sellerName"));
            if(data.getQueryParameter("storeCurrency") != null && !data.getQueryParameter("storeCurrency").isEmpty())
                vtexData.setStoreCurrency(data.getQueryParameter("storeCurrency"));

            if(data.getQueryParameter("urlCallback") != null && !data.getQueryParameter("urlCallback").isEmpty())
                vtexData.setUrlCallBack(data.getQueryParameter("urlCallback"));
            if(data.getQueryParameter("transactionId") != null && !data.getQueryParameter("transactionId").isEmpty())
                vtexData.setTransactionId(data.getQueryParameter("transactionId"));
        } catch (NullPointerException e) {
            Log.i("MAS","query nullpointer || "+e.getMessage());
            e.printStackTrace();
            action = "";
            queryString = "";

            showErrorMessage(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();

            showErrorMessage(e.getMessage());
        }

        appendLog("Payment", "DATA: "+vtexData.toString());

        if (action != null && (action.equals("payment") || action.equals("payment-reversal"))) {
            Log.i("MAS","query: "+queryString);
            Log.i("MAS","vtexData: "+vtexData.toString());
            infoText.append("Payment\n"+vtexData.getPaymentId()+"\nrecieved\n\n");
        } else {
            showProgress(false);

            Log.i("MAS", "getParamsFromIntent no action");
            String msg = "Action not recognized -/ " +action;
            showErrorMessage(msg);

            Log.i("MAS","ERROR - "+msg);
        }
    }

    private void sendSale() {
        posLib = new mposLib(mPclService);
        appendLog("Sale", "Send sale");
        if (vtexData == null || vtexData.getAmount() <= 0) {
            Log.i("MAS", "sendSale");
            showProgress(false);

            showErrorMessage("No sale data found");
            toogleRetry(true);

            return;
        }

        int amount = (int) vtexData.getAmount();
        String tkt = vtexData.getPaymentId();

        Log.i("MAS", "mCurrentDevice -> "+mCurrentDevice);
        Log.i("MAS", "isCompanionConnected? -> "+isCompanionConnected());

        String stx = "02";
        String ext = "03";

        String mensajeriaTrx = "0200|"+amount+"|"+tkt+"|||0";
        /*Convierto mi string de trx a hex*/
        String trxToHex = posLib.convertStringToHex(mensajeriaTrx);
        /*Luego calculo el largo de mi trama en hex (LRC)*/
        String obtenerLrc = calcularLRC(trxToHex);
        /*Ahora armo el comando completo de trx*/
        String trxCompleta = stx+trxToHex+ext+ obtenerLrc;
        /*Envio el comando completo para que el POS integrado bluetooth lo procese*/
        appendLog("Sale", "Start transaction");
        posLib.startTransaction(trxCompleta);

        posLib.setOnTransactionFinishedListener(new mposLib.onTransactionFinishedListener() {
            public void onFinish(String response) {
                /*EJEMPLO DE RESPONSE*/
                String respToString = posLib.convertHexToString(response);
                Log.i("Respuesta response: ", respToString);
                showProgress(false);

                /*EJEMPLO DE RESPONSE*/
                Log.i("MAS", "Respuesta hexResponse: "+ response);
                Log.i("MAS", "Respuesta stringResponse: "+ respToString);
                appendLog("Sale", "Transaction finished with response '"+response+"'");

                if (response.equals("Aprobado")) {
                    respondWithSuccess(vtexData.getTransactionId());
                } else
                    respondWithFail(response);;
            }
        });

    }

    protected void respondWithSuccess(String tid) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(vtexData.getScheme())
                .authority(vtexData.getAction())
                .appendQueryParameter("responsecode", "0")
                .appendQueryParameter("acquirerName", "transbankpos")
                .appendQueryParameter("paymentId", vtexData.getPaymentId())
                .appendQueryParameter("tid", tid);
        String responseUrl = builder.build().toString();

        Log.i("SendVTEX", responseUrl);

        openURL(responseUrl);
    }

    protected void respondWithFail(String status) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(vtexData.getScheme())
                .authority(vtexData.getAction())
                .appendQueryParameter("responsecode", "110")
                .appendQueryParameter("reason", status)
                .appendQueryParameter("acquirerName", "transbankpos")
                .appendQueryParameter("paymentId", vtexData.getPaymentId());
        String responseUrl = builder.build().toString();

        Log.i("SendVTEX", responseUrl);

        openURL(responseUrl);
    }

    protected void openURL(String url) {
        if (url == null || url.isEmpty())
            return;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            CharSequence error = "Could not open URL '" + url + "': " + e.getMessage();
            Toast toast = Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private String calcularLRC(String input){
        String LRC = "";
        int uintVal = 0;
        if(!input.equals("")){
            byte[] arrayofhex = hexStringToByteArray(input);
            for(int count = 1; count < arrayofhex.length; ++count){
                if(count == 1){
                    uintVal = arrayofhex[count - 1] ^ arrayofhex[count];
                }else{
                    uintVal ^= arrayofhex[count];
                }
            }
        }
        LRC = Integer.toHexString(uintVal).toUpperCase();
        int f = LRC.length();
        if(f == 2){
            return LRC;
        }else {
            char[] chars = LRC.toCharArray();
            StringBuilder hex = new StringBuilder();
            for (char aChar : chars) {
                hex.append(Integer.toHexString(aChar));
            }
            return hex.toString();
        }
    }

    private void toogleRetry(boolean value) {
        retryButton.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void showProgress(boolean value) {
        progressBar.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void showErrorMessage(String mError) {
        appendLog("ERROR", mError);

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("ERROR");
        alertDialog.setMessage(mError);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public void appendLog(String tag, String text) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss-SSS", Locale.ENGLISH);
        Log.i("MAS", "tag: "+tag+" // text: "+text);
        String filesDirs = ContextCompat.getExternalFilesDirs(this, null)[0].toString();

        File logFile = new File(filesDirs, "/log-file.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Log.i("MAS-", "file exists - catch \n"+e.getMessage());
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(sdf.format(new Date())).append(": ").append(tag).append(" -> ").append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.i("MAS-", "write file - catch \n"+e.getMessage());
            e.printStackTrace();
        }
    }

    public void toggleConnection(View view) {
        if(!isConnected){
            connectDevice(selectedDevice);
            return;
        }

        disconnectDevice();
    }

    private void connectDevice(String deviceAddress) {
        textViewStatus.setText(R.string.connecting);
        textViewStatus.setTextColor(ContextCompat.getColor(this, R.color.connecting));
        mPclUtil.ActivateCompanion(deviceAddress);
        startPclService();
        initService();
    }

    private void disconnectDevice() {
        releaseService();
        stopPclService();
    }


}