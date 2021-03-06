/* THIS FILE NOT UNDER COPYRIGHT
 *
 * @author digithree
 */

package github.digithree.soundgap.ui.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import github.digithree.soundgap.App;
import github.digithree.soundgap.R;
import github.digithree.soundgap.ui.interfaces.IMainView;
import github.digithree.soundgap.ui.presenters.MainPresenter;

public class MainActivity extends AppCompatActivity implements IMainView {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private Button mBtnListen;
    private LinearLayout mLlMessages;
    private EditText mEtMessage;
    private Button mBtnSend;
    private TextView mTvStatus;

    private MainPresenter mMainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();

        mMainPresenter = new MainPresenter(this);

        mBtnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainPresenter.clickListen();
            }
        });

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainPresenter.clickSendMessage();
            }
        });

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M
                || checkForAccessPermissions(this)) {
            mMainPresenter.setRecordPermission(true);
        } else {
            mMainPresenter.setRecordPermission(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, item.toString());
        switch (item.getItemId()) {
            case R.id.action_clear:
                mMainPresenter.clickPeakListClear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void bindViews() {
        mBtnListen  = (Button) findViewById(R.id.btn_listen);
        mLlMessages = (LinearLayout) findViewById(R.id.ll_messages);
        mEtMessage = (EditText) findViewById(R.id.edit_text_message);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mTvStatus = (TextView) findViewById(R.id.text_view_status);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMainPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMainPresenter.onPause();
    }


    // IMainView

    @Override
    public void setStatusText(String text) {
        mTvStatus.setText(String.format("%s%s", getString(R.string.status_prefix), text));
    }

    @Override
    public void showListeningForMessages() {
        setStatusText(getString(R.string.listen_for_message_in_progress));
        mBtnListen.setText(R.string.btn_stop_listen);
    }

    @Override
    public void showStopListeningForMessages() {
        setStatusText(getString(R.string.listen_for_message_stopped));
        mBtnListen.setText(R.string.btn_listen);
    }

    @Override
    public void showListeningForMessagesError() {
        setStatusText(getString(R.string.listen_for_message_failure));
        mBtnListen.setText(R.string.btn_listen);
    }

    @Override
    public void addNewMessage(String text) {
        TextView textView = (TextView) LayoutInflater.from(mLlMessages.getContext())
                .inflate(R.layout.view_freq_item, mLlMessages, false);
        textView.setText(text);
        mLlMessages.addView(textView);
    }

    @Override
    public void clearMessages() {
        mLlMessages.removeAllViews();
    }

    @Override
    public String getMessageToSend() {
        return mEtMessage.getText().toString();
    }

    @Override
    public void showSendMessageInProgress() {
        setStatusText(getString(R.string.send_message_in_progress));
        mBtnSend.setEnabled(false);
    }

    @Override
    public void showSendMessageSuccess() {
        setStatusText(getString(R.string.send_message_success));
        mBtnSend.setEnabled(true);
    }

    @Override
    public void showSendMessageError() {
        setStatusText(getString(R.string.send_message_failure));
        mBtnSend.setEnabled(true);
    }

    // Permissions handling for Marshmallow +

    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkForAccessPermissions(Context context) {
        int permission = App.getStaticInstance().checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ((AppCompatActivity) context).requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_ASK_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                    Toast.makeText(App.getStaticInstance(), "Record permission granted", Toast.LENGTH_SHORT).show();
                    mMainPresenter.setRecordPermission(true);
                } else {
                    // denied
                    Toast.makeText(App.getStaticInstance(), "Record permission must be granted for app to work", Toast.LENGTH_SHORT).show();
                    mMainPresenter.setRecordPermission(false);
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
