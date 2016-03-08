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
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.venomvendor.sms.deduplicate.R;
import com.venomvendor.sms.deduplicate.data.FindDuplicates;
import com.venomvendor.sms.deduplicate.data.FindDuplicates.OnDuplicatesFoundListener;
import com.venomvendor.sms.deduplicate.service.DeleteSmsService;
import com.venomvendor.sms.deduplicate.util.Constants;
import com.venomvendor.sms.deduplicate.util.Utils;

import java.util.ArrayList;

import static com.venomvendor.sms.deduplicate.util.Utils.isValidMessageApp;

/*
 * Created by VenomVendor on 11/8/15.
 */
@SuppressWarnings("NullableProblems")
public class Deduplication extends Activity implements View.OnClickListener {

    private static final int APP_CHANGE_REQUEST = 253;
    private static final int UPDATE_PERMISSIONS = 254;
    private static final int RUNTIME_PERMISSIONS_CODE = 255;
    private final String[] mRequiredPermissions = {permission.READ_SMS};

    private Intent mService;

    private LinearLayout mProgressBarHolder;
    private ProgressBar mProgressBar;
    private Button mDeDuplicate;
    private Button mCancel;
    private Button mRevert;
    private TextView mDeleted;
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int totalMessages = intent.getIntExtra(Constants.TOTAL_MSGS, 0);
            int deletedMessages = intent.getIntExtra(Constants.DELETED_MSGS, 0);
            if (intent.getBooleanExtra(Constants.INTERRUPTED, false)) {
                cancelDeletion();
                mCancel.setEnabled(true);
                mCancel.setClickable(true);
                mCancel.setText(android.R.string.cancel);
                mDeDuplicate.setVisibility(View.VISIBLE);
                mProgressBarHolder.setVisibility(View.GONE);
                Toast toast = Toast.makeText(context, String.format(getString(R.string.deleted_messages),
                        deletedMessages, totalMessages), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            mDeleted.setText(String.format(getString(R.string.deleted_messages),
                    deletedMessages, totalMessages));
            mProgressBar.setMax(totalMessages);
            mProgressBar.setProgress(deletedMessages);
        }
    };
    private TextView mRevertMessage;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deduplication);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

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
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = mPref.edit();
                                editor.putBoolean(Constants.SHOW_EULA, false);
                                editor.commit();
                                dialog.dismiss();
                                initViews();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
        builder.create().show();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initViews() {
        mDeDuplicate = (Button) findViewById(R.id.deduplicate);
        Button mMoreApps = (Button) findViewById(R.id.more_apps);
        mCancel = (Button) findViewById(R.id.cancel);
        mRevert = (Button) findViewById(R.id.revert);
        mDeleted = (TextView) findViewById(R.id.current_progress);
        mRevertMessage = (TextView) findViewById(R.id.revert_message);
        mProgressBarHolder = (LinearLayout) findViewById(R.id.progress_bar_holder);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mDeDuplicate.setOnClickListener(this);
        mMoreApps.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mRevert.setOnClickListener(this);
        if (getIntent().getBooleanExtra(Constants.FROM_SERVICE, false)) {
            doCancel();
        }

        if (!isValidMessageApp(this)) {
            SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor mEditor = mPref.edit();
            mEditor.putString(Constants.CURRENT_SMS_APP, Sms.getDefaultSmsPackage(this));
            mEditor.apply();
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
        if (checkSelfPermission(mRequiredPermissions[0]) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(mRequiredPermissions[0])) {

                //Educate User
                showCustomDialog(getString(R.string.required_permissions), getString(R.string.m_permission_inform),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //ask permissions.
                                requestPermissions(mRequiredPermissions, RUNTIME_PERMISSIONS_CODE);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                deadLock();
                            }
                        }
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, UPDATE_PERMISSIONS);
                    }
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
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }, null
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(Constants.FROM_SERVICE, false)) {
            doCancel();
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
                startActivity(intent);
                break;
            case R.id.cancel:
                doCancel();
                break;
            case R.id.revert:
                Utils.revertOldApp(getApplicationContext());
                break;
        }
    }

    private void doCancel() {
        mCancel.setEnabled(false);
        mCancel.setClickable(false);
        mCancel.setText(R.string.cancelling);
        mDeDuplicate.setVisibility(View.GONE);
        mProgressBarHolder.setVisibility(View.VISIBLE);
        cancelDeletion();
    }

    private void cancelDeletion() {
        if (mService == null) {
            mService = new Intent(this, DeleteSmsService.class);
        }
        stopService(mService);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void deduplicate() {
        mProgressBar.setMax(0);
        mProgressBar.setProgress(0);
        mDeleted.setText(String.format(getString(R.string.deleted_messages), 0, 0));
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
        warningDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                findDuplicates();
            }
        });

        warningDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = warningDialog.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void findDuplicates() {
        FindDuplicates findDuplicates = new FindDuplicates(this);
        findDuplicates.setOnDuplicatesFoundListener(new OnDuplicatesFoundListener() {
            @Override
            public void duplicatesFound(ArrayList<String> duplicateIds) {
                if (duplicateIds.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.no_duplicates, Toast.LENGTH_SHORT).show();
                    Utils.revertOldApp(getApplicationContext());
                } else {
                    mDeDuplicate.setVisibility(View.GONE);
                    mProgressBarHolder.setVisibility(View.VISIBLE);
                    startDeleteService(duplicateIds);
                }
            }
        });
        findDuplicates.execute();
    }

    private void startDeleteService(ArrayList<String> duplicateIds) {
        mService = new Intent(this, DeleteSmsService.class);
        mService.putStringArrayListExtra(Constants.DUPLICATE_IDS, duplicateIds);
        startService(mService);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == APP_CHANGE_REQUEST) {
            if (resultCode == RESULT_OK || isValidMessageApp(this)) {
                mRevert.setVisibility(View.VISIBLE);
                mRevertMessage.setVisibility(View.VISIBLE);
                showWarning();
            } else {
                mRevert.setVisibility(View.GONE);
                mRevertMessage.setVisibility(View.GONE);
                showCustomDialog(getString(R.string.failed), getString(R.string.failure_message),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }, null);
            }
        } else if (requestCode == UPDATE_PERMISSIONS) {
            if (checkSelfPermission(mRequiredPermissions[0]) != PackageManager.PERMISSION_GRANTED) {
                deadLock();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    protected void onPause() {
        unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.INTENT_FILTER);
        registerReceiver(mMessageReceiver, filter);
    }
}
