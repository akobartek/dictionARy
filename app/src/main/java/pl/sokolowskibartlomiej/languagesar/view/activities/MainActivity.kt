package pl.sokolowskibartlomiej.languagesar.view.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import pl.sokolowskibartlomiej.languagesar.R
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO() -> Ask for language

        // TODO() -> Add german and english

        lifecycleScope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(assets.open("words-es.txt")))
            var line = reader.readLine()
            while (line != null) {
                val words = line.split(" - ")
                if (words[2].contains(",")) Log.d("word", "todo")
                Log.d("word", "${words[2]} - ${words[1]} - ${words[1]}")
                line = reader.readLine()
            }
            reader.close()
        }
    }
}