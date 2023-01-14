package com.flydog.connectanya.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.flydog.connectanya.databinding.FragmentHomeBinding
import com.flydog.connectanya.ui.MainViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val viewModel: MainViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        val clipboardNowData = binding.clipboardNowData.clipboardData
        viewModel.currentClipboardData.observe(viewLifecycleOwner) {
            clipboardNowData.text = it
        }
        val clipboardNowDate = binding.clipboardNowData.clipboardDate
        clipboardNowDate.text = "当前剪切板"
        clipboardNowDate.setTextColor(Color.RED)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}