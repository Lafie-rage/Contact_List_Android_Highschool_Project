package com.example.to_dolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Class used to scan QRCode.
 * Using https://github.com/dm77/barcodescanner/tree/f8218eb9d7066fff7f4862bb27b3b3a6da7d3e42 code samples.
 */
public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent intent = new Intent(SimpleScannerActivity.this, Profil.class);
        intent.putExtra("action", "qrCode");
        intent.putExtra("qrStr", rawResult.getText());
        startActivity(intent);
        return;
    }
}
