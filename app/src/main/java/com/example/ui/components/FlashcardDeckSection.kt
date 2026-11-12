package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MriFlashcard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavySlate
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun FlashcardDeckSection(
    cards: List<MriFlashcard>,
    currentIndex: Int,
    isFlipped: Boolean,
    shuffleSeed: Int,
    masteredCardIds: Set<String>,
    isCardBookmarked: (MriFlashcard) -> Boolean,
    onToggleBookmark: (MriFlashcard) -> Unit,
    onFlip: () -> Unit,
    onPrev: (Int) -> Unit,
    onNext: (Int) -> Unit,
    onShuffle: () -> Unit,
    onToggleMastered: (String) -> Unit,
    emptyMessage: String = "Tidak ada kartu hafalan untuk filter saat ini."
) {
    val deck = remember(cards, shuffleSeed) {
        if (shuffleSeed == 0) cards else cards.shuffled(Random(shuffleSeed))
    }

    if (deck.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = emptyMessage,
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, deck.size - 1)
    val currentCard = deck[safeIndex]
    val isMastered = masteredCardIds.contains(currentCard.id)
    val bookmarked = isCardBookmarked(currentCard)
    val masteredInDeck = deck.count { masteredCardIds.contains(it.id) }
    val progress = if (deck.isNotEmpty()) masteredInDeck.toFloat() / deck.size.toFloat() else 0f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Progress & Shuffle Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Progres Hafalan Cepat",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$masteredInDeck dari ${deck.size} kartu dikuasai • Kartu ${safeIndex + 1}/${deck.size}",
                            fontSize = 11.sp,
                            color = CyanBright
                        )
                    }

                    OutlinedButton(
                        onClick = onShuffle,
                        modifier = Modifier.testTag("flashcard_shuffle_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DeepObsidian,
                            contentColor = CyanBright
                        ),
                        border = BorderStroke(1.dp, CyanPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Acak Kartu",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Acak", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = EmeraldSuccess,
                    trackColor = DeepObsidian
                )
            }
        }

        // Interactive Flip Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 250.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onFlip)
                .testTag("flashcard_flip_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFlipped) CardSlate else DarkNavySlate
            ),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (isFlipped) CyanBright else if (isMastered) EmeraldSuccess else CardSlateBorder
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Card Top Header: Category Badge, Node Code, and Bookmark Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isFlipped) CyanPrimary.copy(alpha = 0.2f) else CyanDark,
                            border = BorderStroke(1.dp, CyanPrimary)
                        ) {
                            Text(
                                text = currentCard.nodeCode,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DeepObsidian,
                            border = BorderStroke(1.dp, CardSlateBorder)
                        ) {
                            Text(
                                text = if (isFlipped) "JAWABAN & ANALOGI" else currentCard.category.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFlipped) AmberAccent else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    AnimatedBookmarkPill(
                        isBookmarked = bookmarked,
                        onToggleBookmark = { onToggleBookmark(currentCard) },
                        testTag = "flashcard_bookmark_button",
                        savedContentDescription = "Hapus Bookmark",
                        unsavedContentDescription = "Simpan Bookmark"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Animated Card Body (Front vs Back)
                AnimatedContent(
                    targetState = isFlipped,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "flashcard_flip_transition"
                ) { flipped ->
                    if (!flipped) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentCard.frontTitle,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DeepObsidian,
                                border = BorderStroke(1.dp, CardSlateBorder)
                            ) {
                                Text(
                                    text = currentCard.frontSubtitle,
                                    fontSize = 12.sp,
                                    color = CyanBright,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = currentCard.frontTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright
                            )
                            Text(
                                text = currentCard.backContent,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = CyanDark.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = currentCard.clinicalAnalogyOrTip,
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = CyanBright,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Tap Hint & Mastered Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isFlipped) "Ketuk untuk kembali ke pertanyaan" else "Ketuk kartu untuk melihat jawaban",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    if (isMastered) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldDark,
                            border = BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = "✓ Dikuasai",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Navigation & Mastery Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onPrev(deck.size) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("flashcard_prev_button"),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkNavySlate,
                    contentColor = TextPrimary
                ),
                border = BorderStroke(1.dp, CardSlateBorder)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Sebelumnya",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sebelumnya",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Button(
                onClick = { onToggleMastered(currentCard.id) },
                modifier = Modifier
                    .weight(1.15f)
                    .testTag("flashcard_mastered_button"),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMastered) EmeraldDark else AmberDark,
                    contentColor = if (isMastered) EmeraldSuccess else AmberAccent
                ),
                border = BorderStroke(1.dp, if (isMastered) EmeraldSuccess else AmberAccent)
            ) {
                Icon(
                    imageVector = if (isMastered) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isMastered) "Sudah Hafal" else "Tandai Hafal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            OutlinedButton(
                onClick = { onNext(deck.size) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("flashcard_next_button"),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkNavySlate,
                    contentColor = CyanBright
                ),
                border = BorderStroke(1.dp, CyanPrimary)
            ) {
                Text(
                    text = "Berikutnya",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Berikutnya",
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun StudyModeToggleBar(
    primaryLabel: String,
    flashcardLabel: String,
    isFlashcardMode: Boolean,
    onSelectMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkNavySlate)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (!isFlashcardMode) CyanPrimary else DeepObsidian)
                .clickable { onSelectMode(false) }
                .padding(vertical = 10.dp)
                .testTag("mode_list_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = primaryLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isFlashcardMode) DeepObsidian else TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isFlashcardMode) CyanPrimary else DeepObsidian)
                .clickable { onSelectMode(true) }
                .padding(vertical = 10.dp)
                .testTag("mode_flashcard_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = flashcardLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFlashcardMode) DeepObsidian else CyanBright
            )
        }
    }
}

@Composable
fun AnimatedBookmarkPill(
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    testTag: String,
    savedContentDescription: String = "Hapus Bookmark Materi",
    unsavedContentDescription: String = "Simpan Bookmark Materi",
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bookmark_press_scale"
    )

    val pulseScale = remember { Animatable(1f) }
    val iconPopScale = remember { Animatable(1f) }
    val iconShakeRotation = remember { Animatable(0f) }
    val iconHopOffsetY = remember { Animatable(0f) }
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(isBookmarked) {
        if (hasInitialized) {
            // Pill soft breathing pulse
            launch {
                pulseScale.animateTo(
                    targetValue = if (isBookmarked) 1.06f else 0.96f,
                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
                )
                pulseScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = 280f
                    )
                )
            }
            // Smooth icon 'pop'
            launch {
                iconPopScale.animateTo(
                    targetValue = if (isBookmarked) 1.24f else 0.86f,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                )
                iconPopScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.62f,
                        stiffness = 250f
                    )
                )
            }
            // Gentle pendulum 'shake' on icon
            launch {
                if (isBookmarked) {
                    iconShakeRotation.animateTo(
                        targetValue = -9f,
                        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing)
                    )
                    iconShakeRotation.animateTo(
                        targetValue = 7f,
                        animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing)
                    )
                } else {
                    iconShakeRotation.animateTo(
                        targetValue = 7f,
                        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing)
                    )
                    iconShakeRotation.animateTo(
                        targetValue = -5f,
                        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
                    )
                }
                iconShakeRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.68f,
                        stiffness = 230f
                    )
                )
            }
            // Subtle vertical lift on icon
            launch {
                iconHopOffsetY.animateTo(
                    targetValue = if (isBookmarked) -2.0f else 1.5f,
                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
                )
                iconHopOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.70f,
                        stiffness = 250f
                    )
                )
            }
        } else {
            hasInitialized = true
        }
    }

    val containerColor by animateColorAsState(
        targetValue = if (isBookmarked) AmberDark else CardSlate,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "bookmark_pill_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isBookmarked) AmberAccent else CardSlateBorder,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "bookmark_pill_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isBookmarked) AmberAccent else TextSecondary,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "bookmark_pill_content"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                val combinedScale = pulseScale.value * pressScale
                scaleX = combinedScale
                scaleY = combinedScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleBookmark()
                }
            )
            .testTag(testTag),
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(12.dp)
                    .graphicsLayer {
                        scaleX = iconPopScale.value
                        scaleY = iconPopScale.value
                        rotationZ = iconShakeRotation.value
                        translationY = iconHopOffsetY.value
                    }
            ) {
                Crossfade(
                    targetState = isBookmarked,
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                    label = "bookmark_icon_crossfade"
                ) { saved ->
                    Icon(
                        imageVector = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (saved) savedContentDescription else unsavedContentDescription,
                        tint = contentColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            AnimatedContent(
                targetState = isBookmarked,
                transitionSpec = {
                    (fadeIn(tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
                        slideInVertically(tween(durationMillis = 220, easing = FastOutSlowInEasing)) { it / 4 }) togetherWith
                        (fadeOut(tween(durationMillis = 160, easing = FastOutSlowInEasing)) +
                            slideOutVertically(tween(durationMillis = 160, easing = FastOutSlowInEasing)) { -it / 4 }) using
                        SizeTransform(clip = false)
                },
                label = "bookmark_label_anim"
            ) { saved ->
                Text(
                    text = if (saved) "Tersimpan" else "Simpan",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}



