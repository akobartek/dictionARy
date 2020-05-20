package pl.sokolowskibartlomiej.languagesar.view.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import pl.sokolowskibartlomiej.languagesar.utils.showShortToast
import pl.sokolowskibartlomiej.languagesar.view.fragments.DictionaryFragment
import pl.sokolowskibartlomiej.languagesar.view.fragments.DictionaryFragmentDirections
import pl.sokolowskibartlomiej.languagesar.view.fragments.TestResultsFragmentDirections
import pl.sokolowskibartlomiej.languagesar.view.fragments.TestStartFragmentDirections

class MainActivity : AppCompatActivity() {

    private var mCurrentFragmentId: Int? = null
    private var mBackPressed = 0L

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

        val navController = (navHostFragment as NavHostFragment? ?: return).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mCurrentFragmentId = destination.id
            if (mCurrentFragmentId == R.id.settingsFragment || mCurrentFragmentId == R.id.testQuestionsFragment)
                bottomNavView.visibility = View.GONE
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
                    if (mCurrentFragmentId == R.id.testStartFragment)
                        findNavController(R.id.navHostFragment).navigate(
                            TestStartFragmentDirections.showDictionaryFragment()
                        )
                    else if (mCurrentFragmentId == R.id.testResultsFragment)
                        findNavController(R.id.navHostFragment).navigate(
                            TestResultsFragmentDirections.showDictionaryFragment()
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
    }

    override fun onResume() {
        super.onResume()
        if (bottomNavView.selectedItemId == R.id.navigation_detection)
            bottomNavView.selectedItemId = R.id.navigation_dictionary
    }

    override fun onBackPressed() {
        when (mCurrentFragmentId) {
            R.id.dictionaryFragment ->
                if ((supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
                        .childFragmentManager.fragments[0] as DictionaryFragment).onBackPressed()
                ) doubleBackPressToExit()
            // TODO() -> onBackPressed for test questions and results fragments
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