package com.codelab.mlkitcodelab

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.codelab.mlkitcodelab.utils.AppConstants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector
import kotlinx.android.synthetic.main.activity_classify_image.*
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Get the image from the gallery and upload to the firebase storage.
 * Access firebase cloud vision api to get the content of the image and
 * upload it to cloud firestore.
 */
class RecognizeTextActivity : AppCompatActivity() {

    companion object {
        private const val IMAGE_PICKER_TYPE = "image/*"
        private const val SELECT_IMAGE = "Select Picture"
    }

    //Reference of the collection.
    private val mFirestoreReference by lazy {
        FirebaseFirestore.getInstance().collection(AppConstants.DATABASE_COLLECTION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classify_image)

        launchImagePicker()
    }

    /** Open image picker dialog to select image from the device */
    private fun launchImagePicker() {
        val intent = Intent()
        // Show only images, no videos or anything else
        intent.type = IMAGE_PICKER_TYPE
        intent.action = Intent.ACTION_GET_CONTENT
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, SELECT_IMAGE), 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri = data.data

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

                val (images, labelDetector) = initiateFirebaseVision(bitmap)
                fetchTextFromImage(labelDetector, images)
                ivLabelIt.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Fetch the text and push it to the cloud firestore.
     */
    private fun fetchTextFromImage(labelDetector: FirebaseVisionTextDetector,
                                   images: FirebaseVisionImage) {

        labelDetector.detectInImage(images)
                .addOnCompleteListener { task ->
                    val builder = StringBuilder()
                    if (task.isSuccessful && task.result.blocks.isEmpty().not()) {
                        for (blocks in task.result.blocks) {
                            builder.append(blocks.text).append("\n")
                        }
                        mFirestoreReference.add(
                                mapOf("Text" to builder.toString(),
                                        "Timestamp" to FieldValue.serverTimestamp())
                        )
                        Toast.makeText(this@RecognizeTextActivity, "Success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RecognizeTextActivity, "Failed ${task.exception}", Toast.LENGTH_LONG).show()
                    }
                }
    }

    /**
     * Set the option with firebase vision and initiate cloud label detector with
     * desired options. When option is omitted it works with the default settings.
     */
    private fun initiateFirebaseVision(bitmap: Bitmap):
            Pair<FirebaseVisionImage, FirebaseVisionTextDetector> {

        val images = FirebaseVisionImage.fromBitmap(bitmap)

        //Use detector to identify which service to use
        val labelDetector = FirebaseVision.getInstance().visionTextDetector
        return Pair(images, labelDetector)
    }
}