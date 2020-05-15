package pl.sokolowskibartlomiej.languagesar.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import kotlinx.android.synthetic.main.activity_photo.*
import kotlinx.android.synthetic.main.content_photo.*
import kotlinx.android.synthetic.main.dialog_photo.view.*
import kotlinx.android.synthetic.main.top_camera_actionbar.*
import pl.sokolowskibartlomiej.languagesar.BuildConfig
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.*
import pl.sokolowskibartlomiej.languagesar.viewmodel.PhotoViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PhotoActivity : AppCompatActivity() {

    private var mDisplayId: Int = -1
    private var mPreview: Preview? = null
    private var mImageCapture: ImageCapture? = null
    private var mCamera: Camera? = null
    private var mOrientationEventListener: OrientationEventListener? = null
    private var mRotation = 0f

    private lateinit var mPhotoViewModel: PhotoViewModel
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mLoadingDialog: AlertDialog
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>

    private val displayManager by lazy {
        getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == this@PhotoActivity.mDisplayId) {
                mImageCapture?.targetRotation = windowManager.defaultDisplay.rotation
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        mPhotoViewModel = ViewModelProvider(this@PhotoActivity).get(PhotoViewModel::class.java)
        cameraExecutor = Executors.newSingleThreadExecutor()
        displayManager.registerDisplayListener(displayListener, null)
        mBottomSheetBehavior = from(translationBottomSheet)
        mBottomSheetBehavior.state = STATE_HIDDEN
        mLoadingDialog = AlertDialog.Builder(this@PhotoActivity)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        if (mPhotoViewModel.objectsLabels.value.isNullOrEmpty())
            openTranslationBottomSheetBtn.visibility = View.GONE
        if (mPhotoViewModel.latestPhoto.value == null)
            photoThumbnail.visibility = View.GONE

        viewFinder.post {
            mDisplayId = viewFinder.display.displayId
        }
        mPhotoViewModel.latestPhoto.observe(this@PhotoActivity, Observer {
            if (it != null) {
                photoThumbnail.visibility = View.VISIBLE
                GlideApp.with(this@PhotoActivity)
                    .load(it)
                    .circleCrop()
                    .into(photoThumbnail)
            } else photoThumbnail.visibility = View.GONE
        })
        mPhotoViewModel.translation.observe(this@PhotoActivity, Observer {
            if (mLoadingDialog.isShowing) mLoadingDialog.hide()
            if (it == null) openTranslationBottomSheetBtn.visibility = View.GONE
            else {
                openTranslationBottomSheetBtn.visibility = View.VISIBLE
                mBottomSheetBehavior.state = STATE_COLLAPSED
            }
        })

        detectObjectBtn.setOnClickListener {
            mLoadingDialog.show()
            mImageCapture?.takePicture(
                cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                    @SuppressLint("UnsafeExperimentalUsageError")
                    override fun onCaptureSuccess(image: ImageProxy) {
                        image.use {
                            val rotation = image.imageInfo.rotationDegrees - mRotation
                            val bitmap = it.image?.toBitmap(rotation)
                            if (bitmap != null) {
                                mPhotoViewModel.latestPhoto.postValue(bitmap)
                                tryToRunFunctionOnInternet({ detectObjectsOnCloud(bitmap) }, {})
                            } else showShortToast(R.string.image_capture_error)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                        showShortToast(R.string.image_capture_error)
                    }
                })
        }
        openTranslationBottomSheetBtn.setOnClickListener {
            mBottomSheetBehavior.state = STATE_COLLAPSED
        }
        photoThumbnail.setOnClickListener { showPhotoDialog() }
        flashBtn.setOnClickListener {
            mImageCapture?.flashMode =
                if (it.isSelected) ImageCapture.FLASH_MODE_OFF else ImageCapture.FLASH_MODE_ON
            it.isSelected = !it.isSelected
        }
        closeBtn.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) viewFinder.post { bindCameraUseCases() }
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        mOrientationEventListener =
            object : OrientationEventListener(this) {
                override fun onOrientationChanged(orientation: Int) {
                    if ((orientation < 35 || orientation > 325) && mRotation != 0f) {
                        mRotation = 0f
                        rotateUI(mRotation)
                    } else if (orientation in 146..214 && mRotation != 180f) {
                        mRotation = 180f
                        rotateUI(mRotation)
                    } else if (orientation in 56..124 && mRotation != 270f) {
                        mRotation = 270f
                        rotateUI(mRotation)
                    } else if (orientation in 236..304 && mRotation != 90f) {
                        mRotation = 90f
                        rotateUI(mRotation)
                    }
                }
            }
        mOrientationEventListener?.enable()
    }

    private fun rotateUI(rotation: Float) {
        openTranslationBottomSheetBtn.rotate(rotation)
        photoThumbnail.rotate(rotation)
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
        mOrientationEventListener?.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onBackPressed() {
        if (::mBottomSheetBehavior.isInitialized && mBottomSheetBehavior.state != STATE_HIDDEN)
            mBottomSheetBehavior.state = STATE_HIDDEN
        else super.onBackPressed()
    }

    @SuppressLint("InflateParams")
    private fun showPhotoDialog() {
        val builder = Dialog(this@PhotoActivity)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogView =
            LayoutInflater.from(this@PhotoActivity).inflate(R.layout.dialog_photo, null)
        dialogView.photo.setImageBitmap(mPhotoViewModel.latestPhoto.value)
        dialogView.closeDialogBtn.setOnClickListener { builder.hide() }

        builder.setContentView(dialogView)
        builder.show()
    }

    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = viewFinder.display.rotation

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this@PhotoActivity)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            mPreview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            mImageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()

            try {
                mCamera =
                    cameraProvider.bindToLifecycle(this, cameraSelector, mPreview, mImageCapture)
                mPreview?.setSurfaceProvider(viewFinder.createSurfaceProvider(mCamera?.cameraInfo))
            } catch (exc: Exception) {
                if (BuildConfig.DEBUG) Log.e("MainActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this@PhotoActivity))
    }

    private fun detectObjectsOnCloud(bitmap: Bitmap) {
        if (!mLoadingDialog.isShowing) mLoadingDialog.show()
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val options = FirebaseVisionCloudImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.65f)
            .build()
        val detector = FirebaseVision.getInstance()
            .getCloudImageLabeler(options)

        detector.processImage(image)
            .addOnSuccessListener {
                val labels = ArrayList(it.map { label -> label.text })
                mPhotoViewModel.objectsLabels.postValue(labels)
                mPhotoViewModel.fetchLabelsTranslation(labels)
            }
            .addOnFailureListener {
                if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                showShortToast(R.string.object_detection_error)
            }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) AspectRatio.RATIO_4_3
        else AspectRatio.RATIO_16_9
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { bindCameraUseCases() }
            } else {
                AlertDialog.Builder(this)
                    .setTitle(R.string.permission_error_title)
                    .setMessage(R.string.permission_error_msg)
                    .setCancelable(false)
                    .setPositiveButton(R.string.allow) { dialog, _ ->
                        dialog.dismiss()
                        ActivityCompat.requestPermissions(
                            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                        )
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat
                    .checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 42
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}