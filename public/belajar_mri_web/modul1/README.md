# Modul 1: Fisika Dasar MRI (proyek multi-file)

Buka `index.html` langsung di browser atau lewat ekstensi Live Server di VS Code. Tidak perlu instalasi apa pun.

## Struktur
- `index.html`: kerangka halaman dan urutan pemuatan skrip.
- `css/base.css`: gaya dasar bersama. `css/modul.css`: gaya khusus modul ini.
- `js/core.js`: registri modul, loop animasi, pembantu gambar, pilihan singkat/lengkap/analogi.
- `js/data.js`: data dan konstanta fisika (DATA, BUKU, daftar inti, fungsi `fmt`).
- `js/materi/`: satu file per simpul pohon (t1-1 sampai t3-5). Edit HTML di dalamnya.
  - Kelas `.s` = materi singkat, `.l` = lengkap, `.a` = analogi.
  - Angka dari data: `<span data-isi='l15'>`, `<span data-buku='l05'>`.
- `js/aktivitas/`: satu file per simulasi (HTML kartu + kode simulasi).
  - `latihan-inti-aktif.js` dan `spektrum-em.js` hanya berisi kode. HTML-nya ada di kartu materi T1.4 dan T1.7.
- `js/kuis/soal-t1.js`, `soal-t2.js`, `soal-t3.js`: soal per tingkat.
- `js/main.js`: merakit halaman. `js/kuis-engine.js`, `js/kuis-tabs.js`: mesin kuis.

## Cara menambah
- **Simpul materi baru**: salin satu file di `js/materi/`, ganti `kode`, `tingkat` (1/2/3), dan HTML. Daftarkan `<script>` di `index.html` pada posisi yang diinginkan (urutan file = urutan tampil).
- **Simulasi baru**: salin file di `js/aktivitas/`. Gambar animasi dengan `LOOP.push((ts,dt)=>{...})`. Daftarkan di `index.html` setelah `js/materi/`.
  - Isi `setelah:'kode-materi'` pada `MODUL.aktivitas.push({...})` (mis. `setelah:'t1-6-presesi-larmor'`). Aktivitas akan muncul tepat di bawah kartu materi itu. Tanpa `setelah` yang valid, aktivitas jatuh ke bagian "Aktivitas" di akhir materi.
  - Beberapa aktivitas di bawah satu kartu tampil menurut urutan `<script>` di `index.html`.
- **Soal baru**: tambahkan objek di file `soal-tN.js` yang sesuai.

## Satu file mandiri
`python tools/gabung.py` menghasilkan `dist/modul1.html` (CSS dan JS digabung).

## Catatan
- Urutan `<script>` di `index.html` penting: core, data, kuis-engine, materi, aktivitas, soal, main. Untuk aktivitas, urutan skrip hanya menentukan urutan di bawah kartu materi yang sama.
- Tanda `TODO-VERIFIKASI` di kode menandai nilai yang perlu dicek ke buku rujukan (mis. γ 19F dan 23Na).

## Aktivitas A5 dan A6 (Bola Bloch dan TR berulang)
- `js/aktivitas/a5-bola-bloch.js` (T2.3, setelah `t2-3b-komponen-nmv`): bola Bloch dari integrasi RK4 persamaan Bloch, pulsa 90/180, offset, T1/T2 dari `DATA`.
- `js/aktivitas/a6-tr-pulsa-berulang.js` (T2.4, setelah `t2-4-tr-te`, di bawah "Tambahan"): dua jaringan, TR/TE/sudut flip, keadaan tunak. Hanya jaringan ber-PD yang dipilih.
- Semua ID diberi awalan (`bl-` untuk A5, `ul-` untuk A6) agar tidak bentrok. Gaya tambahan ada di akhir `css/modul.css`.
