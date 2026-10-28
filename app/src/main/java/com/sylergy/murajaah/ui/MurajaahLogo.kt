package com.sylergy.murajaah.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sylergy.murajaah.R

/**
 * Official Murajaah brand icon / favicon:
 * The square Kufic root (ر ج ع) in Forest Green on Warm Cream parchment.
 */
@Composable
fun MurajaahLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_murajaah_logo),
        contentDescription = "Murajaah",
        modifier = modifier.size(size)
    )
}
