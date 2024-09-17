package com.example.funfacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.funfacts.ui.theme.FunFactsTheme

val colors = listOf(Color.Red, Color.Blue, Color.DarkGray, Color.Magenta, Color.Cyan)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FunFactsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UpdateFact()
                }
            }
        }
    }
}

private suspend fun fetchFacts(callback: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://api.api-ninjas.com/v1/facts?"
            val url = URL(apiUrl)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("X-Api-Key", "ufRtxnNVzIGC/PWm08KxDg==DkJ44HWdhxzwoIXt")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonArray = JSONArray(response.toString())
                val jsonObject = jsonArray.getJSONObject(0)
                callback(jsonObject.getString("fact"))
            } else {
                val errorStream = connection.errorStream
                val errorReader = BufferedReader(InputStreamReader(errorStream))
                val errorResponse = errorReader.readText()
                errorReader.close()
                callback("Error: $responseCode, $errorResponse")
            }
        } catch (e: Exception) {
            callback("Failed to load quote: ${e.message}")
        }
    }
}


@Composable
fun FactDisplay(fact: String) {
    Text(
        text = fact,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = getRandomColor(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun UpdateFact() {
    var fact by remember { mutableStateOf("Fetching quote...") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        fetchFacts { fetchFacts ->
            fact = fetchFacts
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        FactDisplay(fact = fact)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            coroutineScope.launch {
                fetchFacts{ fetchFacts ->
                    fact = fetchFacts
                }
            }
        }) {
            Text(text = "Next Fact")
        }
    }
}

private fun getRandomColor(): Color {
    return colors.random()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FunFactsTheme {
        UpdateFact()
    }
}