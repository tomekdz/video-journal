package co.ynd.interview.tomek.feature.camera.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ynd.interview.tomek.core.ui.component.VideoPlayer
import co.ynd.interview.tomek.feature.camera.R

@Composable
internal fun ReviewContent(
    filePath: String,
    onSave: (description: String) -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
    saveError: String? = null
) {
    val maxLength = 500
    var description by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VideoPlayer(
            filePath = filePath,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= maxLength) description = it },
            label = { Text(stringResource(R.string.camera_description_hint)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            supportingText = { Text("${description.length}/$maxLength") }
        )

        if (saveError != null) {
            Text(
                text = saveError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDiscard,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.camera_discard))
            }
            Button(
                onClick = { onSave(description) },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.camera_save))
            }
        }
    }
}
