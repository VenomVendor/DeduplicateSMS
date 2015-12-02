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

package com.venomvendor.sms.deduplicate.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.venomvendor.sms.deduplicate.R;
import com.venomvendor.sms.deduplicate.activity.Deduplication;
import com.venomvendor.sms.deduplicate.util.Constants;
import com.venomvendor.sms.deduplicate.util.Utils;

import java.util.ArrayList;
import java.util.Collections;

/*
 * Created by VenomVendor on 11/8/15.
 */
public class DeleteSmsService extends Service {
    private static final int NOTIFICATION_ID = 500;
    private ArrayList<String> mDuplicateIds;
    private Thread mDelDuplicates;

    public DeleteSmsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDuplicateIds = intent.getStringArrayListExtra(Constants.DUPLICATE_IDS);
        deleteDuplicates();
        return START_STICKY;
    }

    private void deleteDuplicates() {

        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, Deduplication.class);
        intent.putExtra(Constants.FROM_SERVICE, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            builder = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.deleting_duplicates))
                    .setContentText(getString(R.string.tap_here))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setAutoCancel(false);
        } else {
            builder = null;
        }

        Collections.sort(mDuplicateIds);
        final int size = mDuplicateIds.size();

        mDelDuplicates = new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = getContentResolver();
                int k = 0;
                for (int i = 0; i < size; i++) {
                    if (!mDelDuplicates.isInterrupted()) {
                        k += contentResolver.delete(Constants.CONTENT_URI, "_id=?", new String[]{mDuplicateIds.get(i)});

                        if (builder != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                builder.setContentTitle(String.format(getString(R.string.deleted_messages),
                                        i + 1, size));
                                builder.setProgress(size, i + 1, false);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                notificationManager.notify(NOTIFICATION_ID, builder.build());
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                notificationManager.notify(NOTIFICATION_ID, builder.getNotification());
                            }
                        }

                        Intent broadCastIntent = new Intent(Constants.INTENT_FILTER);
                        broadCastIntent.putExtra(Constants.TOTAL_MSGS, size);
                        broadCastIntent.putExtra(Constants.DELETED_MSGS, i + 1);
                        sendBroadcast(broadCastIntent);
                    }
                }
                Intent broadCastIntent = new Intent(Constants.INTENT_FILTER);
                broadCastIntent.putExtra(Constants.INTERRUPTED, true);
                broadCastIntent.putExtra(Constants.TOTAL_MSGS, size);
                broadCastIntent.putExtra(Constants.DELETED_MSGS, k);
                sendBroadcast(broadCastIntent);
                notificationManager.cancel(NOTIFICATION_ID);

                Utils.revertOldApp(getApplicationContext());

                stopSelf();

            }
        });

        if (builder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                builder.setProgress(size, 0, false);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                notificationManager.notify(NOTIFICATION_ID, builder.getNotification());
            }
        }

        mDelDuplicates.start();

    }

    @Override
    public void onDestroy() {
        mDelDuplicates.interrupt();
        super.onDestroy();
    }
}
