package com.example.ui.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.CharacterCustomizationState

/**
 * 1. Slot lapisan pada sistem avatar berlapis "Nano".
 * Setiap slot hanya boleh memiliki 1 item aktif pada satu waktu.
 * Jika pemain memilih item baru pada slot yang sama, item lama otomatis tergantikan.
 */
enum class AvatarSlot(val defaultZOrder: Int) {
    BODY(0),
    UNIFORM(1),
    EXPRESSION(2),
    HEAD(3),
    HEADSET(4),
    TOOL(5)
}

/**
 * 2. Enam ekspresi wajah visor "Nano" yang dipakai bersama oleh seluruh 9 peran spesialis.
 */
enum class Expression {
    NETRAL,
    SENANG,
    BERPIKIR,
    WASPADA,
    BANGGA,
    SEDIH
}

/**
 * 3. Titik jangkar (anchor) dalam bentuk fraksi (0f..1f) terhadap kanvas dasar 1024x1024.
 * Dengan memakai fraksi (bukan piksel tetap), posisi item selalu pas di semua ukuran layar HP/tablet.
 *
 * @param x Fraksi titik tengah horizontal (0.5f = tepat di tengah).
 * @param y Fraksi titik tengah vertikal (0.5f = tepat di tengah).
 * @param sizeFraction Fraksi ukuran kotak layer terhadap kotak avatar utama (1.0f = sama besar dengan kotak utama).
 */
data class AvatarAnchor(
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val sizeFraction: Float = 1.0f
)

/**
 * 4. Data satu item layer pada avatar berlapis "Nano".
 */
data class AvatarItem(
    val id: String,
    val slot: AvatarSlot,
    val drawableRes: Int?,
    val anchor: AvatarAnchor = AvatarAnchor(0.5f, 0.5f, 1.0f),
    val zOrder: Int = slot.defaultZOrder,
    val lockedByNode: String? = null
)

/**
 * Katalog layer untuk memetakan ID item kustomisasi ke layer gambar PNG transparan (drawableRes),
 * posisi anchor fraksi (0f..1f), urutan tumpukan (zOrder), serta atribut bawaan per peran.
 */
object AvatarLayerRegistry {

    // Anchor khusus untuk kanvas ekspresi 512x512 agar jatuh tepat di tengah kaca visor Nano (512, 385 pada 1024x1024)
    val VISOR_EXPRESSION_ANCHOR = AvatarAnchor(
        x = 0.50f,
        y = 0.376f,
        sizeFraction = 0.50f
    )

    /**
     * Atribut bawaan (default loadout) untuk masing-masing dari 9 peran spesialis.
     * Topi, kacamata, seragam, headset, dan alat bawaan dipindahkan menjadi item slot
     * supaya pemain bebas mengganti atau melepasnya kapan saja!
     */
    fun defaultLoadoutForRole(
        avatarId: String,
        currentBackgroundId: String = "bg_control_room"
    ): CharacterCustomizationState {
        return when (avatarId) {
            "radiografer_mri" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_default",
                equippedUniform = "uniform_scrub_mint",
                equippedHeadset = "headset_radio",
                equippedAccessory = "acc_magnet_detector",
                backgroundId = currentBackgroundId
            )
            "radiolog" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_visor",
                equippedUniform = "uniform_standard",
                equippedHeadset = "headset_none",
                equippedAccessory = "acc_clipboard",
                backgroundId = currentBackgroundId
            )
            "ahli_neuro_mri" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_crown_rf",
                equippedUniform = "uniform_scrub_blue",
                equippedHeadset = "headset_pneumatic",
                equippedAccessory = "acc_coil_surface",
                backgroundId = currentBackgroundId
            )
            "fisikawan_medis" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_goggles",
                equippedUniform = "uniform_standard",
                equippedHeadset = "headset_holo",
                equippedAccessory = "acc_phantom",
                backgroundId = currentBackgroundId
            )
            "petugas_proteksi_radiasi" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_default",
                equippedUniform = "uniform_apron_shield",
                equippedHeadset = "headset_holo",
                equippedAccessory = "acc_magnet_detector",
                backgroundId = currentBackgroundId
            )
            "ahli_jantung" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_visor",
                equippedUniform = "uniform_scrub_teal",
                equippedHeadset = "headset_neural",
                equippedAccessory = "acc_oximeter",
                backgroundId = currentBackgroundId
            )
            "perawat" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_cap",
                equippedUniform = "uniform_scrub_teal",
                equippedHeadset = "headset_none",
                equippedAccessory = "acc_oximeter",
                backgroundId = currentBackgroundId
            )
            "dokter_bedah" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_cap",
                equippedUniform = "uniform_scrub_blue",
                equippedHeadset = "headset_none",
                equippedAccessory = "acc_badge_zona4",
                backgroundId = currentBackgroundId
            )
            "dokter_robot" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_cyber",
                equippedUniform = "uniform_armor",
                equippedHeadset = "headset_holo",
                equippedAccessory = "acc_clipboard",
                backgroundId = currentBackgroundId
            )
            "dosen" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_mortarboard",
                equippedUniform = "uniform_dosen",
                equippedHeadset = "headset_lecture",
                equippedAccessory = "acc_laser_pointer",
                backgroundId = currentBackgroundId
            )
            "mahasiswa" -> CharacterCustomizationState(
                avatarId = avatarId,
                equippedHair = "hair_beanie_mri",
                equippedUniform = "uniform_mahasiswa",
                equippedHeadset = "headset_comm_pro",
                equippedAccessory = "acc_handbook_mri",
                backgroundId = currentBackgroundId
            )
            else -> CharacterCustomizationState(
                avatarId = avatarId,
                backgroundId = currentBackgroundId
            )
        }
    }

    fun getBodyLayer(): AvatarItem = AvatarItem(
        id = "nano_body_base",
        slot = AvatarSlot.BODY,
        drawableRes = R.drawable.nano_body_base,
        anchor = AvatarAnchor(0.5f, 0.5f, 1.0f),
        zOrder = AvatarSlot.BODY.defaultZOrder
    )

    fun getExpressionLayer(expression: Expression): AvatarItem {
        val resId = when (expression) {
            Expression.NETRAL -> R.drawable.nano_exp_netral
            Expression.SENANG -> R.drawable.nano_exp_senang
            Expression.BERPIKIR -> R.drawable.nano_exp_berpikir
            Expression.WASPADA -> R.drawable.nano_exp_waspada
            Expression.BANGGA -> R.drawable.nano_exp_bangga
            Expression.SEDIH -> R.drawable.nano_exp_sedih
        }
        return AvatarItem(
            id = "exp_${expression.name.lowercase()}",
            slot = AvatarSlot.EXPRESSION,
            drawableRes = resId,
            anchor = VISOR_EXPRESSION_ANCHOR,
            zOrder = AvatarSlot.EXPRESSION.defaultZOrder
        )
    }

    fun getHeadLayer(headId: String): AvatarItem {
        val (drawableRes, lockedNode) = when (headId) {
            "hair_default" -> R.drawable.nano_head_larmor to null
            "hair_visor" -> R.drawable.nano_head_visor to "m01"
            "hair_cap" -> R.drawable.nano_head_cap to "m02"
            "hair_goggles" -> R.drawable.nano_head_goggles to "m03a"
            "hair_cyber" -> R.drawable.nano_head_cyber to "m05"
            "hair_crown_rf" -> R.drawable.nano_head_coil64 to "m07"
            "hair_mortarboard" -> R.drawable.nano_head_mortarboard to null
            "hair_beanie_mri" -> R.drawable.nano_head_beanie_mri to null
            "hair_loupe" -> R.drawable.nano_head_loupe to null
            else -> R.drawable.nano_head_larmor to null
        }
        return AvatarItem(
            id = headId,
            slot = AvatarSlot.HEAD,
            drawableRes = drawableRes,
            anchor = AvatarAnchor(0.5f, 0.5f, 1.0f),
            zOrder = AvatarSlot.HEAD.defaultZOrder,
            lockedByNode = lockedNode
        )
    }

    fun getUniformLayer(uniformId: String): AvatarItem {
        val (drawableRes, lockedNode) = when (uniformId) {
            "uniform_scrub_mint" -> R.drawable.nano_uniform_scrub_mint to null
            "uniform_standard" -> R.drawable.nano_uniform_labcoat to "m01"
            "uniform_scrub_blue" -> R.drawable.nano_uniform_scrub_blue to "m03b"
            "uniform_scrub_teal" -> R.drawable.nano_uniform_scrub_teal to "m04"
            "uniform_apron_shield" -> R.drawable.nano_uniform_vest_mrso to "m09"
            "uniform_armor" -> R.drawable.nano_uniform_cryo to "m05"
            "uniform_dosen" -> R.drawable.nano_uniform_dosen to null
            "uniform_mahasiswa" -> R.drawable.nano_uniform_mahasiswa to null
            "uniform_leadfree" -> R.drawable.nano_uniform_leadfree to null
            else -> R.drawable.nano_uniform_scrub_mint to null
        }
        return AvatarItem(
            id = uniformId,
            slot = AvatarSlot.UNIFORM,
            drawableRes = drawableRes,
            anchor = AvatarAnchor(0.5f, 0.5f, 1.0f),
            zOrder = AvatarSlot.UNIFORM.defaultZOrder,
            lockedByNode = lockedNode
        )
    }

    fun getHeadsetLayer(headsetId: String): AvatarItem {
        val (drawableRes, lockedNode) = when (headsetId) {
            "headset_none" -> null to null
            "headset_radio" -> R.drawable.nano_headset_radio to null
            "headset_neural" -> R.drawable.nano_headset_intercom to "m06"
            "headset_pneumatic" -> R.drawable.nano_headset_pneumatic to "m08"
            "headset_holo" -> R.drawable.nano_headset_sar to "m09"
            "headset_lecture" -> R.drawable.nano_headset_lecture to null
            "headset_comm_pro" -> R.drawable.nano_headset_comm_pro to null
            else -> R.drawable.nano_headset_radio to null
        }
        return AvatarItem(
            id = headsetId,
            slot = AvatarSlot.HEADSET,
            drawableRes = drawableRes,
            anchor = AvatarAnchor(0.5f, 0.5f, 1.0f),
            zOrder = AvatarSlot.HEADSET.defaultZOrder,
            lockedByNode = lockedNode
        )
    }

    fun getToolLayer(toolId: String): AvatarItem {
        val (drawableRes, lockedNode) = when (toolId) {
            "acc_magnet_detector" -> R.drawable.nano_tool_detector to null
            "acc_badge_zona4" -> R.drawable.nano_tool_badge to "m01"
            "acc_phantom" -> R.drawable.nano_tool_phantom to "m08"
            "acc_clipboard" -> R.drawable.nano_tool_tablet to "m02"
            "acc_oximeter" -> R.drawable.nano_tool_oximeter to "m09"
            "acc_coil_surface" -> R.drawable.nano_tool_coil to "m07"
            "acc_laser_pointer" -> R.drawable.nano_tool_pointer to null
            "acc_handbook_mri" -> R.drawable.nano_tool_handbook to null
            else -> R.drawable.nano_tool_detector to null
        }
        return AvatarItem(
            id = toolId,
            slot = AvatarSlot.TOOL,
            drawableRes = drawableRes,
            anchor = AvatarAnchor(0.5f, 0.5f, 1.0f),
            zOrder = AvatarSlot.TOOL.defaultZOrder,
            lockedByNode = lockedNode
        )
    }

    /**
     * Menyusun daftar layer aktif (1 item per slot) yang diurutkan berdasarkan zOrder dari belakang ke depan.
     */
    fun buildActiveLayers(
        equipped: CharacterCustomizationState,
        expression: Expression
    ): List<AvatarItem> {
        val layers = mutableListOf(
            getBodyLayer(),
            getUniformLayer(equipped.equippedUniform),
            getExpressionLayer(expression),
            getHeadLayer(equipped.equippedHair),
            getToolLayer(equipped.equippedAccessory)
        )
        // Bila memilih "Tanpa Headset" (headset_none), slot HEADSET dikosongkan sama sekali
        if (equipped.equippedHeadset != "headset_none") {
            layers.add(getHeadsetLayer(equipped.equippedHeadset))
        }
        return layers.sortedBy { it.zOrder }
    }
}

/**
 * 5. Composable utama untuk menampilkan karakter berlapis "Nano".
 * Menumpuk setiap layer PNG transparan sesuai urutan zOrder di dalam kotak berukuran sama,
 * dengan penempatan berbasis fraksi anchor (0f..1f).
 */
@Composable
fun AvatarView(
    role: String,
    equipped: CharacterCustomizationState,
    expression: Expression,
    modifier: Modifier = Modifier
) {
    val orderedLayers = remember(equipped, expression) {
        AvatarLayerRegistry.buildActiveLayers(equipped, expression)
    }

    BoxWithConstraints(modifier = modifier) {
        val boxSide = if (maxWidth < maxHeight) maxWidth else maxHeight

        orderedLayers.forEach { layer ->
            val drawableRes = layer.drawableRes
            if (drawableRes != null) {
                val layerSize = boxSide * layer.anchor.sizeFraction
                val offsetX = (boxSide * layer.anchor.x) - (layerSize / 2f)
                val offsetY = (boxSide * layer.anchor.y) - (layerSize / 2f)

                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = "${role}_${layer.id}",
                    modifier = Modifier
                        .size(layerSize)
                        .offset(x = offsetX, y = offsetY),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
