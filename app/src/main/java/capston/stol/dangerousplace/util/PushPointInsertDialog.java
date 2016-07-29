package capston.stol.dangerousplace.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import capston.stol.dangerousplace.R;

/**
 * Created by sjlee on 2016-07-18.
 */
public class PushPointInsertDialog extends Dialog {

    private TextView mTitleView;
    private EditText etPointTitle;
    private Button mLeftButton;
    private Button mRightButton;

    private View.OnClickListener mLeftClickListener;
    private View.OnClickListener mRightClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_push_point_insert);

        mTitleView = (TextView) findViewById(R.id.txt_title);
        etPointTitle = (EditText) findViewById(R.id.etPointTitle);
        mLeftButton = (Button) findViewById(R.id.btn_left);
        mRightButton = (Button) findViewById(R.id.btn_right);

        // 클릭 이벤트 셋팅
        if (mLeftClickListener != null && mRightClickListener != null) {
            mLeftButton.setOnClickListener(mLeftClickListener);
            mRightButton.setOnClickListener(mRightClickListener);
        } else if (mLeftClickListener != null
                && mRightClickListener == null) {
            mLeftButton.setOnClickListener(mLeftClickListener);
        } else {

        }
    }

    public PushPointInsertDialog(Context context, View.OnClickListener leftListener,
                        View.OnClickListener rightListener) {
        super(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        this.mLeftClickListener = leftListener;
        this.mRightClickListener = rightListener;
    }

    public String getPointTitle() {
        return etPointTitle.getText().toString();
    }
}