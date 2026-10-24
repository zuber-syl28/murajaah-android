package com.sylergy.murajaah.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sylergy.murajaah.widget.model.MurajaahWidgetData
import com.sylergy.murajaah.widget.model.MurajaahWidgetDataRepository

class MurajaahGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = MurajaahWidgetDataRepository.loadData(context)

        provideContent {
            MurajaahWidgetContent(context = context, data = data)
        }
    }

    companion object {
        suspend fun update(context: Context) {
            MurajaahGlanceWidget().updateAll(context)
        }
    }
}

@Composable
fun MurajaahWidgetContent(context: Context, data: MurajaahWidgetData) {
    val launchComponent = ComponentName(context.packageName, "com.sylergy.murajaah.MainActivity")

    val primaryGreen = Color(0xFF2E6F40)
    val darkForest = Color(0xFF2F4A3D)
    val warmParchment = Color(0xFFFBF8F1)
    val warmGold = Color(0xFF7A6B56)
    val cardBg = Color(0xFFFAF6EE)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(warmParchment))
            .cornerRadius(16.dp)
            .padding(14.dp)
            .clickable(actionStartActivity(launchComponent))
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // Top Bar Row: App Name & Active Plan Tag
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📖 مراجعة · Murajaah",
                    style = TextStyle(
                        color = ColorProvider(darkForest),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(Color(0xFFE8F0EC)))
                        .cornerRadius(6.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = data.activePlanName,
                        style = TextStyle(
                            color = ColorProvider(primaryGreen),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Main Card Section
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ColorProvider(cardBg))
                    .cornerRadius(10.dp)
                    .padding(10.dp)
            ) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    if (data.dueCount > 0) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${data.dueCount} Cards Due Today",
                                style = TextStyle(
                                    color = ColorProvider(darkForest),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = "${data.dueCount} بطاقات مستحقة",
                                style = TextStyle(
                                    color = ColorProvider(warmGold),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "Next: ${data.nextSurahEnglish} (${data.nextSurahArabic}) · Ayahs ${data.nextAyahRange}",
                            style = TextStyle(
                                color = ColorProvider(warmGold),
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    } else {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Caught Up · تم الوِرد ✓",
                                style = TextStyle(
                                    color = ColorProvider(primaryGreen),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "Your memory retention is secured. Take rest with zero streak pressure.",
                            style = TextStyle(
                                color = ColorProvider(warmGold),
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Launch Wird Button
            Button(
                text = if (data.dueCount > 0) "Start Wird · ابدأ الوِرد" else "Open Murajaah · فتح التطبيق",
                onClick = actionStartActivity(launchComponent),
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .cornerRadius(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(primaryGreen),
                    contentColor = ColorProvider(Color.White)
                )
            )
        }
    }
}

class MurajaahGlanceReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MurajaahGlanceWidget()
}
