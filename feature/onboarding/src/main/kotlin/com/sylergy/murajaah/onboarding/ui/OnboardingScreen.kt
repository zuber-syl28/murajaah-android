package com.sylergy.murajaah.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.onboarding.OnboardingViewModel
import com.sylergy.murajaah.onboarding.model.OnboardingPresets
import com.sylergy.murajaah.onboarding.model.OnboardingStep
import com.sylergy.murajaah.quran.Narration

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F2))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step Dots Indicator
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OnboardingStep.entries.forEachIndexed { index, step ->
                val active = step.ordinal <= currentStep.ordinal
                val current = step == currentStep
                Box(
                    modifier = Modifier
                        .size(if (current) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                current -> Color(0xFF2F4A3D)
                                active -> Color(0xFF2F4A3D).copy(alpha = 0.5f)
                                else -> Color(0xFFD6D4CE)
                            }
                        )
                )
            }
        }

        AnimatedContent(
            targetState = currentStep,
            label = "OnboardingStepTransition",
            modifier = Modifier.weight(1f)
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep(onNext = { viewModel.nextStep() })
                    OnboardingStep.NARRATION_AND_SIZE -> NarrationAndSizeStep(viewModel)
                    OnboardingStep.MEMORIZED_RANGES -> MemorizedRangesStep(viewModel)
                    OnboardingStep.GOAL_AND_STYLE -> GoalAndStyleStep(
                        viewModel = viewModel,
                        isGenerating = isGenerating,
                        onComplete = { viewModel.completeOnboarding(onFinishOnboarding) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = "مراجعة",
        fontSize = 44.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2F4A3D)
    )
    Text(
        text = "Welcome to Murajaah",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF2F4A3D).copy(alpha = 0.85f)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "The Qur'an was revealed to be held firmly in the chest. Active blind recitation with modern spaced repetition keeps your hifdh intact.",
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        color = Color(0xFF555555),
        modifier = Modifier.padding(horizontal = 8.dp)
    )

    Spacer(modifier = Modifier.height(32.dp))

    // 3 Principles Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PillarItem(
                number = "١",
                title = "Blind Recall First",
                description = "Always recite first from memory with a live stopwatch before opening the Mushaf."
            )
            PillarItem(
                number = "٢",
                title = "FSRS-6 Intelligent Spacing",
                description = "Cards adapt to slipped verses, giving fragile surahs immediate reinforcement and solid ones longer intervals."
            )
            PillarItem(
                number = "٣",
                title = "ADHD-Friendly Daily Wird",
                description = "Celebrate what you accomplished. Stopping anytime is a complete outcome, never an abandoned one."
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D))
    ) {
        Text("Begin Setup →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PillarItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFEDECE6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F4A3D))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F4A3D))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF666666), lineHeight = 17.sp)
        }
    }
}

@Composable
private fun NarrationAndSizeStep(viewModel: OnboardingViewModel) {
    val selectedNarration by viewModel.selectedNarration.collectAsState()
    val selectedCardSize by viewModel.selectedCardSize.collectAsState()

    Text(
        text = "Recitation & Card Size",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2F4A3D)
    )
    Text(
        text = "Select your Qira'ah and preferred card length for revision.",
        fontSize = 13.sp,
        color = Color(0xFF666666),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Narration Selector
    Text(
        text = "QIRA'AH / NARRATION",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF888888),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OnboardingPresets.NARRATIONS.take(4).forEach { (narration, description) ->
                val isSelected = selectedNarration == narration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFFE8F0EC) else Color.Transparent)
                        .clickable { viewModel.selectNarration(narration) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF2F4A3D) else Color(0xFF333333)
                    )
                    if (isSelected) {
                        Text("✓", color = Color(0xFF2F4A3D), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Card Size
    Text(
        text = "CARD PASSAGE SIZE",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF888888),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SizeOptionCard(
            title = "10 Ayahs",
            subtitle = "Recommended",
            isSelected = selectedCardSize == "ten",
            onClick = { viewModel.selectCardSize("ten") },
            modifier = Modifier.weight(1f)
        )
        SizeOptionCard(
            title = "5 Ayahs",
            subtitle = "Shorter focus",
            isSelected = selectedCardSize == "five",
            onClick = { viewModel.selectCardSize("five") },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { viewModel.prevStep() },
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("← Back", color = Color(0xFF2F4A3D))
        }
        Button(
            onClick = { viewModel.nextStep() },
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D))
        ) {
            Text("Continue →")
        }
    }
}

@Composable
private fun SizeOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFE8F0EC) else Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF2F4A3D) else Color(0xFFDDDDDD),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F4A3D))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF777777))
        }
    }
}

@Composable
private fun MemorizedRangesStep(viewModel: OnboardingViewModel) {
    val selectedJuzSet by viewModel.selectedJuzSet.collectAsState()

    Text(
        text = "What Have You Memorized?",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2F4A3D)
    )
    Text(
        text = "Cards are generated exclusively from your memorized portions.",
        fontSize = 13.sp,
        color = Color(0xFF666666),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PRESET JUZ SELECTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF888888)
        )
        Text(
            text = "Select All",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4A3D),
            modifier = Modifier.clickable { viewModel.selectAllJuz() }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OnboardingPresets.PRESETS.forEach { preset ->
            val isSelected = selectedJuzSet.contains(preset.juzNumber)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleJuz(preset.juzNumber) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE8F0EC) else Color.White),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2F4A3D)) else null
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = preset.nameArabic,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = "· ${preset.nameEnglish}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF555555)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.surahsSummary,
                            fontSize = 12.sp,
                            color = Color(0xFF777777)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (isSelected) Color(0xFF2F4A3D) else Color(0xFFEDECE6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { viewModel.prevStep() },
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("← Back", color = Color(0xFF2F4A3D))
        }
        Button(
            onClick = { viewModel.nextStep() },
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D))
        ) {
            Text("Continue →")
        }
    }
}

@Composable
private fun GoalAndStyleStep(
    viewModel: OnboardingViewModel,
    isGenerating: Boolean,
    onComplete: () -> Unit
) {
    val dailyPages by viewModel.dailyPages.collectAsState()
    val revisionStyle by viewModel.revisionStyle.collectAsState()

    Text(
        text = "Daily Target & Style",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2F4A3D)
    )
    Text(
        text = "Set your pace to establish a sustainable revision habit.",
        fontSize = 13.sp,
        color = Color(0xFF666666),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Daily Pages Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAILY REVISION GOAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF888888)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEDECE6), CircleShape)
                        .clickable { viewModel.setDailyPages(dailyPages - 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F4A3D))
                }
                Text(
                    text = "$dailyPages Pages",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEDECE6), CircleShape)
                        .clickable { viewModel.setDailyPages(dailyPages + 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F4A3D))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "~${dailyPages * 3} minutes of focused recitation daily",
                fontSize = 12.sp,
                color = Color(0xFF777777)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Revision Style Selector
    Text(
        text = "REVISION ORDER",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF888888),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SizeOptionCard(
            title = "Ordered",
            subtitle = "Sequential Surahs",
            isSelected = revisionStyle == "ordered",
            onClick = { viewModel.setRevisionStyle("ordered") },
            modifier = Modifier.weight(1f)
        )
        SizeOptionCard(
            title = "Smart FSRS",
            subtitle = "Urgency first",
            isSelected = revisionStyle == "smart",
            onClick = { viewModel.setRevisionStyle("smart") },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { viewModel.prevStep() },
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("← Back", color = Color(0xFF2F4A3D))
        }
        Button(
            onClick = onComplete,
            enabled = !isGenerating,
            modifier = Modifier.weight(1.5f).height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D))
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Start Revising 🚀", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
