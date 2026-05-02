package com.example.tripline.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.tripline.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PdfShareBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_pdf_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.buttonPdfGenerate)?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "PDF 생성 요청을 준비했어요. 백엔드 연결 후 파일 공유까지 이어집니다.",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
    }
}
