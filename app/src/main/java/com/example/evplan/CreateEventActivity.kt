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
import com.example.evplan.databinding.ActivityCreateEventBinding
import com.example.evplan.entity.Event
import com.example.evplan.usecases.EventUseCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var eventUseCase: EventUseCase

    // Gunakan Calendar agar mudah diatur tanggal dan waktu
    private var selectedDateTime: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventUseCase = EventUseCase()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
    }

    private fun setupListeners() {
        // Tombol pilih tanggal
        binding.btnPickDate.setOnClickListener { showDatePicker() }

        // Tombol pilih waktu
        binding.btnPickTime.setOnClickListener { showTimePicker() }

        // Tombol simpan event
        binding.tombolSimpanEvent.setOnClickListener { saveEventToFirestore() }
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                if (selectedDateTime == null) selectedDateTime = Calendar.getInstance()
                selectedDateTime!!.set(Calendar.YEAR, year)
                selectedDateTime!!.set(Calendar.MONTH, month)
                selectedDateTime!!.set(Calendar.DAY_OF_MONTH, day)

                // Format tanggal
                val formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    .format(selectedDateTime!!.time)
                binding.tvSelectedDate.text = formattedDate
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                if (selectedDateTime == null) selectedDateTime = Calendar.getInstance()
                selectedDateTime!!.set(Calendar.HOUR_OF_DAY, hour)
                selectedDateTime!!.set(Calendar.MINUTE, minute)
                selectedDateTime!!.set(Calendar.SECOND, 0)

                // Format waktu
                val formattedTime = String.format("%02d:%02d", hour, minute)
                binding.tvSelectedTime.text = formattedTime
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveEventToFirestore() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Judul dan deskripsi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDateTime == null) {
            Toast.makeText(this, "Silakan pilih tanggal dan waktu terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        val event = Event(
            id = "",
            title = title,
            description = description,
            date = selectedDateTime!!.time
        )

        lifecycleScope.launch {
            try {
                eventUseCase.createEvent(event)
                Toast.makeText(this@CreateEventActivity, "Sukses menambah event planner", Toast.LENGTH_SHORT).show()
                toEventListPage()
            } catch (exc: Exception) {
                Toast.makeText(this@CreateEventActivity, "Gagal: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toEventListPage() {
        val intent = Intent(this, EventActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
