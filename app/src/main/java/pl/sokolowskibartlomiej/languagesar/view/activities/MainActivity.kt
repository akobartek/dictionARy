package pl.sokolowskibartlomiej.languagesar.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_list.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import pl.sokolowskibartlomiej.languagesar.utils.showShortToast
import pl.sokolowskibartlomiej.languagesar.view.adapters.LanguageRecyclerAdapter
import pl.sokolowskibartlomiej.languagesar.view.fragments.DictionaryFragmentDirections
import pl.sokolowskibartlomiej.languagesar.view.fragments.TestFragmentDirections
import pl.sokolowskibartlomiej.languagesar.viewmodel.MainViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private var mCurrentFragmentId: Int? = null
    private var mBackPressed = 0L

    private lateinit var mMainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferencesManager.getNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            window.decorView.systemUiVisibility = 0
        } else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PreferencesManager.getNightMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }

        mMainViewModel = ViewModelProvider(this@MainActivity).get(MainViewModel::class.java)

        val navController = (navHostFragment as NavHostFragment? ?: return).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mCurrentFragmentId = destination.id
            if (mCurrentFragmentId == R.id.settingsFragment) bottomNavView.visibility = View.GONE
            else bottomNavView.visibility = View.VISIBLE
        }
        bottomNavView.selectedItemId = R.id.navigation_dictionary

        bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_test -> {
                    if (mCurrentFragmentId == R.id.dictionaryFragment)
                        findNavController(R.id.navHostFragment).navigate(
                            DictionaryFragmentDirections.showTestFragment()
                        )
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_dictionary -> {
                    if (mCurrentFragmentId == R.id.testFragment)
                        findNavController(R.id.navHostFragment).navigate(
                            TestFragmentDirections.showDictionaryFragment()
                        )
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_detection -> {
                    startActivity(Intent(this@MainActivity, PhotoActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(assets.open("words-en.txt")))
            var line = reader.readLine()
            var counter = 1
            while (line != null) {
                Log.d("word", "$counter - line")
                ++counter
                line = reader.readLine()
            }
            reader.close()
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO() -> Ask for language
        if (mMainViewModel.isDictionaryReady.value != true) showSelectLanguageDialog()
        if (bottomNavView.selectedItemId == R.id.navigation_detection)
            bottomNavView.selectedItemId = R.id.navigation_dictionary
    }

    @SuppressLint("InflateParams")
    private fun showSelectLanguageDialog() {
        val dialogView =
            LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_list, null)
        val languageAdapter = LanguageRecyclerAdapter()
        dialogView.dialogRecyclerView.apply {
            layoutManager = LinearLayoutManager(dialogView.context)
            itemAnimator = DefaultItemAnimator()
            adapter = languageAdapter
        }

        AlertDialog.Builder(this@MainActivity)
            .setTitle(R.string.select_language_dialog_title)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                PreferencesManager.setSelectedLanguage(languageAdapter.getSelectedLanguage())
                mMainViewModel.isDictionaryReady.postValue(true)
                dialog?.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog?.dismiss()
                finish()
            }
            .create()
            .show()
    }

    override fun onBackPressed() {
        when (mCurrentFragmentId) {
            R.id.settingsFragment ->
                findNavController(R.id.navHostFragment).navigateUp()
            else -> doubleBackPressToExit()
        }
    }

    private fun doubleBackPressToExit() {
        if (mBackPressed + 2000 > System.currentTimeMillis()) finish()
        showShortToast(R.string.press_to_exit)
        mBackPressed = System.currentTimeMillis()
    }
}