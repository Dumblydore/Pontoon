package me.mauricee.pontoon.tv.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import me.mauricee.pontoon.tv.ui.createScreen

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createScreen {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        LoginScreen(
            email = email,
            emailUpdated = { email = it },
            password = password,
            passwordUpdated = { password = it },
            authCode = "",
            login = {}
        )
    }
}