# Belajar MRI (MRI Edu) 🧲

Aplikasi pembelajaran fisika dan teknik radiologi Magnetic Resonance Imaging (MRI) interaktif berbasis Android (Kotlin & Jetpack Compose) dan Web (HTML5, Tailwind CSS, JavaScript).

Aplikasi ini mengadopsi kurikulum medis terintegrasi (M01 – M09), konsep pembaca 3-mode (Singkat, Lengkap, Analogi), kamus istilah baku, kuis mandiri, serta simulator interaktif (Presesi Larmor, Kontras TR/TE, Ruang-k, dan SAR).

---

## 🚀 Cara Menghubungkan & Mengunggah Proyek ke GitHub

Tersedia **3 metode mudah** sesuai kebutuhan Anda:

### Metode 1: Langsung dari Google AI Studio (Paling Cepat Tanpa Terminal)
1. Di layar Google AI Studio Anda, lihat ke menu pojok kanan atas atau ikon pengaturan (**Settings**).
2. Pilih opsi **"Export to GitHub"** / **"Push to GitHub"**.
3. Hubungkan (*authorize*) akun GitHub Anda jika diminta.
4. Tentukan nama repositori (misal: `belajar-mri-android`) dan pilih Public atau Private.
5. Klik **Push**. Seluruh kode sumber, aset, dan konfigurasi Gradle akan otomatis dibuatkan repositori di akun GitHub Anda!

---

### Metode 2: Unggah Manual Proyek Android via Git (Komputer Lokal)
Jika Anda mengunduh proyek ini sebagai **ZIP** dari AI Studio:
1. Ekstrak file ZIP di komputer Anda.
2. Buat repositori baru di [GitHub](https://github.com/new) (misal: `belajar-mri`).
3. Buka Terminal / Git Bash di dalam folder proyek hasil ekstrak, lalu jalankan:
   ```bash
   git init
   git add .
   git commit -m "feat: inisialisasi aplikasi Belajar MRI"
   git branch -M main
   git remote add origin https://github.com/USERNAME-ANDA/belajar-mri.git
   git push -u origin main
   ```
4. Buka **Android Studio**:
   - Pilih **File > Open**, lalu arahkan ke folder proyek ini.
   - Tunggu proses Gradle Sync selesai.
   - Klik tombol **Run (Play)** untuk menjalankannya di Emulator atau HP Android fisik Anda.

---

### Metode 3: Publikasikan Versi Web Interaktif ke GitHub Pages (Gratis & Online)
Jika Anda ingin versi simulasi web langsung bisa dibuka oleh siapa saja lewat browser:
1. Di folder `public/`, terdapat file **`belajar_mri_web.zip`** (atau folder `public/belajar_mri_web/`).
2. Ekstrak file tersebut (berisi `index.html`, `app.js`, dan file gambar).
3. Buat repositori baru di GitHub (misal: `mri-edu-web`).
4. Upload semua file tersebut langsung ke repositori GitHub.
5. Masuk ke menu **Settings** repositori di GitHub > pilih menu **Pages** di sebelah kiri.
6. Pada bagian **Branch**, pilih `main` dan folder `/ (root)`, lalu klik **Save**.
7. Dalam 1-2 menit, website simulasi Anda akan aktif secara publik di:
   `https://USERNAME-ANDA.github.io/mri-edu-web/`

---

## 📂 Struktur Proyek

```
├── app/                               # Modul Aplikasi Android
│   ├── src/main/java/com/example/     # Kode Sumber Kotlin & Compose
│   │   ├── data/MriCurriculumData.kt  # Database Modul M01-M09, Konstanta & Kuis
│   │   ├── ui/home/HomeScreen.kt      # Beranda & Progres Belajar (Mockup Foto)
│   │   ├── ui/simulators/             # Virtual Lab (Spin Larmor, Kontras, k-Space, SAR)
│   │   ├── ui/module/                 # Detail Modul & Reader 3-Mode
│   │   ├── ui/glossary/               # Kamus Istilah Radiologi
│   │   ├── ui/quiz/                   # Mesin Kuis Interaktif
│   │   └── MainActivity.kt            # Navigasi & Bottom Bar
│   └── src/main/res/                  # Ikon Adaptif & Hero Gambar
├── public/
│   ├── belajar_mri_web.zip            # Paket Terkompresi Web Siap Ekstrak
│   └── belajar_mri_web/               # Berkas Sumber Web (index.html, app.js, aset)
└── README.md
```

---

## ⚠️ Pernyataan Kepatuhan Non-Klinis
Aplikasi ini dikembangkan khusus sebagai sarana edukasi bagi radiografer, mahasiswa fisika medis, dan dokter residen radiologi. Seluruh nilai perhitungan dan konstanta adalah untuk pemahaman konsep dasar, bukan acuan diagnostik pasien individual.
