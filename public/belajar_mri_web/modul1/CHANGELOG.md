# Catatan perubahan Modul 1

## Struktur dan isi
- Materi disesuaikan dengan pohon M01: kartu diberi kode simpul T1.1 sampai T3.5, dan paragraf yang salah tingkat dipindah (populasi/NMV, presesi/Larmor, dan spektrum RF ke Tingkat 1; Boltzmann dan tabel inti ke Tingkat 3).
- Ditambah: T1.1 (peran MRI klinis), T3.1 sampai T3.5, tujuan belajar, prasyarat, dan tautan silang (M02, M07A, M07C, M06B).
- Daftar inti diganti menjadi 1H, 13C, 19F, 23Na (gamma 19F dan 23Na: TODO-VERIFIKASI).
- Kuis dipisah menjadi tab Tingkat 1, 2, 3 (12, 7, 5 soal; 6 soal baru).
- Proyek dipecah menjadi multi-file: `js/materi/`, `js/aktivitas/`, `js/kuis/`, `css/`, `tools/gabung.py`.

## Aktivitas
- A1: ditambah mode Boltzmann (T3.1).
- A2: kalkulator Larmor untuk 1H, 13C, 19F, 23Na.
- A3 (fase dan sinyal koil): garis putus-putus dari ujung resultan ke titik sinyal terbaru (satu nilai, seirama); pulsa tidak lagi menghapus riwayat sinyal; keterangan "nilai yang dicatat koil" tetap di bawah kotak sinyal.
- A3 (sudut flip): NMV digambar pada bola (fungsi bersama `proyeksiBola` dan `gambarBola` di `core.js`).
- A4: NMV pada bola; tombol Pulsa 90 dan 180 hanya muncul bila RF = Larmor.

## Penempatan aktivitas dan perbaikan A1
- Tiap aktivitas kini disisipkan tepat di bawah kartu materi pendukungnya lewat properti `setelah` (bukan lagi satu blok di akhir). Kartu aktivitas diberi garis tepi dan awalan "Aktivitas" agar mudah dibedakan. Bagian "Aktivitas" di akhir disembunyikan bila kosong.
  - T1.3: Atom hidrogen. T1.4: Pengantar. T1.5: A1. T1.6: Presesi bola, A2, Latihan tebak Larmor. T2.2 nutasi: A4. T2.2 sudut flip: A3 (sudut flip). T2.3 sinyal FID: A3 (fase dan sinyal koil). T2.4: Tambahan TR dan TE.
- A1: sumbu spin tiap proton kini panah putus-putus berkepala (ke atas = searah B0, ke bawah = berlawanan), ditambah panah B0 putus-putus di lajur kiri. Kanvas diperbesar dan perhitungan klik disesuaikan.

## Aktivitas baru (T1.3 dan T1.6)
- `atom-hidrogen.js` (T1.3): model Bohr atom hidrogen bergaya gambar acuan (nukleus, orbit, elektron, legenda). Pilihan isotop hidrogen-1, -2, -3, ionisasi (lepas elektron), tampilan orbit atau awan elektron, klik partikel untuk melihat sifatnya, dan tabel Z, neutron, A, elektron, muatan.
- `presesi-bola.js` (T1.6): bola proton berspin yang sumbunya berpresesi mengelilingi B0 (bola 3D, kerucut presesi, jejak, komponen transversal), pilihan inti dan B0 untuk melihat f = gamma x B0, sudut miring, spin atas atau bawah, dan grafik komponen x terhadap waktu. Arah presesi mengikuti persamaan Bloch (searah jarum jam dari atas).

## T1.7 Spektrum EM
- Strip warna bergradasi, gelombang berwarna mengikuti gradasi.
- Ikon tiap pita (radio, mikro, inframerah, pelangi, UV, sinar-X, gamma) di antara balok pengion/non-pengion dan strip.
- Titik bisa diseret, slider dan titik saling sinkron, klik ikon meluncurkan halus ke pita.

## Perlu diverifikasi ke buku rujukan
- Angka kelebihan proton (hasil hitungan 4,9 dan 9,9 per sejuta pada 1,5 T dan 3 T).
- Batas pita spektrum EM dan batas pengion 10^16 Hz.

## Interaksi dan tombol (pembaruan)
- Semua animasi langsung berjalan saat halaman dibuka (A3 sudut flip mengulang pulsa; A4 mengulang siklus pulsa 90, FID, pulih; A3 fase memberi pulsa awal).
- Tombol "Jeda/Lanjutkan animasi" menjadi satu toggle ikon (jeda/putar). Tombol Reset menjadi tombol ikon. Helper: `tombolJeda`, `tombolReset`, `seret` di `core.js`.
- Bola bisa diseret: proton A1, spin A3 fase, ujung sumbu spin (Presesi), NMV (A3 sudut flip, A4), elektron/proton/neutron (Atom H), proton Pengantar. Bagian kanvas lain memutar pandangan bola 3D.
- Kanvas dan baris tombol dipusatkan; legenda dipindah ke tengah bawah; panah B0 A1 di tengah latar.

## A5 Bola Bloch dan A6 TR berulang (baru)
- A5: prototipe bola Bloch dimasukkan sebagai aktivitas di bawah T2.3 (komponen NMV). Memakai `proyeksiBola`/`gambarBola`, `seret`, `tombolJeda`, `tombolReset`, `waktuRelaksasi`, dan `LOOP` dari `core.js`/`data.js`. Pulsa 90 diberikan otomatis di awal dan setelah reset.
- A6: prototipe TR dengan pulsa berulang dimasukkan di bawah T2.4. Jaringan diambil dari `DATA.JARINGAN` yang punya PD. Riwayat grafik kini dipangkas (di prototipe tumbuh tanpa batas).
- Kedua aktivitas: ID berawalan, warna dari variabel CSS modul, kanvas HiDPI.
