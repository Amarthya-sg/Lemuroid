package come.asg.lemudroid.app.mobile.feature.cheats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import come.asg.lemudroid.R
import come.asg.lemudroid.lib.library.db.dao.GameCheatDao
import come.asg.lemudroid.lib.library.db.entity.GameCheat
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun GameCheatsScreen(
    gameId: Int,
    gameName: String = "",
    gameCheatDao: GameCheatDao,
    modifier: Modifier = Modifier,
    onCheatsChanged: () -> Unit = {},
) {
    val cheats by gameCheatDao.observeForGame(gameId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCheat by remember { mutableStateOf<GameCheat?>(null) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (gameName.isNotBlank()) {
            Text(text = gameName, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.game_cheats_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.game_cheats_add),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        if (cheats.isEmpty()) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.game_cheats_empty),
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            cheats.forEach { cheat ->
                GameCheatItem(
                    cheat = cheat,
                    onEdit = { editingCheat = cheat },
                    onEnabledChanged = { enabled ->
                        onCheatsChanged()
                        scope.launch {
                            runCatching { gameCheatDao.update(cheat.copy(enabled = enabled)) }
                                .onFailure { Timber.e(it, "Unable to update cheat ${cheat.id}") }
                        }
                    },
                    onDelete = {
                        onCheatsChanged()
                        scope.launch {
                            runCatching { gameCheatDao.delete(cheat) }
                                .onFailure { Timber.e(it, "Unable to delete cheat ${cheat.id}") }
                        }
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showAddDialog) {
        GameCheatEditorDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, code ->
                onCheatsChanged()
                scope.launch {
                    runCatching {
                        gameCheatDao.insert(
                            GameCheat(
                                gameId = gameId,
                                name = name,
                                code = code,
                                enabled = false,
                            ),
                        )
                    }.onSuccess {
                        showAddDialog = false
                    }.onFailure { Timber.e(it, "Unable to add a cheat for game $gameId") }
                }
            },
        )
    }

    editingCheat?.let { cheat ->
        GameCheatEditorDialog(
            cheat = cheat,
            onDismiss = { editingCheat = null },
            onSave = { name, code ->
                onCheatsChanged()
                scope.launch {
                    runCatching { gameCheatDao.update(cheat.copy(name = name, code = code)) }
                        .onSuccess { editingCheat = null }
                        .onFailure { Timber.e(it, "Unable to edit cheat ${cheat.id}") }
                }
            },
        )
    }
}

@Composable
private fun GameCheatItem(
    cheat: GameCheat,
    onEdit: () -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = cheat.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = cheat.code,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Switch(checked = cheat.enabled, onCheckedChange = onEnabledChanged)
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = androidx.compose.ui.res.stringResource(R.string.game_cheats_edit_action, cheat.name),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = androidx.compose.ui.res.stringResource(R.string.game_cheats_delete, cheat.name),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun GameCheatEditorDialog(
    cheat: GameCheat? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String) -> Unit,
) {
    var name by remember(cheat?.id) { mutableStateOf(cheat?.name.orEmpty()) }
    var code by remember(cheat?.id) { mutableStateOf(cheat?.code.orEmpty()) }
    val canSave = name.isNotBlank() && code.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                androidx.compose.ui.res.stringResource(
                    if (cheat == null) R.string.game_cheats_add_title else R.string.game_cheats_edit_title,
                ),
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(androidx.compose.ui.res.stringResource(R.string.game_cheats_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(androidx.compose.ui.res.stringResource(R.string.game_cheats_code)) },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = { onSave(name.trim(), code.trim()) },
            ) {
                Text(
                    androidx.compose.ui.res.stringResource(
                        if (cheat == null) R.string.game_cheats_save else R.string.game_cheats_update,
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.cancel))
            }
        },
    )
}
