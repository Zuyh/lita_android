package com.example.lita.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lita.R

private val Ibmplexsansjp = FontFamily (
    Font(R.font.ibmplexsansjp_regular, weight = FontWeight.Normal),
    Font(R.font.ibmplexsansjp_light, weight = FontWeight.Light),
    Font(R.font.ibmplexsansjp_medium, weight = FontWeight.Bold)
)

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = Ibmplexsansjp,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    caption = TextStyle(
        fontFamily = Ibmplexsansjp,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        color = Color.Gray
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    */
)