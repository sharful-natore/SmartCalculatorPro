package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Save
import com.example.util.AppLanguage
import android.widget.Toast
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import java.util.*

@Composable
fun AgeCalculatorCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Age Calculator",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    viewModel.ageDob = "$dayOfMonth/${month + 1}/$year"
                    viewModel.calculateAge()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Date of Birth Selector
            OutlinedTextField(
                value = viewModel.ageDob,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Birth", color = themeColors.displayText.copy(alpha = 0.7f), fontSize = 12.sp) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select Date",
                        tint = themeColors.buttonOperatorBg,
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText,
                    focusedBorderColor = themeColors.buttonOperatorBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { datePickerDialog.show() }
                    .testTag("age_dob_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Results Row (Years, Months, Days)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "Years",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                    Text(
                        text = viewModel.ageYearsResult,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.testTag("age_years_text")
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Months",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                    Text(
                        text = viewModel.ageMonthsResult,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.testTag("age_months_text")
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Days",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                    Text(
                        text = viewModel.ageDaysResult,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.testTag("age_days_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
            Button(
                onClick = {
                    val dob = viewModel.ageDob
                    val years = viewModel.ageYearsResult
                    val months = viewModel.ageMonthsResult
                    val days = viewModel.ageDaysResult
                    if (years != "0" && years.isNotEmpty() && years != "-") {
                        val expr = if (isBn) "জন্মতারিখ: $dob" else "DOB: $dob"
                        val result = if (isBn) "$years বছর $months মাস $days দিন" else "$years Y, $months M, $days D"
                        viewModel.saveToolResultToHistory("Age Calculator", expr, result)
                        Toast.makeText(context, if (isBn) "ফলাফল হিস্টোরিতে সেভ করা হয়েছে!" else "Saved to history!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, if (isBn) "অনুগ্রহ করে সঠিক জন্মতারিখ দিন" else "Please enter a valid date of birth", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                    contentColor = themeColors.buttonEqualBg
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBn) "ফলাফল হিস্টোরিতে রাখুন" else "Save Result to History", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = themeColors.displayText.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(16.dp))

            // Additional Info: Next Birthday, Which Birthday, Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Next Birthday",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = viewModel.ageNextBirthdayResult,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        modifier = Modifier.testTag("age_next_birthday_text")
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Upcoming",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = viewModel.ageWhichBirthdayResult,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.testTag("age_which_birthday_text")
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Countdown",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = viewModel.ageCountdownResult,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        modifier = Modifier.testTag("age_countdown_text")
                    )
                }
            }
        }
    }
}
