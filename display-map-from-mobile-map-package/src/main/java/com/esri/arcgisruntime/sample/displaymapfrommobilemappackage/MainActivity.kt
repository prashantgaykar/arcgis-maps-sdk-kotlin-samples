/* Copyright 2022 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.displaymapfrommobilemappackage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import arcgisruntime.ApiKey
import arcgisruntime.ArcGISRuntimeEnvironment
import arcgisruntime.LoadStatus
import arcgisruntime.mapping.MobileMapPackage
import arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.displaymapfrommobilemappackage.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.sampleslib.SampleActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : SampleActivity() {

    private val TAG = MainActivity::class.java.simpleName

    // ArcGIS Portal item containing the .mmpk mobile map package
    private val provisionURL: String = "https://www.arcgis.com/home/item.html?id=e1f3a7254cb845b09450f54937c16061"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // authentication with an API key or named user is
        // required to access basemaps and other location services
        ArcGISRuntimeEnvironment.apiKey = ApiKey.create(BuildConfig.API_KEY)

        // set up data binding for the activity
        val activityMainBinding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        // create and add the MapView to the lifecycle
        val mapView = activityMainBinding.mapView
        lifecycle.addObserver(mapView)

        // get the file path of the (.mmpk) file
        val filePath = getExternalFilesDir(null)?.path + getString(R.string.yellowstone_mmpk)

        lifecycleScope.launch {
            // start the download manager to automatically add the .mmpk file to the app
            // alternatively, you can use ADB/Device File Explorer
            downloadManager(provisionURL, filePath).collect { downloadStatus ->
                if (downloadStatus == LoadStatus.Loaded) {
                    // download complete, resuming sample
                    openMobileMapPackage(mapView, filePath)
                } else if (downloadStatus is LoadStatus.FailedToLoad) {
                    // failed to download provision data
                    showError(downloadStatus.error.message.toString(), mapView)
                }
            }
        }

    }

    /**
     * Sets the [mapView] to display a map using the .mmpk
     * file located at [filePath]
     */
    private suspend fun openMobileMapPackage(mapView: MapView, filePath: String) {
        // create the mobile map package
        val mapPackage = MobileMapPackage(filePath)
        // load the mobile map package
        val loadResult = mapPackage.load()
        loadResult.apply {
            onSuccess {
                // add the map from the mobile map package to the MapView
                mapView.map = mapPackage.maps[0]
            }
            onFailure { throwable ->
                showError(throwable.message.toString(), mapView)
            }
        }
    }

    private fun showError(message: String, view: View) {
        Log.e(TAG, message)
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }
}
