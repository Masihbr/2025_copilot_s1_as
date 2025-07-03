package com.example.movieswipe.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GroupManagementScreen(
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create a Group")
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Group Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { if (groupName.isNotBlank()) onCreateGroup(groupName) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Group")
        }
        Text("or")
        Text("Join a Group")
        OutlinedTextField(
            value = inviteCode,
            onValueChange = { inviteCode = it },
            label = { Text("Invite Code") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { if (inviteCode.isNotBlank()) onJoinGroup(inviteCode) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join Group")
        }
    }
}

