# Fitur Ruang Karakter (Character Room) - Belajar MRI

Fitur interaktif baru yang memungkinkan pengguna mengkustomisasi avatar medis mereka di panggung ruang kontrol MRI, dengan pilihan kategori kustomisasi (Rambut/Gaya, Seragam, Headset, dan Latar) serta pembaruan tampilan Beranda yang menampilkan scene ruang kontrol MRI dan tiga monitor progres pemindaian.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> - **Unlocked Items**: Berdasarkan preferensi Anda, semua item kustomisasi (Rambut, Seragam, Headset, Latar) terbuka langsung (unlocked) untuk pengujian instan.
> - **Visual Theme**: Latar Ruang Karakter dan scene Beranda mengusung tema **Ruang Kontrol MRI Utama** lengkap dengan jendela scanner, jam digital merah, monitor citra otak, dan meja kontrol interaktif.

---

### 1. Overview & Core Concept

- **What It Does**: Memberikan ruang khusus bagi pengguna untuk mempersonalisasi avatar robot medis mereka (seperti Radiografer, Radiolog, Fisikawan Medis, dll.) dengan berbagai aksesori dan latar ruang kontrol MRI. Di Beranda, kartu avatar lama diganti dengan scene ruang kontrol MRI interaktif yang menampilkan avatar terpilih bersama monitor progres pemindaian.
- **Target Audience / Persona**: Mahasiswa radiologi, radiografer, dan tenaga medis yang belajar fisika & teknik MRI.
- **Key Value**: Meningkatkan keterlibatan (gamification & personalization) dan motivasi belajar melalui visualisasi ruang kontrol MRI yang imersif.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. Pengguna membuka **Beranda**, melihat scene ruang kontrol MRI dengan avatar mereka dan 3 monitor progres pemindaian (modul, kuis, simulasi).
  2. Pengguna menekan tombol "Ruang Karakter" atau tab "Karakter" di navigasi bawah.
  3. Di layar **Ruang Karakter**, pengguna dapat memilih kategori (Rambut/Gaya, Seragam, Headset, Latar), memilih item, mengubah warna, mereset, atau menekan "Simpan dan Kembali".
  4. Perubahan langsung tersimpan di DataStore dan tercermin secara real-time di Beranda.
- **Visual Identity & Theme**:
  - *Color Palette*: Obsidian `#071A2B`, Card Slate `#0F2A45`, Border `#1D4468`, Cyan Accent `#1FB6E8`, Teal `#7FDCD6`, Success Mint `#3CC4A3`, Orange Action `#FF8A1F`, Text `#E6F2FB`, Muted `#BFD9EC`.
  - *Typography & Spacing*: Material 3 typography with 16–20dp rounded corner cards and minimum 48dp touch targets.

---

### 3. Key Product Decisions & Trade-Offs

- **State Persistence**: Menyimpan status kustomisasi karakter (avatarId, equipped items, backgroundId) menggunakan Jetpack Preferences DataStore agar tetap bertahan setelah aplikasi ditutup.
- **Backward Compatibility**: Mempertahankan `userAvatarId` yang sudah ada di DataStore agar profil lama tidak rusak.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   MriViewModel                         │
│   (Manages Character State, Equipped Items, DataStore) │
└───────────┬────────────────────────────────┬───────────┘
            │                                │
            ▼                                ▼
┌───────────────────────┐        ┌───────────────────────┐
│     HomeScreen        │        │ CharacterRoomScreen   │
│ (MRI Control Room     │        │ (Category Tabs, Grid, │
│  Scene & 3 Monitors)  │        │  Color Picker, Save)  │
└───────────────────────┘        └───────────────────────┘
```

- **Data Models**:
  - `CharacterCustomizationState`: Menyimpan `avatarId`, `equippedHairId`, `equippedUniformId`, `equippedHeadsetId`, `backgroundId`, `accentColor`.
  - `CharacterItem`: Mendefinisikan item kustomisasi (id, name, category, icon/drawable, unlocked status).
- **Navigation**:
  - Menambahkan `MainTab.KARAKTER` ke `MainTab` enum dan menambahkan item tab ke `MriBottomNavigationBar`.
