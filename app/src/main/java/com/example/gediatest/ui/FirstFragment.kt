package com.example.gediatest.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.gediatest.R
import com.example.gediatest.databinding.FragmentFirstBinding
import com.example.gediatest.sdk.PosHelper
import com.example.gediatest.sdk.PosPICCResultListener
import com.example.gediatest.sdk.PrinterUtils
import com.example.gediatest.sdk.PrinterUtils.printTransaction

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), PosPICCResultListener {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        PosHelper.startListenTOPiccCard(this)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            requireContext().printTransaction(){

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onScanResult(UID: String) {
        requireContext().printTransaction(){

        }
    }

    override fun onScanError(error: String) {

    }
}