package com.salman.tarun.afinal.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.salman.tarun.afinal.R
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var image: File
    private lateinit var storageDir: File
    private lateinit var photoURI: Uri

    companion object {
        const val TAKE_PHOTO_PERMISSION = 1
        const val REQUEST_TAKE_PHOTO = 2
        const val PICK_IMAGE_REQUEST = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        title = "Sign Up"
        // get storage directory
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // checks for camera permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            imageUser.isEnabled = false
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), TAKE_PHOTO_PERMISSION)
        }

        buttonSignUp.setOnClickListener {

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isAcceptingText)
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            // user form input
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val confirmPassword = editConfirmPassword.text.toString().trim()
            val roleID = radioGroupRole.checkedRadioButtonId

            // if any fields are left empty
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || roleID == -1) {
                Toast.makeText(this, "Please enter all the fields.", Toast.LENGTH_LONG).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Password and confirm password fields should match", Toast.LENGTH_LONG).show()
            } else {
                // Firebase authentication
                auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this@SignUpActivity, {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Successfully created an account!", Toast.LENGTH_LONG).show()
                        // want to make user sign back in rather than continuing in app after sign up
                        auth.signOut()
                        val indexAt = email.indexOf('@')
                        val computingID = email.substring(0, indexAt)
                        val isStudent = when (roleID) {
                            R.id.radioInstructor -> false
                            R.id.radioStudent -> true
                            else -> true
                        }
                        val remoteDb = FirebaseDatabase.getInstance().reference
                        remoteDb.child("users").child(computingID).setValue(isStudent)

                        // save the image path and rotation in shared preferences
                        val editor = getSharedPreferences(application.packageName + ".PREF_KEY", Context.MODE_PRIVATE).edit()
                        Log.d("shared", "preferences")
                        editor.putString("$computingID-userImagePath", imagePath ?: "")
                        editor.putFloat("$computingID-userImageRotation", rotation)
                        editor.apply()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "Account creation failed. ${it.exception}", Toast.LENGTH_LONG).show()
                    }

                })
            }
        }
        // take a picture when the image is pressed
        imageUser.setOnClickListener {
            takePicture()
        }
    }

    private var imagePath: String? = null
    private var rotation = 0.0f

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Log.d("afterTaking", "this")
            // sets the image view to whatever picture was taken
            imageUser.setImageURI(photoURI)
            val rotation = getCameraPhotoOrientation(this, photoURI, image.absolutePath).toFloat()
            imageUser.rotation = rotation
            imagePath = photoURI.path
            this.rotation = rotation
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data
            imageUser.setImageURI(selectedImageUri)
            imageUser.rotation = getCameraPhotoOrientation(this, selectedImageUri, storageDir.absolutePath + "/" + selectedImageUri.path).toFloat()
        }
    }

    // helper function for correcting the orientation of the input image URI
    private fun getCameraPhotoOrientation(context: Context, imageUri: Uri,
                                          imagePath: String): Int {
        var rotate = 0
        try {
            context.contentResolver.notifyChange(imageUri, null)
            val imageFile = File(imagePath)
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }

        } catch (e: Exception) {
            Log.d("orientation issue", "there has been an image orientation issue")
        }

        return rotate
    }

    // creates image file
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        image = File(storageDir, "$imageFileName.jpg")
        return image
    }

    // called to take a picture and save it
    private fun takePicture() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        photoURI = FileProvider.getUriForFile(applicationContext, application.packageName + ".fileprovider", file)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO)
    }

    // request camera permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // This is called when permissions are either granted or not for the app
        // You do not need to edit this code.

        if (requestCode == TAKE_PHOTO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                imageUser.isEnabled = true
            }
        }
    }


    // retrieves images from the image library
    fun getImageFromLibrary(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onBackPressed() {
        finish()
    }
}