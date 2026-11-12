// T1.7 Spektrum elektromagnetik dan sifat non-pengion RF
// Edit teks di dalam HTML. Kelas: .s = materi singkat, .l = lengkap, .a = analogi.
MODUL.materi.push({kode:'t1-7-spektrum-em',tingkat:1,html:`<div class='kartu bm'>
<span class='tag'>T1.7 Spektrum elektromagnetik dan sifat non-pengion RF</span>
<p class='s'>Frekuensi untuk MRI hidrogen berada di pita radiofrekuensi (RF). RF bersifat non-pengion.</p>
<p class='l'>Frekuensi Larmor hidrogen pada B0 klinis berada di pita radiofrekuensi (RF) pada spektrum elektromagnetik. Radiasi RF bersifat non-pengion: energinya tidak cukup untuk melepas elektron dari atom. Sinar-X dan sinar gamma berfrekuensi jauh lebih tinggi dan bersifat pengion. Batas keselamatan RF (SAR) dibahas di M07C.</p>
<div id='em'>

<b>Peta spektrum elektromagnetik</b>
<p class='lembut'>Skala frekuensi logaritmik, warna strip bergradasi mengikuti pita. Geser slider, atau seret titik pada sumbu, atau klik gambar untuk memilih frekuensi. Klik ikon pita untuk loncat ke pita itu. Segitiga menandai frekuensi Larmor hidrogen (MRI) pada B0 yang Anda pilih.</p>
<canvas id='kem' class='klik' width='520' height='270' style='cursor:grab;touch-action:pan-y' role='img' aria-label='Peta spektrum elektromagnetik dari gelombang radio sampai sinar gamma, dengan ikon tiap pita, strip warna bergradasi, dan penanda frekuensi MRI'></canvas>
<label for='sem'>Frekuensi yang dilihat (log<sub>10</sub> Hz)</label>
<input type='range' id='sem' min='4' max='20' step='0.01' value='7.8'>
<label for='bem'>B0 untuk penanda MRI: <span id='vbem'></span> T</label>
<input type='range' id='bem' min='0.5' max='3' step='0.1' value='1.5'>
<p id='emtxt' role='status' aria-live='polite'></p>
<p id='emmri' class='lembut'></p>
<div class='baris'><button type='button' id='embtn'>Ke frekuensi MRI</button><button id='emjeda'></button><button id='emreset'></button></div>
</div>
<p class='a'><b>Analogi, stasiun radio:</b> tiap stasiun punya frekuensi sendiri. Radio hanya menangkap siaran bila disetel tepat pada frekuensinya. Pulsa RF juga hanya "menyapa" inti yang frekuensi Larmornya cocok. Inti lain tidak menanggapi.</p>
</div>`});
