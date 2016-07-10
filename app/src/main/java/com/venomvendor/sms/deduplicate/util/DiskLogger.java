/*
 *   Copyright (C) 2016 VenomVendor <info@VenomVendor.com>
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

package com.venomvendor.sms.deduplicate.util;

import android.Manifest;
import android.os.Environment;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class DiskLogger {
    private DiskLogger() {
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static void log(String... messages) {
        writeText(TextUtils.join(", ", messages));
    }

    private static void writeText(String msg) {
        // Log.d("LOGGER", msg);
        try {
            File myFile = new File(Environment.getExternalStorageDirectory() + "/deduplicate.tar");
            FileOutputStream fOut = new FileOutputStream(myFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(System.getProperty("line.separator"));
            myOutWriter.append(msg);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
