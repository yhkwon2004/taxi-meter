package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareReceiptHelper {
    private val wonFormatter = NumberFormat.getNumberInstance(Locale.KOREA)
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)

    fun formatWon(amount: Int): String = "${wonFormatter.format(amount)}원"

    fun formatDuration(seconds: Long): String {
        val min = seconds / 60
        val sec = seconds % 60
        return if (min > 0) "${min}분 ${sec}초" else "${sec}초"
    }

    fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            String.format(Locale.KOREA, "%.2f km", meters / 1000.0)
        } else {
            String.format(Locale.KOREA, "%d m", meters.toInt())
        }
    }

    fun buildShareMessage(
        startTimeMillis: Long,
        endTimeMillis: Long,
        distanceMeters: Double,
        durationSeconds: Long,
        baseFare: Int,
        distanceFare: Int,
        timeFare: Int,
        surchargeFare: Int,
        tollFee: Int,
        totalFare: Int,
        surchargeDesc: String,
        passengerCount: Int,
        accountInfo: String = "",
        memo: String = ""
    ): String {
        val dateStr = dateFormat.format(Date(if (startTimeMillis > 0) startTimeMillis else System.currentTimeMillis()))
        val distanceStr = formatDistance(distanceMeters)
        val durationStr = formatDuration(durationSeconds)
        val perPerson = if (passengerCount > 0) (totalFare + passengerCount - 1) / passengerCount else totalFare

        val sb = StringBuilder()
        sb.append("🚕 [택시 이용 영수증 / 정산 안내]\n")
        sb.append("─────────────────────\n")
        sb.append("📅 탑승 일시: $dateStr\n")
        sb.append("⏱️ 주행 시간: $durationStr\n")
        sb.append("📏 주행 거리: $distanceStr\n")
        if (memo.isNotBlank()) {
            sb.append("📍 운행 구간: $memo\n")
        }
        sb.append("─────────────────────\n")
        sb.append("💰 요금 내역\n")
        sb.append("• 기본요금: ${formatWon(baseFare)}\n")
        if (distanceFare > 0) {
            sb.append("• 거리요금: ${formatWon(distanceFare)}\n")
        }
        if (timeFare > 0) {
            sb.append("• 지체/시간요금: ${formatWon(timeFare)}\n")
        }
        if (surchargeFare > 0) {
            sb.append("• 할증요금 ($surchargeDesc): ${formatWon(surchargeFare)}\n")
        }
        if (tollFee > 0) {
            sb.append("• 통행료/추가금: ${formatWon(tollFee)}\n")
        }
        sb.append("─────────────────────\n")
        sb.append("💳 총 결제 금액: ${formatWon(totalFare)}\n")

        if (passengerCount > 1) {
            sb.append("─────────────────────\n")
            sb.append("👥 1/N 더치페이 정산 ($passengerCount 명)\n")
            sb.append("👉 1인당 부담 금액: ${formatWon(perPerson)}\n")
        }

        if (accountInfo.isNotBlank()) {
            sb.append("─────────────────────\n")
            sb.append("🏦 입금 안내: $accountInfo\n")
        }

        sb.append("─────────────────────\n")
        sb.append("스마트 택시 미터기로 안전하게 정산되었습니다.")

        return sb.toString()
    }

    fun shareViaSns(context: Context, text: String, title: String = "택시 요금 영수증 공유") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("택시 요금 정산", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "정산 내역이 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }
}
