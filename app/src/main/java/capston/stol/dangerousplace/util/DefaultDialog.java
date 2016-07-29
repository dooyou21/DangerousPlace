package capston.stol.dangerousplace.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import capston.stol.dangerousplace.R;

/**
 * Created by sjlee on 2016-07-20.
 */
public class DefaultDialog extends Dialog {
    private View.OnClickListener mLeftClickListener, mRightClickListener;
    private Button btnLeft, btnRight;
    private TextView tvTitle, tvContents;
    private String mTitle, mContents, left, right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_default);

        tvTitle = (TextView) findViewById(R.id.tvDialogTitle);
        tvContents = (TextView) findViewById(R.id.tvDialogContents);
        btnLeft = (Button) findViewById(R.id.btn_left);
        btnRight = (Button) findViewById(R.id.btn_right);

        tvTitle.setText(mTitle);
        tvContents.setText(mContents);
        btnLeft.setText(left);
        btnRight.setText(right);

        if(mLeftClickListener != null && mRightClickListener != null){
            btnLeft.setOnClickListener(mLeftClickListener);
            btnRight.setOnClickListener(mRightClickListener);
        }else{
            btnRight.setVisibility(View.GONE);
            if(mLeftClickListener != null) btnLeft.setOnClickListener(mLeftClickListener);
            else if(mRightClickListener != null) btnLeft.setOnClickListener(mRightClickListener);
            else btnLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancel();
                    }
                });
        }


    }


    public DefaultDialog(Context context, String title, String contents,
                          View.OnClickListener leftListener, View.OnClickListener rightListener,
                         String left, String right) {
        super(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        this.mLeftClickListener = leftListener;
        this.mRightClickListener = rightListener;
        mTitle = title;
        mContents = contents;
        this.left = left;
        this.right = right;
    }
}
