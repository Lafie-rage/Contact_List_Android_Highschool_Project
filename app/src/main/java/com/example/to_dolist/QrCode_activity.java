package com.example.to_dolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * Activity generating and showing QRCodes
 */
public class QrCode_activity extends AppCompatActivity {

    // Extras transmitted through intent
    private String qrStr;
    private long id;

    private ImageView backBtn_qrc;
    private ImageView qrCodeView;

    // QR Code default parameters
    public static int white = 0xFFFFFFFF;
    public static int blue = 0xFF5A8CFD;
    public final static int WIDTH = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_activity);

        // Retrieve contact id
        Intent intent = getIntent();
        qrStr = intent.getStringExtra("qrStr");
        id = intent.getLongExtra("id", 0);

        backBtn_qrc = findViewById(R.id.backBtn_qrc);
        qrCodeView = findViewById(R.id.qrCodeView);

        backBtn_qrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QrCode_activity.this, Profil.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        try {
            Bitmap bitmap = encodeAsBitmap(qrStr);
            qrCodeView.setImageBitmap(bitmap);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate the QRCode with the String provided. Return a Bitmap corresponding to the QRCode generated.
     * @param str Encoded String corresponding to a contact profile
     * @return Bitmap corresponding to the generated QRCode
     * @throws WriterException
     */
    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        Bitmap bitmap=null;
        try
        {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);

            int w = result.getWidth();
            int h = result.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = result.get(x, y) ? blue:white;
                }
            }
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);
        } catch (Exception iae) {
            iae.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /**
     * Convert contact data provided to a String.
     * Muse be used to transmit the String to encode as QRCode.
     * @param name name
     * @param surname surname
     * @param phone phone
     * @param mail mail address
     * @param address postal address
     * @return String corresponding to the data provided
     */
    public static String convertContactToString(String name, String surname, String phone, String mail, String address) {
        String qrStr =  "CONTACT:" +
                        "NAME:" + name +
                        "SURNAME:" + surname +
                        "PHONE:" + phone +
                        "MAIL:" + mail +
                        "ADDRESS:" + address;
        return qrStr;
    }

}
