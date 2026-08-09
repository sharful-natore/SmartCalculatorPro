package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel

fun getCategoryIcon(category: CalculatorViewModel.UnitCategory): ImageVector {
    return when (category) {
        CalculatorViewModel.UnitCategory.LENGTH -> Icons.Default.Straighten
        CalculatorViewModel.UnitCategory.WEIGHT -> Icons.Default.FitnessCenter
        CalculatorViewModel.UnitCategory.AREA -> Icons.Default.AspectRatio
        CalculatorViewModel.UnitCategory.VOLUME -> Icons.Default.Opacity
        CalculatorViewModel.UnitCategory.TEMPERATURE -> Icons.Default.Thermostat
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    var isFromDropdownExpanded by remember { mutableStateOf(false) }
    var isToDropdownExpanded by remember { mutableStateOf(false) }
    val availableUnits = viewModel.getUnitsForCategory(viewModel.unitCategory)

    val mainScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(mainScrollState)
            .padding(16.dp)
    ) {
        // 1. Horizontal Scroll Category Chips (Icon & text styled like Tools page)
        val chipScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipScrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorViewModel.UnitCategory.values().forEach { category ->
                val isSelected = viewModel.unitCategory == category
                val icon = getCategoryIcon(category)
                val label = category.label

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 90.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF6366F1) else themeColors.buttonNormalBg)
                        .clickable { viewModel.onUnitCategoryChange(category) }
                        .testTag("chip_${category.name.lowercase()}")
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else themeColors.unselectedItemText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else themeColors.unselectedItemText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. From Unit Panel Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "From Unit",
                        fontSize = 13.sp,
                        color = themeColors.displayExpressionText,
                        fontWeight = FontWeight.Bold
                    )

                    // Dropdown Trigger on the right side
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeColors.background)
                                .clickable { isFromDropdownExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("from_unit_dropdown")
                        ) {
                            Text(
                                text = viewModel.fromUnit,
                                color = themeColors.buttonNormalText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                tint = themeColors.buttonNormalText
                            )
                        }

                        DropdownMenu(
                            expanded = isFromDropdownExpanded,
                            onDismissRequest = { isFromDropdownExpanded = false },
                            modifier = Modifier.background(themeColors.cardBg)
                        ) {
                            availableUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(text = unit, color = themeColors.displayText) },
                                    onClick = {
                                        viewModel.fromUnit = unit
                                        viewModel.calculateConverter()
                                        isFromDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Numeric text field for user input underneath
                OutlinedTextField(
                    value = TextFieldValue(
                        text = viewModel.converterInput,
                        selection = TextRange(viewModel.converterInput.length)
                    ),
                    onValueChange = {
                        viewModel.converterInput = it.text
                        viewModel.calculateConverter()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("converter_input_field")
                )
            }
        }

        // 3. Swap Floating Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            FilledIconButton(
                onClick = { viewModel.swapUnits() },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("swap_units_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap Units",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 4. To Unit Panel Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = themeColors.cardBg),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "To Unit",
                        fontSize = 13.sp,
                        color = themeColors.displayExpressionText,
                        fontWeight = FontWeight.Bold
                    )

                    // Dropdown Trigger on the right side
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeColors.background)
                                .clickable { isToDropdownExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("to_unit_dropdown")
                        ) {
                            Text(
                                text = viewModel.toUnit,
                                color = themeColors.buttonNormalText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                tint = themeColors.buttonNormalText
                            )
                        }

                        DropdownMenu(
                            expanded = isToDropdownExpanded,
                            onDismissRequest = { isToDropdownExpanded = false },
                            modifier = Modifier.background(themeColors.cardBg)
                        ) {
                            availableUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(text = unit, color = themeColors.displayText) },
                                    onClick = {
                                        viewModel.toUnit = unit
                                        viewModel.calculateConverter()
                                        isToDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Editable OutlinedTextField for To Unit output
                OutlinedTextField(
                    value = TextFieldValue(
                        text = viewModel.converterOutput,
                        selection = TextRange(viewModel.converterOutput.length)
                    ),
                    onValueChange = {
                        viewModel.converterOutput = it.text
                        viewModel.calculateConverterReverse()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("converter_output_text")
                )
            }
        }
    }
}
