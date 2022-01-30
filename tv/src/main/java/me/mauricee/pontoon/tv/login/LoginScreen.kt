package me.mauricee.pontoon.tv.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.mauricee.pontoon.tv.R

@Composable
fun LoginScreen(
    email: String,
    password: String,
    authCode: String,
    emailUpdated: (String) -> Unit,
    passwordUpdated: (String) -> Unit,
    login: () -> Unit
) = Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .align(Alignment.Center)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = emailUpdated
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = passwordUpdated
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = login, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(text = "Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_discord),
                    contentDescription = null
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lttforum),
                    contentDescription = null
                )
            }
        }
    }
}