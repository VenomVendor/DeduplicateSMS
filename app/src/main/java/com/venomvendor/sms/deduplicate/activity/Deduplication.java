/*
 *   Copyright (C) 2015 VenomVendor <info@VenomVendor.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.venomvendor.sms.deduplicate.activity;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.venomvendor.sms.deduplicate.BuildConfig;
import com.venomvendor.sms.deduplicate.R;
import com.venomvendor.sms.deduplicate.data.FindDuplicates;
import com.venomvendor.sms.deduplicate.service.DeleteSMSAsync;
import com.venomvendor.sms.deduplicate.util.Constants;
import com.venomvendor.sms.deduplicate.util.Utils;

import java.util.ArrayList;

import static com.venomvendor.sms.deduplicate.util.Utils.isValidMessageApp;

/*
 * Created by VenomVendor on 11/8/15.
 */
public class Deduplication extends Activity implements View.OnClickListener {

    private static final int APP_CHANGE_REQUEST = 253;
    private static final int UPDATE_PERMISSIONS = 254;
    private static final int RUNTIME_PERMISSIONS_CODE = 255;
    private static final String[] REQUIRED_PERMISSIONS = {permission.READ_SMS};
    private static final int DEFAULT_DEL = BuildConfig.DEBUG ? 2 : 50;
    private String mFormat;
    private int mTotalMessages;

    private DeleteSMSAsync mSmsDeleter;

    private LinearLayout mProgressBarHolder;
    private ProgressBar mProgressBar;

    private Button mDeDuplicate;
    private Button mCancel;
    private Button mRevert;

    private TextView mDeleted;
    private TextView mRevertMessage;

    private EditText mDeleteBy;

    private CheckedTextView mIgnoreTimestamp;
    private LinearLayout mIgnoreMessage;
    private RadioButton mKeepFirst;

    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deduplication);

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (mPref.getBoolean(Constants.SHOW_EULA, true)) {
            showEula();
        } else {
            initViews();
        }
    }

    private void showEula() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.eula))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            SharedPreferences.Editor editor = mPref.edit();
                            editor.putBoolean(Constants.SHOW_EULA, false);
                            editor.commit();
                            dialog.dismiss();
                            initViews();
                        })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> finish());
        builder.create().show();
    }

    private void initViews() {
        mPref = null;

        mDeDuplicate = findViewById(R.id.deduplicate);
        mCancel = findViewById(R.id.cancel);
        mRevert = findViewById(R.id.revert);
        mDeleted = findViewById(R.id.current_progress);
        mRevertMessage = findViewById(R.id.revert_message);
        mProgressBarHolder = findViewById(R.id.progress_bar_holder);
        mProgressBar = findViewById(R.id.progress_bar);
        mIgnoreTimestamp = findViewById(R.id.ignore_timestamp);
        mIgnoreMessage = findViewById(R.id.ignore_timestamp_message);
        mKeepFirst = findViewById(R.id.keep_first);
        mDeleteBy = findViewById(R.id.per_iteration);

        initListeners();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initListeners() {
        findViewById(R.id.more_apps).setOnClickListener(this);
        mDeDuplicate.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mRevert.setOnClickListener(this);
        mIgnoreTimestamp.setOnClickListener(this);

        if (!isValidMessageApp(this)) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext());
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(Constants.CURRENT_SMS_APP,
                    Sms.getDefaultSmsPackage(getApplicationContext()));
            editor.apply();
            mRevert.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mRevertMessage.setText(Html.fromHtml(getString(R.string.security_reasons)));
        } else {
            mRevertMessage.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        if (checkSelfPermission(REQUIRED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0])) {
                //Educate User
                showCustomDialog(getString(R.string.required_permissions),
                        getString(R.string.m_permission_inform),
                        (dialog, which) -> {
                            dialog.dismiss();
                            //ask permissions.
                            requestPermissions(REQUIRED_PERMISSIONS, RUNTIME_PERMISSIONS_CODE);
                        }, (dialog, which) -> {
                            dialog.dismiss();
                            deadLock();
                        }
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case RUNTIME_PERMISSIONS_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permissionsDenied();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void permissionsDenied() {
        showCustomDialog(getString(R.string.required_permissions),
                String.format(getString(R.string.permission_explain), getString(R.string.app_name)),
                (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, UPDATE_PERMISSIONS);
                }, null);
    }

    private void showCustomDialog(String title, String msg,
                                  DialogInterface.OnClickListener listener,
                                  DialogInterface.OnClickListener cancelListener) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(android.R.string.ok, listener);
        if (cancelListener != null) {
            builder.setNegativeButton(android.R.string.cancel, cancelListener);
        }
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deadLock() {
        showCustomDialog(getString(R.string.permissions_revoked), String.format("%s\n%s %s",
                getString(R.string.app_cannot_run), getString(R.string.app_will_exit),
                getString(android.R.string.ok)),
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }, null
        );
    }

    private void showHideWarning() {
        mKeepFirst.setChecked(true);
        mIgnoreTimestamp.setChecked(!mIgnoreTimestamp.isChecked());
        mIgnoreMessage.setVisibility(mIgnoreTimestamp.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void doCancel() {
        mSmsDeleter.doCancel();

        mCancel.setEnabled(false);
        mCancel.setClickable(false);
        mCancel.setText(R.string.cancelling);
        mDeDuplicate.setVisibility(View.GONE);
        mProgressBarHolder.setVisibility(View.VISIBLE);
    }

    private void cancelDeletion() {
        mCancel.setEnabled(true);
        mCancel.setClickable(true);
        mCancel.setText(android.R.string.cancel);
        mDeDuplicate.setVisibility(View.VISIBLE);
        mProgressBarHolder.setVisibility(View.GONE);
        revertApp();
    }

    private void revertApp() {
        new Handler().postDelayed(() -> Utils.revertOldApp(getApplicationContext()), 3000);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void deduplicate() {
        mProgressBar.setMax(0);
        mProgressBar.setProgress(0);

        mDeleted.setText(String.format(getResources()
                .getQuantityString(R.plurals.deleted_messages, 0), 0, 0));
        mProgressBarHolder.setVisibility(View.GONE);
        if (!isValidMessageApp(this)) {
            Intent setApp = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            setApp.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivityForResult(setApp, APP_CHANGE_REQUEST);
        } else {
            showWarning();
        }
    }

    private void showWarning() {
        AlertDialog.Builder warningDialog = new AlertDialog.Builder(this);
        warningDialog.setMessage(R.string.warning_message);
        warningDialog.setPositiveButton(android.R.string.ok, (dialog, id) -> findDuplicates());

        warningDialog.setNegativeButton(android.R.string.cancel,
                (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = warningDialog.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void findDuplicates() {
        FindDuplicates findDuplicates = new FindDuplicates(this,
                mIgnoreTimestamp.isChecked(),
                mKeepFirst.isChecked());
        findDuplicates.setOnDuplicatesFoundListener(duplicateIds -> {
            if (duplicateIds.isEmpty()) {
                showToast(getString(R.string.no_duplicates));
                revertApp();
            } else {
                showConfirmation(duplicateIds);
            }
        });
        findDuplicates.execute();
    }

    private void showConfirmation(ArrayList<String> duplicateIds) {
        mTotalMessages = duplicateIds.size();
        mFormat = getResources().getQuantityString(R.plurals.deleted_messages, mTotalMessages);

        AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(this);
        confirmationDialog.setCancelable(false);
        confirmationDialog.setMessage(getResources()
                .getQuantityString(R.plurals.delete_duplicates, mTotalMessages, mTotalMessages));

        confirmationDialog.setPositiveButton(getString(android.R.string.ok),
                (dialog, which) -> {
                    dialog.dismiss();
                    mDeDuplicate.setVisibility(View.GONE);
                    mProgressBarHolder.setVisibility(View.VISIBLE);
                    startDeleteService(duplicateIds);
                });
        confirmationDialog.setNegativeButton(getString(android.R.string.cancel),
                (dialog, which) -> dialog.dismiss());
        confirmationDialog.show();
    }

    private void startDeleteService(ArrayList<String> duplicateIds) {
        mProgressBar.setMax(mTotalMessages);

        int deleteBy = getPerBatch();
        mSmsDeleter = new DeleteSMSAsync(this, duplicateIds, deleteBy,
                this::updateDeletedItems);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mSmsDeleter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mSmsDeleter.execute();
        }
    }

    private int getPerBatch() {
        String del = mDeleteBy.getText().toString().trim();
        del = TextUtils.isEmpty(del) ? "" + DEFAULT_DEL : del;
        int deleteBy = Math.abs(Integer.valueOf(del));
        deleteBy = Math.max(1, deleteBy);
        return Math.min(deleteBy, 100);
    }

    private void updateDeletedItems(int deletedMsgs, boolean interrupted) {
        runOnUiThread(() -> {
            mDeleted.setText(String.format(mFormat, deletedMsgs, mTotalMessages));
            if (interrupted) {
                showToast(String.format(mFormat, deletedMsgs, mTotalMessages));
                cancelDeletion();
            }
        });
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == APP_CHANGE_REQUEST) {
            if (resultCode == RESULT_OK || isValidMessageApp(this)) {
                mRevert.setVisibility(View.VISIBLE);
                mRevertMessage.setVisibility(View.GONE);
                showWarning();
            } else {
                mRevert.setVisibility(View.GONE);
                mRevertMessage.setVisibility(View.VISIBLE);
                showCustomDialog(getString(R.string.failed), getString(R.string.failure_message),
                        (dialog, which) -> dialog.dismiss(), null);
            }
        } else if (requestCode == UPDATE_PERMISSIONS) {
            if (checkSelfPermission(REQUIRED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED) {
                deadLock();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deduplicate:
                deduplicate();
                break;

            case R.id.more_apps:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://search?q=pub:VenomVendor"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case R.id.cancel:
                doCancel();
                break;

            case R.id.revert:
                Utils.revertOldApp(getApplicationContext());
                break;

            case R.id.ignore_timestamp:
                showHideWarning();
                break;
        }
    }
}
