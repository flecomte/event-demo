package eventDemo.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eventDemo.app.network.ApiClient
import eventDemo.shared.game.projection.GameList
import kotlinx.coroutines.launch

private enum class Screen { LOGIN, GAMES }

@Composable
fun App() {
  MaterialTheme {
    Surface(modifier = Modifier.fillMaxSize()) {
      val apiClient = remember { ApiClient() }
      var screen by remember { mutableStateOf(Screen.LOGIN) }

      when (screen) {
        Screen.LOGIN -> LoginScreen(apiClient) { screen = Screen.GAMES }
        Screen.GAMES -> GamesScreen(apiClient) { screen = Screen.LOGIN }
      }
    }
  }
}

@Composable
private fun LoginScreen(
  apiClient: ApiClient,
  onLoggedIn: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text("EventDemo", style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(
      value = username,
      onValueChange = { username = it },
      label = { Text("Username") },
      modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
    )
    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Password") },
      modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
    errorMessage?.let {
      Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
    }
    Button(
      onClick = {
        errorMessage = null
        isLoading = true
        scope.launch {
          apiClient
            .login(username, password)
            .onSuccess { onLoggedIn() }
            .onFailure { errorMessage = it.message ?: "Login failed" }
          isLoading = false
        }
      },
      enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
      modifier = Modifier.padding(top = 16.dp),
    ) {
      Text(if (isLoading) "Signing in..." else "Sign in")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamesScreen(
  apiClient: ApiClient,
  onLoggedOut: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var games by remember { mutableStateOf<List<GameList>>(emptyList()) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(true) }

  fun refresh() {
    isLoading = true
    scope.launch {
      apiClient
        .listGames()
        .onSuccess { games = it }
        .onFailure { errorMessage = it.message ?: "Could not load games" }
      isLoading = false
    }
  }

  remember { refresh() }

  Scaffold(
    topBar = { TopAppBar(title = { Text("Games") }) },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
      when {
        isLoading -> CircularProgressIndicator()
        errorMessage != null -> Text(errorMessage ?: "")
        games.isEmpty() -> Text("No game yet.")
        else ->
          LazyColumn {
            items(games) { game ->
              Text("${game.aggregateId} - ${game.status} - ${game.players.size} player(s)")
            }
          }
      }
      Button(onClick = { refresh() }, modifier = Modifier.padding(top = 16.dp)) {
        Text("Refresh")
      }
      Button(
        onClick = {
          apiClient.logout()
          onLoggedOut()
        },
        modifier = Modifier.padding(top = 8.dp),
      ) {
        Text("Sign out")
      }
    }
  }
}
