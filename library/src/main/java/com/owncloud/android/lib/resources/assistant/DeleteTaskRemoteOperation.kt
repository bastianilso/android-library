/*
 * Nextcloud Android Library is available under MIT license
 *
 * @author Alper Ozturk
 * Copyright (C) 2024 Alper Ozturk
 * Copyright (C) 2024 Nextcloud GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.owncloud.android.lib.resources.assistant

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.users.DeletePrivateKeyRemoteOperation
import java.io.IOException
import java.net.HttpURLConnection

class DeleteTaskRemoteOperation(private val appId: Long) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        var postMethod: DeleteMethod? = null
        var result: RemoteOperationResult<Void>
        try {
            postMethod =
                DeleteMethod(
                    client.baseUri.toString() + DIRECT_ENDPOINT + appId,
                    true
                )
            val status = client.execute(postMethod)
            result = RemoteOperationResult<Void>(status == HttpURLConnection.HTTP_OK, postMethod)
        } catch (e: IOException) {
            result = RemoteOperationResult<Void>(e)
            Log_OC.e(TAG, "Deletion of task failed: " + result.logMessage, result.exception)
        } finally {
            postMethod?.releaseConnection()
        }

        return result
    }

    companion object {
        private val TAG = DeletePrivateKeyRemoteOperation::class.java.simpleName
        private const val DIRECT_ENDPOINT =
            "/ocs/v2.php/textprocessing/task/"
    }
}
