package com.example.evplan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evplan.adapter.EventAdapter
import com.example.evplan.databinding.ActivityEventBinding
import com.example.evplan.entity.Event
import com.example.evplan.usecases.EventUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class EventActivity : AppCompatActivity(), EventAdapter.EventItemEvents {

    private lateinit var activityBinding: ActivityEventBinding
    private lateinit var eventAdapter: EventAdapter
    private lateinit var eventUseCase: EventUseCase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        activityBinding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        auth = FirebaseAuth.getInstance()
        eventUseCase = EventUseCase()
        eventAdapter = EventAdapter(mutableListOf(), this)

        // Untuk padding otomatis di area status bar
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupEvents()
        initializeData()
    }

    /** Menyiapkan RecyclerView dengan EventAdapter. */
    private fun setupRecyclerView() {
        activityBinding.container.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(this@EventActivity)
        }
    }

    /** Menyiapkan tombol tambah event & logout. */
    private fun setupEvents() {
        // ✅ Tombol tambah event → buka CreateEventActivity
        activityBinding.tombolTambah.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java)
            startActivity(intent)
        }

        // ✅ Tombol logout
        activityBinding.tombolLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari akun ini?")
                .setPositiveButton("Ya") { _, _ ->
                    auth.signOut()
                    Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    /** Ambil data Event dari Firestore. */
    private fun initializeData() {
        activityBinding.container.visibility = View.GONE
        activityBinding.loading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val data = eventUseCase.getEvents()
                eventAdapter.updateDataSet(data)

                activityBinding.container.visibility = View.VISIBLE
                activityBinding.loading.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@EventActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                activityBinding.loading.visibility = View.GONE
            }
        }
    }

    // Aksi edit event
    override fun onEventItemEdit(event: Event) {
        val intent = Intent(this, EditEventActivity::class.java)
        intent.putExtra("EVENT_ID", event.id)
        intent.putExtra("EVENT_TITLE", event.title)
        intent.putExtra("EVENT_DESCRIPTION", event.description)
        intent.putExtra("EVENT_LOCATION", event.location)
        intent.putExtra("EVENT_DATE", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(event.date))
        intent.putExtra("EVENT_REMINDER", event.reminder)
        startActivity(intent)
    }

    // Aksi hapus event
    override fun onEventItemDelete(event: Event) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Event")
            .setMessage("Apakah kamu yakin ingin menghapus event \"${event.title}\"?")
            .setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    try {
                        eventUseCase.deleteEvent(event.id)
                        initializeData()
                        Toast.makeText(
                            this@EventActivity,
                            "Event berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@EventActivity,
                            "Gagal menghapus event",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
