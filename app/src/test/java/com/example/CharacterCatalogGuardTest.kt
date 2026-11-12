package com.example

import com.example.data.CharacterCatalog
import com.example.data.CharacterCategory
import com.example.data.MriRepository
import com.example.ui.character.AvatarLayerRegistry
import com.example.ui.character.Expression
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterCatalogGuardTest {

    private fun drawableDir(): File? =
        listOf("src/main/res/drawable", "app/src/main/res/drawable")
            .map { File(it) }
            .firstOrNull { it.isDirectory }

    @Test
    fun allCatalogItemIdsAreUniqueAndComplete() {
        val allItems = CharacterCatalog.allItems
        val ids = allItems.map { it.id }
        val uniqueIds = ids.toSet()
        assertEquals("Seluruh ID item di CharacterCatalog wajib unik", ids.size, uniqueIds.size)

        allItems.forEach { item ->
            assertTrue("Nama item ${item.id} tidak boleh kosong", item.name.isNotBlank())
            assertTrue("Deskripsi item ${item.id} tidak boleh kosong", item.description.isNotBlank())
            assertTrue("Manfaat klinis item ${item.id} tidak boleh kosong", item.clinicalBenefit.isNotBlank())
        }
    }

    @Test
    fun allWearableItemsHaveValidLayerRenderer() {
        CharacterCatalog.hairItems.forEach { item ->
            assertNotNull("Item kepala ${item.id} wajib punya drawableRes", item.drawableRes)
            assertNotNull(
                "AvatarLayerRegistry.getHeadLayer(${item.id}) wajib punya drawableRes",
                AvatarLayerRegistry.getHeadLayer(item.id).drawableRes
            )
        }

        CharacterCatalog.uniformItems.forEach { item ->
            assertNotNull("Item seragam ${item.id} wajib punya drawableRes", item.drawableRes)
            assertNotNull(
                "AvatarLayerRegistry.getUniformLayer(${item.id}) wajib punya drawableRes",
                AvatarLayerRegistry.getUniformLayer(item.id).drawableRes
            )
        }

        CharacterCatalog.headsetItems.forEach { item ->
            if (item.id == "headset_none") {
                assertNull("headset_none harus mengosongkan drawableRes", item.drawableRes)
                assertNull(
                    "AvatarLayerRegistry.getHeadsetLayer(headset_none) harus null",
                    AvatarLayerRegistry.getHeadsetLayer(item.id).drawableRes
                )
            } else {
                assertNotNull("Item headset ${item.id} wajib punya drawableRes", item.drawableRes)
                assertNotNull(
                    "AvatarLayerRegistry.getHeadsetLayer(${item.id}) wajib punya drawableRes",
                    AvatarLayerRegistry.getHeadsetLayer(item.id).drawableRes
                )
            }
        }

        CharacterCatalog.accessoryItems.forEach { item ->
            assertNotNull("Item alat klinis ${item.id} wajib punya drawableRes", item.drawableRes)
            assertNotNull(
                "AvatarLayerRegistry.getToolLayer(${item.id}) wajib punya drawableRes",
                AvatarLayerRegistry.getToolLayer(item.id).drawableRes
            )
        }
    }

    @Test
    fun allElevenSpecialistRolesHaveValidProfilesAndDefaultLoadouts() {
        val avatars = MriRepository.avatars
        assertEquals("Harus ada 11 peran spesialis", 11, avatars.size)

        val mrsoAvatar = avatars.find { it.id == "petugas_proteksi_radiasi" }
        assertNotNull("Peran petugas_proteksi_radiasi harus ada", mrsoAvatar)
        assertEquals("Label petugas keselamatan MRI harus MRSO (bukan PPR)", "MRSO", mrsoAvatar?.label)
        assertEquals("Kategori RAMBUT harus berjudul Kepala", "Kepala", CharacterCategory.RAMBUT.title)

        val hairIds = CharacterCatalog.hairItems.map { it.id }.toSet()
        val uniformIds = CharacterCatalog.uniformItems.map { it.id }.toSet()
        val headsetIds = CharacterCatalog.headsetItems.map { it.id }.toSet()
        val accessoryIds = CharacterCatalog.accessoryItems.map { it.id }.toSet()

        avatars.forEach { avatar ->
            val profile = CharacterCatalog.specialistProfiles[avatar.id]
            assertNotNull("Profil spesialis untuk ${avatar.id} wajib tersedia", profile)
            assertTrue("Sapaan untuk ${avatar.id} minimal 4 kalimat", (profile?.greetings?.size ?: 0) >= 4)

            val loadout = AvatarLayerRegistry.defaultLoadoutForRole(avatar.id)
            assertTrue("Default head ${loadout.equippedHair} untuk ${avatar.id} harus valid", loadout.equippedHair in hairIds)
            assertTrue("Default uniform ${loadout.equippedUniform} untuk ${avatar.id} harus valid", loadout.equippedUniform in uniformIds)
            assertTrue("Default headset ${loadout.equippedHeadset} untuk ${avatar.id} harus valid", loadout.equippedHeadset in headsetIds)
            assertTrue("Default accessory ${loadout.equippedAccessory} untuk ${avatar.id} harus valid", loadout.equippedAccessory in accessoryIds)
        }
    }

    @Test
    fun allSixExpressionsAndNanoLayerDrawablesExistOnDisk() {
        Expression.entries.forEach { exp ->
            val layer = AvatarLayerRegistry.getExpressionLayer(exp)
            assertNotNull("Ekspresi $exp wajib memiliki drawableRes", layer.drawableRes)
        }

        val dir = requireNotNull(drawableDir()) { "Folder res/drawable tidak ditemukan" }
        val requiredNanoFiles = listOf(
            "nano_body_base.png",
            "nano_exp_netral.png", "nano_exp_senang.png", "nano_exp_berpikir.png",
            "nano_exp_waspada.png", "nano_exp_bangga.png", "nano_exp_sedih.png",
            "nano_head_larmor.png", "nano_head_visor.png", "nano_head_cap.png",
            "nano_head_goggles.png", "nano_head_cyber.png", "nano_head_coil64.png",
            "nano_head_mortarboard.png", "nano_head_beanie_mri.png", "nano_head_loupe.png",
            "nano_uniform_scrub_mint.png", "nano_uniform_labcoat.png", "nano_uniform_scrub_blue.png",
            "nano_uniform_scrub_teal.png", "nano_uniform_vest_mrso.png", "nano_uniform_cryo.png",
            "nano_uniform_dosen.png", "nano_uniform_mahasiswa.png", "nano_uniform_leadfree.png",
            "nano_headset_radio.png", "nano_headset_intercom.png",
            "nano_headset_pneumatic.png", "nano_headset_sar.png",
            "nano_headset_lecture.png", "nano_headset_comm_pro.png",
            "nano_tool_detector.png", "nano_tool_badge.png", "nano_tool_phantom.png",
            "nano_tool_tablet.png", "nano_tool_oximeter.png", "nano_tool_coil.png",
            "nano_tool_pointer.png", "nano_tool_handbook.png"
        )
        val missing = requiredNanoFiles.filter { !File(dir, it).isFile }
        assertTrue("File aset layer Nano hilang di res/drawable: $missing", missing.isEmpty())
    }
}
