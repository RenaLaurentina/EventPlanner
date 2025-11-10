package com.example.evplan

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.evplan.databinding.ActivityEditEventBinding
import com.example.evplan.entity.Event
import com.example.evplan.usecases.EventUseCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditEventBinding
    private lateinit var eventUseCase: EventUseCase
    private var eventId: String = ""
    private var selectedDateTime: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventUseCase = EventUseCase()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil data awal dari Intent
        eventId = intent.getStringExtra("EVENT_ID") ?: ""
        binding.etTitle.setText(intent.getStringExtra("EVENT_TITLE") ?: "")
        binding.etDescription.setText(intent.getStringExtra("EVENT_DESCRIPTION") ?: "")
        binding.etLocation.setText(intent.getStringExtra("EVENT_LOCATION") ?: "")
        intent.getStringExtra("EVENT_DATE")?.let {
            binding.etDate.setText(it)
        }
        binding.cbReminder.isChecked = intent.getBooleanExtra("EVENT_REMINDER", false)

        registerEvents()
        loadData()
    }

    private fun registerEvents() {
        // Klik field tanggal → buka pemilih waktu
        binding.etDate.setOnClickListener {
            showDateTimePicker()
        }

        // Tombol simpan → update semua data ke Firestore
        binding.tombolSimpanEdit.setOnClickListener {
            lifecycleScope.launch {
                val title = binding.etTitle.text.toString().trim()
                val description = binding.etDescription.text.toString().trim()
                val location = binding.etLocation.text.toString().trim()
                val reminder = binding.cbReminder.isChecked
                val dateString = binding.etDate.text.toString().trim()

                if (title.isEmpty() || description.isEmpty() || location.isEmpty() || dateString.isEmpty()) {
                    showToast("Semua field harus diisi!")
                    return@launch
                }

                val parsedDate = try {
                    dateFormat.parse(dateString)
                } catch (e: Exception) {
                    null
                }

                if (parsedDate == null) {
                    showToast("Format tanggal/waktu tidak valid!")
                    return@launch
                }

                val updatedEvent = Event(
                    id = eventId,
                    title = title,
                    description = description,
                    location = location,
                    date = parsedDate,
                    reminder = reminder
                )

                try {
                    eventUseCase.updateEvent(updatedEvent)
                    showToast("Event berhasil diperbarui!")
                    backToEventList()
                } catch (e: Exception) {
                    showToast("Gagal memperbarui: ${e.message}")
                }
            }
        }
    }

    /** Menampilkan DatePicker lalu TimePicker berurutan */
    private fun showDateTimePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDateTime.set(Calendar.YEAR, year)
                selectedDateTime.set(Calendar.MONTH, month)
                selectedDateTime.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                        selectedDateTime.set(Calendar.MINUTE, minute)
                        binding.etDate.setText(dateFormat.format(selectedDateTime.time))
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /** Memuat data Event dari Firestore */
    private fun loadData() {
        if (eventId.isEmpty()) {
            showToast("ID Event tidak ditemukan")
            backToEventList()
            return
        }

        lifecycleScope.launch {
            try {
                val event = eventUseCase.getEventById(eventId)
                if (event != null) {
                    binding.etTitle.setText(event.title)
                    binding.etDescription.setText(event.description)
                    binding.etLocation.setText(event.location)
                    binding.cbReminder.isChecked = event.reminder
                    event.date?.let {
                        binding.etDate.setText(dateFormat.format(it))
                    }
                } else {
                    showToast("Event tidak ditemukan di server")
                    backToEventList()
                }
            } catch (e: Exception) {
                showToast("Gagal memuat data: ${e.message}")
            }
        }
    }

    private fun backToEventList() {
        startActivity(Intent(this, EventActivity::class.java))
        finish()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
