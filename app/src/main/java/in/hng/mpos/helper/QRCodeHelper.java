package in.hng.mpos.helper;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.IntRange;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class QRCodeHelper {

    private static QRCodeHelper qrCodeHelper = null;
    private ErrorCorrectionLevel mErrorCorrectionLevel;
    private int mMargin;
    private String mContent;
    private int mWidth, mHeight;
    /**
     * private constructor of this class only access by stying in this class.
     */
    private QRCodeHelper(Context context) {
        mHeight = (int) (context.getResources().getDisplayMetrics().heightPixels / 2.4);
        mWidth = (int) (context.getResources().getDisplayMetrics().widthPixels / 1.3);
        Log.e("Dimension = %s", mHeight + "");
        Log.e("Dimension = %s", mWidth + "");
    }
    /**
     * This method is for singleton instance od this class.
     *
     * @return the QrCode instance.
     */
    public static QRCodeHelper newInstance(Context context) {
        if (qrCodeHelper == null) {
            qrCodeHelper = new QRCodeHelper(context);
        }
        return qrCodeHelper;
    }
    /**
     * This method is called generate function who generate the qrcode and return it.
     *
     * @return qrcode image with encrypted user in it.
     */
    public Bitmap getQRCOde() {
        return generate();
    }
    /**
     * Simply setting the correctionLevel to qrcode.
     *
     * @param level ErrorCorrectionLevel for Qrcode.
     * @return the instance of QrCode helper class for to use remaining function in class.
     */
    public QRCodeHelper setErrorCorrectionLevel(ErrorCorrectionLevel level) {
        mErrorCorrectionLevel = level;
        return this;
    }
    /**
     * Simply setting the encrypted to qrcode.
     *
     * @param content encrypted content for to store in qrcode.
     * @return the instance of QrCode helper class for to use remaining function in class.
     */
    public QRCodeHelper setContent(String content) {
        mContent = content;
        return this;
    }
    /**
     * Simply setting the width and height for qrcode.
     *
     * @param width  for qrcode it needs to greater than 1.
     * @param height for qrcode it needs to greater than 1.
     * @return the instance of QrCode helper class for to use remaining function in class.
     */
    public QRCodeHelper setWidthAndHeight(@IntRange(from = 1) int width, @IntRange(from = 1) int height) {
        mWidth = width;
        mHeight = height;
        return this;
    }
    /**
     * Simply setting the margin for qrcode.
     *
     * @param margin for qrcode spaces.
     * @return the instance of QrCode helper class for to use remaining function in class.
     */
    public QRCodeHelper setMargin(@IntRange(from = 0) int margin) {
        mMargin = margin;
        return this;
    }
    /**
     * Generate the qrcode with giving the properties.
     *
     * @return the qrcode image.
     */
    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;

    private Bitmap generate() {
        Map<EncodeHintType, Object> hintsMap = new HashMap<>();
        hintsMap.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hintsMap.put(EncodeHintType.ERROR_CORRECTION, mErrorCorrectionLevel);
        hintsMap.put(EncodeHintType.MARGIN, mMargin);
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(mContent, BarcodeFormat.QR_CODE, mWidth, mHeight, hintsMap);
            int[] pixels = new int[mWidth * mHeight];
            for (int y = 0; y < mHeight; y++) {
                int offset = y * mWidth;
                for (int x = 0; x < mWidth; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : WHITE;
                }
            }

            /*int[] pixels = new int[mWidth * mHeight];
            for (int i = 0; i < mHeight; i++) {
                for (int j = 0; j < mWidth; j++) {
                    if (bitMatrix.get(j, i)) {
                        pixels[i * mWidth + j] = 0xFFFFFFFF;
                    } else {
                        pixels[i * mWidth + j] = 0x282946;
                    }
                }
            }*/
            Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels, 0, mWidth, 0, 0, mWidth, mHeight);

            return bitmap;
            //return Bitmap.createBitmap(pixels, mWidth, mHeight, Bitmap.Config.ARGB_8888);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

}
