# Pengembangan Fitur Flashcards, Bookmark Materi & Deduplikasi Kode Belajar MRI

Pembaruan ini menghadirkan fitur **Flashcards Hafalan Cepat** dan **Bookmark Materi** yang terintegrasi langsung di dalam bagian Modul dan Glosarium untuk mempercepat penguasaan konsep fisika dan klinis MRI, sekaligus membersihkan seluruh fungsi kode yang duplikat dan berkas aset kembar agar basis kode menjadi bersih, ringan, dan mudah dirawat.

### User Review & Critical Decisions

> [!IMPORTANT]
> Rencana ini disusun berdasarkan pilihan yang telah Anda konfirmasi pada tahap klarifikasi sebelumnya. Silakan tinjau arsitektur di bawah ini dan klik **Proceed** untuk memulai implementasi kode.

- **Confirmed Decision 1 (Fitur Baru)**: Menambahkan **Flashcards Hafalan Cepat** (kartu bolak-balik interaktif untuk menghafal konsep/parameter MRI) dan **Bookmark Materi** (penanda simpan simpul materi & istilah glosarium favorit).
- **Confirmed Decision 2 (Penempatan Fitur)**: Mengintegrasikan fitur **Bookmark** dan **Flashcards** langsung ke dalam tampilan **Modul** dan **Glosarium**, sehingga pengguna dapat menandai materi penting dan melatih hafalan istilah/simpul tanpa berpindah ke menu terpisah yang membingungkan.
- **Confirmed Decision 3 (Cakupan Pembersihan Duplikasi)**: Membersihkan **semua duplikasi**, mencakup penghapusan fungsi kembar (alias redundan) di ViewModel, penyatuan daftar & pemetaan 9 avatar medis ke satu sumber tunggal (*Single Source of Truth*), serta penghapusan seluruh berkas gambar dan berkas web kembar.

---

### 1. Overview & Core Concept

- **What It Does**:
  1. **Bookmark Materi & Istilah Glosarium**: Pengguna dapat menandai (*bookmark*) simpul materi pada setiap Modul maupun istilah penting pada Glosarium, serta menyaring daftar untuk hanya menampilkan materi/istilah yang tersimpan.
  2. **Mode Flashcards Hafalan Cepat**: Di dalam layar Glosarium dan Detail Modul, pengguna dapat beralih ke mode **Flashcards Interaktif** (kartu bolak-balik dengan animasi 3D flip) untuk menguji daya ingat terhadap simbol, parameter, rumus, dan konsep klinis MRI (baik seluruh item maupun khusus item yang di-bookmark).
  3. **Refaktoring & Deduplikasi Menyeluruh**: Menghapus metode kembar di lapisan state, menyatukan katalog avatar medis agar tidak ditulis berulang di beberapa layar, serta membersihkan seluruh berkas aset/web duplikat.
- **Target Audience / Persona**: Mahasiswa radiologi, radiografer MRI, fisikawan medis, dan dokter residen yang membutuhkan alat bantu hafalan cepat (active recall) dan akses instan ke materi rujukan favorit.
- **Key Value**: Meningkatkan retensi belajar melalui metode *active recall* (Flashcards), mempercepat pencarian materi penting lewat *Bookmark* persisten, dan menjaga performa serta kerapian arsitektur aplikasi.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Alur Bookmark di Modul & Glosarium**:
     - Pada setiap kartu simpul materi di layar Detail Modul dan setiap kartu istilah di layar Glosarium, terdapat ikon penanda (**Bookmark**).
     - Saat diketuk, status bookmark langsung tersimpan secara permanen dan ikon berubah warna menjadi aksen emas/cyan aktif.
     - Tersedia chip filter **"Tersimpan (Bookmark)"** di bagian atas Modul dan Glosarium untuk menampilkan daftar simpul atau istilah favorit saja.
  2. **Alur Mode Flashcards Hafalan Cepat**:
     - Di bagian atas layar **Glosarium** (dan tombol pintas pada **Detail Modul**), terdapat pengalih tampilan (*Mode Daftar* vs *Mode Flashcards*).
     - Saat **Mode Flashcards** aktif, pengguna melihat kartu interaktif berukuran besar yang menampilkan pertanyaan/istilah/simbol di sisi depan (*Front Card*).
     - Mengetuk kartu akan memutar kartu (*Flip Card*) untuk menampilkan definisi lengkap, rumus, atau analogi klinis di sisi belakang (*Back Card*).
     - Pengguna dapat menandai kartu sebagai **"Sudah Hafal"** atau **"Ulangi Lagi"**, mengacak urutan kartu (*Shuffle*), serta memfilter dek kartu (Semua Kartu, Per Modul, atau Khusus Bookmark).

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: **Clinical Dark Precision** — mempertahankan atmosfer gelap khas konsol ruang operator MRI yang fokus, kontras tinggi, dan nyaman di mata.
  - *Color Palette & Mood*:
    - `--background`: Deep Obsidian (`#090D16`) & Dark Navy Slate (`#111827`)
    - `--primary`: Cyan Bright (`#22D3EE`) & Cyan Primary (`#06B6D4`) untuk aksi utama dan indikator aktif
    - `--accent`: Amber Warning (`#F59E0B`) untuk penanda Bookmark aktif dan Emerald Success (`#10B981`) untuk indikator penguasaan Flashcard
  - *Typography & Hierarchy*: Menggunakan kontras tipografi tegas antara judul kartu Flashcard (`20sp` ExtraBold), badge kode simpul/simbol (`Monospace`), dan teks penjelasan (`13sp` dengan tinggi baris proporsional).
  - *Component Styling & Layout*: Kartu dengan sudut membulat `20.dp`, garis tepi tipis `1.dp` (`CardSlateBorder`), serta kontrol segmen dan chip filter bergaya Material 3.

- **Interactive Feedback & Motion**:
  - Animasi rotasi kartu (*flip transition*) yang mulus saat membalik sisi depan dan belakang Flashcard.
  - Indikator progres hafalan (`Sudah Hafal X dari Y kartu`) yang diperbarui secara instan.
  - Status kosong (*Empty State*) yang informatif apabila pengguna memfilter tab Bookmark namun belum ada materi atau istilah yang disimpan.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Penyimpanan Persisten Bookmark & Progres Flashcard via DataStore Preferences**
  - *Chosen Approach*: Menyimpan himpunan kode simpul materi yang di-bookmark (`bookmarkedNodeCodes`), istilah glosarium yang di-bookmark (`bookmarkedGlossaryTerms`), serta kartu yang sudah dikuasai (`masteredFlashcardIds`) di dalam `UserDataRepository` menggunakan `stringSetPreferencesKey` pada DataStore.
  - *Why*: Menjaga skema Room Database `mri_edu.db` tetap utuh pada `version = 1` sesuai kontrak pengujian DAO yang sudah ada, sembari tetap memberikan persistensi lokal yang reaktif (`Flow<Set<String>>`), ringan, dan tahan penutupan aplikasi.
  - *Alternatives Considered*: Menambahkan tabel baru ke Room Database dan menaikkan versi skema (dilewati agar tidak merusak kompatibilitas skema versi 1 yang telah teruji).

- **Decision 2: Sentralisasi Katalog Avatar & Penghapusan Metode Kembar**
  - *Chosen Approach*: Memindahkan daftar 9 karakter avatar medis beserta fungsi resolusi gambar ke dalam satu objek katalog terpusat, serta menyatukan seluruh fungsi pengubah state yang duplikat di `MriViewModel` menjadi satu nama fungsi kanonis per aksi.
  - *Why*: Menghilangkan duplikasi kode (`DRY - Don't Repeat Yourself`) antara layar Beranda dan Dialog Avatar, serta membuat API ViewModel jauh lebih bersih dan bebas ambiguitas.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         Lapisan Data & Persistensi                        │
│  ┌─────────────────────────┐              ┌────────────────────────────┐  │
│  │   Room Database (v1)    │              │   DataStore Preferences    │  │
│  │ • NodeProgressEntity    │              │ • Profil & Avatar Medis    │  │
│  │ • StudyNoteEntity       │              │ • Bookmarked Node Codes    │  │
│  │ • QuizAttemptEntity     │              │ • Bookmarked Glossary Ids  │  │
│  │ • StudyDayEntity        │              │ • Mastered Flashcard Ids   │  │
│  └───────────┬─────────────┘              └─────────────┬──────────────┘  │
│              └────────────────────┬─────────────────────┘                 │
│                                   ▼                                       │
│                        ┌──────────────────────┐                           │
│                        │  UserDataRepository  │                           │
│                        └──────────┬───────────┘                           │
└───────────────────────────────────┼───────────────────────────────────────┘
                                    │ Flow<T> & suspend functions
                                    ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                   MriViewModel (Deduplicated & Clean API)                 │
│  • StateFlow<MriUiState> (termasuk bookmark sets & flashcard state)       │
│  • Katalog Avatar Terpusat (Single Source of Truth untuk 9 Avatar Medis)  │
└───────────────┬───────────────────────────────────────────┬───────────────┘
                │                                           │
                ▼                                           ▼
┌───────────────────────────────────────┐   ┌───────────────────────────────┐
│      Detail Modul (Module Screen)     │   │   Glosarium (Glossary Screen) │
│  ┌─────────────────────────────────┐  │   │  ┌─────────────────────────┐  │
│  │ • Filter Level & Filter Bookmark│  │   │  │ • Mode Daftar / Kartu   │  │
│  │ • Tombol Bookmark per Simpul    │  │   │  │ • Filter Modul & Simpan │  │
│  │ • Pintasan Flashcards Modul Ini │  │   │  │ • Dek Flashcards Flip   │  │
│  └─────────────────────────────────┘  │   │  └─────────────────────────┘  │
└───────────────────────────────────────┘   └───────────────────────────────┘
```

- **Data Model & State**:
  - `MriUiState` diperluas dengan:
    - `bookmarkedNodeCodes: Set<String>`: Daftar kode simpul kurikulum yang ditandai favorit.
    - `bookmarkedGlossaryTerms: Set<String>`: Daftar istilah glosarium yang ditandai favorit.
    - `masteredFlashcardIds: Set<String>`: Daftar ID kartu hafalan yang telah ditandai "Sudah Hafal".
    - `isFlashcardMode: Boolean`: Penanda apakah layar Glosarium sedang menampilkan daftar kamus atau dek kartu Flashcards interaktif.
    - `isFlashcardFlipped: Boolean` & `currentFlashcardIndex: Int`: State posisi kartu aktif dan status balik kartu (Depan/Belakang).
    - `onlyBookmarkedNodes: Boolean` & `onlyBookmarkedGlossary: Boolean`: State filter untuk menampilkan item yang di-bookmark saja.

- **Interactive Component & State Mapping**:
  - **Toggle Bookmark Simpul / Glosarium**: Mengetuk ikon bintang/bookmark pada kartu simpul (`toggleNodeBookmark(nodeCode)`) atau kartu glosarium (`toggleGlossaryBookmark(term)`) akan memperbarui `DataStore` secara asinkron dan langsung memperbarui tampilan ikon serta daftar filter.
  - **Interaksi Flashcard Flip & Navigasi Dek**:
    - Mengetuk kartu Flashcard memanggil `flipFlashcard()` untuk membalik antara sisi pertanyaan (Istilah/Simbol/Kode) dan sisi jawaban (Definisi/Rumus/Analogi).
    - Tombol **Sebelumnya / Berikutnya / Acak Kartu** memperbarui `currentFlashcardIndex` dan mereset sisi kartu ke bagian depan.
    - Tombol **Tandai Sudah Hafal** memanggil `toggleFlashcardMastered(id)` yang menyimpan progres penguasaan ke `DataStore` dan memperbarui bar progres hafalan.
  - **Pembersihan Duplikasi Berkas & Kode**:
    - Menghapus fungsi alias kembar di `MriViewModel` dan memperbarui pemanggilannya di seluruh layar UI.
    - Menghapus berkas gambar lawas/kembar yang tidak terpakai di `res/drawable/` dan berkas salinan duplikat (`*-1.html`, `*-1.js`, `*-1.jpg`, `*-1.zip`) di folder `public/`.
