package co.ynd.interview.tomek.feature.camera.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.ynd.interview.tomek.core.ui.component.PermissionRationaleDialog
import co.ynd.interview.tomek.feature.camera.R

private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO
)

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun RequireCameraPermissions(
    onGranted: @Composable () -> Unit,
    onDenied: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionsGranted by rememberSaveable {
        mutableStateOf(
            REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var showRationale by rememberSaveable { mutableStateOf(false) }
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }
    var hasRequestedOnce by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
        if (!permissionsGranted) {
            val activity = context.findActivity()
            val shouldShow = activity != null && REQUIRED_PERMISSIONS.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }
            when {
                shouldShow -> showRationale = true
                hasRequestedOnce -> permanentlyDenied = true
            }
            hasRequestedOnce = true
        }
    }

    // Re-check on resume so returning from Settings refreshes state.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsGranted = REQUIRED_PERMISSIONS.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            launcher.launch(REQUIRED_PERMISSIONS)
        }
    }

    val permissionName = stringResource(R.string.camera_permission_name)
    val permissionGrant = stringResource(R.string.camera_permission_grant)
    val openSettings = stringResource(R.string.camera_open_settings)

    when {
        permissionsGranted -> onGranted()
        showRationale -> {
            PermissionRationaleDialog(
                permissionName = permissionName,
                confirmText = permissionGrant,
                onConfirm = {
                    showRationale = false
                    launcher.launch(REQUIRED_PERMISSIONS)
                },
                onDismiss = { showRationale = false }
            )
        }
        permanentlyDenied -> {
            PermissionRationaleDialog(
                permissionName = permissionName,
                confirmText = openSettings,
                onConfirm = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                onDismiss = { permanentlyDenied = false }
            )
        }
        else -> onDenied()
    }
}
