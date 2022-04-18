package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import in.hng.mpos.R;
import in.hng.mpos.helper.BluetoothUtil;
import in.hng.mpos.helper.ESCUtil;
import in.hng.mpos.helper.Log;
import in.hng.mpos.helper.SunmiPrintHelper;

public abstract class BaseActivity extends AppCompatActivity {

    Handler handler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        initPrinterStyle();
    }

    public void setActionBarTitle(String message) {
        // Set up your ActionBar

        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.action_bar_simple, null);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout);
            actionBar.setTitle("");
            final int actionBarColor = getResources().getColor(R.color.ActionBarColor);
            actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
        }
        TextView title = findViewById(R.id.ab_activity_title);
        title.setText(message);
        title.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#3c3c3c"));
        }
    }

    public void showVolleyError(String TAG, VolleyError error) {
        try {

            if (error instanceof TimeoutError) {
                showAlertMessage(TAG, "Time out error occurred.Please click on OK and try again");
                Log.e(TAG, "Time out error occurred.");

            } else if (error instanceof NoConnectionError) {
                showAlertMessage(TAG, "Network error occurred.Please click on OK and try again");
                Log.e(TAG, "Network error occurred.");

            } else if (error instanceof AuthFailureError) {
                showAlertMessage(TAG, "Authentication error occurred.Please click on OK and try again");
                Log.e(TAG, "Authentication error occurred.");

            } else if (error instanceof ServerError) {
                showAlertMessage(TAG, "Server error occurred.Please click on OK and try again");
                Log.e(TAG, "Server error occurred.");
            } else if (error instanceof NetworkError) {
                showAlertMessage(TAG, "Network error occurred.Please click on OK and try again");
                Log.e(TAG, "Network error occurred.");

            } else if (error instanceof ParseError) {
                showAlertMessage(TAG, "An error occurred.Please click on OK and try again");
                Log.e(TAG, "An error occurred.");
            } else {

                showAlertMessage(TAG, "An error occurred.Please click on OK and try again");
                Log.e(TAG, "An error occurred.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Volley Error Excetion <<::>> " + error);
        }
    }

    public void showAlertMessage(String TAG, String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, message);
                AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
                builder.setTitle("HnG POS");
                builder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                //on click event
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public void showFailedAlert(final String title, final String msg) {

        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
                builder.setTitle(title);
                builder.setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //on click event
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }

    /**
     *  Initialize the printer
     *  All style settings will be restored to default
     */
    private void initPrinterStyle() {
        if(BluetoothUtil.isBlueToothPrinter){
            BluetoothUtil.sendData(ESCUtil.init_printer());
        }else{
            SunmiPrintHelper.getInstance().initPrinter();
        }
    }

    public void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
