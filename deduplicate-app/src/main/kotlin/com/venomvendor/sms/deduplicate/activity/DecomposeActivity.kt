/*
 *   Copyright (C) 2020 VenomVendor <info@VenomVendor.com>
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

package com.venomvendor.sms.deduplicate.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.ScaleFit
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.Spacer
import androidx.ui.layout.preferredHeight
import androidx.ui.res.imageResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.venomvendor.sms.deduplicate.R

class DecomposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sampler()
        }
    }
}

@Composable
fun Sampler() {
    val items = listOf(
        "A day in Shark Fin Cove",
        "Davenport, California",
        "December 2018"
    )
    Column(
        modifier = LayoutHeight(16.dp)
    ) {
        items.forEach {
            sampleText(it)
        }
    }
    val image = imageResource(R.drawable.ic_launcher)
    Image(
        asset = image,
        scaleFit = ScaleFit.None
    )
}

@Composable
fun sampleText(text: String) {
    Spacer(modifier = Modifier.preferredHeight(8.dp))
    Text(text)
}

@Preview
@Composable
fun DefaultPreview() {
    Sampler()
}
