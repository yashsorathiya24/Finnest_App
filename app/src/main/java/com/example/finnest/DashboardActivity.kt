package com.example.finnest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.finnest.databinding.ActivityDashboardBinding
import com.example.finnest.fragmet.Home
import com.example.finnest.fragmet.Portfolio

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Portfolio())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.portfolio -> replaceFragment(Portfolio())
                R.id.home -> replaceFragment(Home())
                R.id.profile -> replaceFragment(Portfolio()) // ⚠️ Profile opens Portfolio?
                else -> { }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}