package com.viplazy.notestaking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.viplazy.notestaking.common.ListNoteAdapter
import com.viplazy.notestaking.ui.main.ListNotesFragment
import com.viplazy.notestaking.ui.main.NoteDetailFragment
import com.viplazy.notestaking.ui.main.NotesViewModel

class MainActivity : AppCompatActivity() {

    private var mBackPressed: Long = 0
    private val TIME_INTERVAL = 2000



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
    }


    override fun onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()

            finish()
            return
        } else {
            Toast.makeText(baseContext, "Tap back button in order to exit", Toast.LENGTH_SHORT)
                .show()
        }
        mBackPressed = System.currentTimeMillis()
    }
}
