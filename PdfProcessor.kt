package com.redactedactual.redacter

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

class PdfProcessor(private val context: Context) {
    private val redactionEngine = RedactionEngine()

    suspend fun processPdf(
        uri: Uri,
        redactionTypes: Set<RedactionType>
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        var fd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null

        try {
            fd = context.contentResolver.openFileDescriptor(uri, "r")
            renderer = fd?.let { PdfRenderer(it) }

            for (i in 0 until (renderer?.pageCount ?: 0)) {
                val page = renderer?.openPage(i) ?: continue
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val redacted = redactionEngine.redact(bitmap, redactionTypes)
                bitmaps.add(redacted)
            }
        } finally {
            renderer?.close()
            fd?.close()
        }
        bitmaps
    }

    suspend fun saveAsPdf(
        bitmaps: List<Bitmap>,
        fileName: String = "Redacted_${System.currentTimeMillis()}.pdf"
    ): Uri? = withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Redacter")
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues) ?: return@withContext null

        resolver.openOutputStream(uri)?.use { output ->
            val pdfDoc = PdfDocument()
            bitmaps.forEachIndexed { index, bmp ->
                val pageInfo = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, index + 1).create()
                val page = pdfDoc.startPage(pageInfo)
                page.canvas.drawBitmap(bmp, 0f, 0f, null)
                pdfDoc.finishPage(page)
            }
            pdfDoc.writeTo(output)
            pdfDoc.close()
        }
        uri
    }
}
