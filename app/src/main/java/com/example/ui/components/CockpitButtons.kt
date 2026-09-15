package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.MeterShapes
import com.example.ui.theme.MeterSizes
import com.example.ui.theme.Space
import com.example.ui.theme.TextPrimary

/**
 * The cockpit's action keys. Two variants only — a gradient-filled key for the
 * one primary action of a state, and an outlined key for everything secondary —
 * so the screen never shows two equally loud buttons.
 */

private val PrimaryLabelStyle = TextStyle(fontWeight = FontWeight.Black, fontSize = 18.sp)
private val SecondaryLabelStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)

@Composable
fun MeterFilledButton(
    label: String,
    icon: ImageVector,
    gradient: List<Color>,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = MeterSizes.secondaryButton,
    labelStyle: TextStyle = SecondaryLabelStyle,
    iconSize: Dp = 20.dp
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            // Colour-matched shadow: the key looks lit from within, the way a
            // backlit dashboard button spills onto the panel around it.
            .shadow(
                elevation = 12.dp,
                shape = MeterShapes.button,
                ambientColor = gradient.last(),
                spotColor = gradient.last()
            )
            .clip(MeterShapes.button)
            .background(Brush.verticalGradient(gradient)),
        shape = MeterShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = Space.xl)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(iconSize))
        Spacer(modifier = Modifier.width(Space.md))
        Text(
            text = label,
            style = labelStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MeterOutlinedButton(
    label: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = TextPrimary,
    borderColor: Color = CockpitCardBorder,
    containerColor: Color = CockpitSurface,
    height: Dp = MeterSizes.tertiaryButton,
    labelStyle: TextStyle = SecondaryLabelStyle,
    iconSize: Dp = 18.dp,
    iconTint: Color = contentColor
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = MeterShapes.button,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(MeterSizes.hairline, borderColor),
        contentPadding = PaddingValues(horizontal = Space.lg)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(Space.sm))
        }
        Text(
            text = label,
            style = labelStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Style handle for the single loudest button on a screen. */
val MeterPrimaryLabelStyle: TextStyle = PrimaryLabelStyle
