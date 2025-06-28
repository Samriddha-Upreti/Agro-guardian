package com.example.agribuddy

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.agribuddy.databinding.ActivityMainBinding
import com.example.agribuddy.ui.settings.settings
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        NotificationManagerSingleton.addNotification(this, "Title", "This is a test message")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar and drawer
        setSupportActionBar(binding.toolbar)
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Handle navigation item clicks
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> replaceFragment(DashboardFragment())
                R.id.nav_settings -> replaceFragment(settings())
                R.id.nav_weather -> replaceFragment(WeatherFragment())
                R.id.nav_marketplace -> replaceFragment(MarketplaceFragment())
                R.id.sensors -> {
                    val intent = Intent(this, SensorsActivity::class.java)
                    startActivity(intent)
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        // Load default fragment
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
        }

        updateNavHeader()
    }

    private fun updateNavHeader() {
        val user = FirebaseAuth.getInstance().currentUser
        CurrentUser.name = user?.displayName
        CurrentUser.email = user?.email

        val headerView = binding.navView.getHeaderView(0)
        val profileName = headerView.findViewById<TextView>(R.id.profile_name)
        val profileImage = headerView.findViewById<ImageView>(R.id.profile_image)

        profileName.text = CurrentUser.name ?: "Guest"
        profileImage.setImageResource(R.drawable.ic_profile) // default image
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
