/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2021 Tobias Kaminsky
 *   Copyright (C) 2021 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.files

import com.nextcloud.test.RandomStringGenerator
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.tags.CreateTagRemoteOperation
import com.owncloud.android.lib.resources.tags.GetTagsRemoteOperation
import com.owncloud.android.lib.resources.tags.PutTagRemoteOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadFolderRemoteOperationIT : AbstractIT() {
    companion object {
        const val TAG_LENGTH = 10
    }

    @Test
    fun readRemoteFolderWithContent() {
        val remotePath = "/test/"

        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(client).isSuccess)

        // create file
        val filePath = createFile("text")
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath + "1.txt", "text/markdown", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        var result = ReadFolderRemoteOperation(remotePath).execute(client)

        assertTrue(result.isSuccess)
        assertEquals(2, result.data.size)

        // tag testing only on NC27+
        testOnlyOnServer(NextcloudVersion.nextcloud_27)

        // Folder
        var remoteFolder = result.data[0] as RemoteFile
        assertEquals(remotePath, remoteFolder.remotePath)
        assertEquals(0, remoteFolder.tags?.size)

        // File
        var remoteFile = result.data[1] as RemoteFile
        assertEquals(remotePath + "1.txt", remoteFile.remotePath)
        assertEquals(0, remoteFile.tags?.size)

        // create tag
        val tag1 = "a" + RandomStringGenerator.make(TAG_LENGTH)
        val tag2 = "b" + RandomStringGenerator.make(TAG_LENGTH)
        assertTrue(CreateTagRemoteOperation(tag1).execute(nextcloudClient).isSuccess)
        assertTrue(CreateTagRemoteOperation(tag2).execute(nextcloudClient).isSuccess)

        // list tags
        val tags = GetTagsRemoteOperation().execute(client).resultData

        // add tag
        assertTrue(
            PutTagRemoteOperation(
                tags[0].id,
                remoteFile.localId
            ).execute(nextcloudClient).isSuccess
        )
        assertTrue(
            PutTagRemoteOperation(
                tags[1].id,
                remoteFile.localId
            ).execute(nextcloudClient).isSuccess
        )

        // check again
        result = ReadFolderRemoteOperation(remotePath).execute(client)

        assertTrue(result.isSuccess)
        assertEquals(2, result.data.size)

        // Folder
        remoteFolder = result.data[0] as RemoteFile
        assertEquals(remotePath, remoteFolder.remotePath)
        assertEquals(0, remoteFolder.tags?.size)

        // File
        remoteFile = result.data[1] as RemoteFile
        assertEquals(remotePath + "1.txt", remoteFile.remotePath)
        assertEquals(2, remoteFile.tags?.size)

        remoteFile.tags?.sort()
        assertEquals(tag1, remoteFile.tags?.get(0))
        assertEquals(tag2, remoteFile.tags?.get(1))
    }
}
