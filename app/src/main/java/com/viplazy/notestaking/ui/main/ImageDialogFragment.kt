package com.viplazy.notestaking.ui.main

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.viplazy.notestaking.R
import kotlinx.android.synthetic.main.fragment_image_dialog.*

class ImageDialogFragment : DialogFragment() {
    private lateinit var navController: NavController
    private lateinit var imagesViewModel: ImagesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Picture_Dialog)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_image_dialog, container, false)
        (activity!! as AppCompatActivity).supportActionBar?.hide()
        activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        imagesViewModel =  ViewModelProviders.of(activity!!).get(ImagesViewModel::class.java)


        imagesViewModel.selectedBitmap.observe(viewLifecycleOwner, Observer {

            it?.let {
                img_info.setImageBitmap(it)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
    }

    private fun navigateUp() {
        imagesViewModel.selectedBitmap.postValue(null)
        navController.navigateUp()
    }

    override fun onResume() {
        super.onResume()

        view?.isFocusableInTouchMode = true
        view?.requestFocus()

        view?.setOnKeyListener( View.OnKeyListener { v, keyCode, event ->
            if( keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {

                navigateUp()
            }
            return@OnKeyListener true
        })

    }
}
