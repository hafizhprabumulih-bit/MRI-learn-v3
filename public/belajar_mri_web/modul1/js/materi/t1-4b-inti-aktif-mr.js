// T1.4 Contoh inti aktif MR
// Edit teks di dalam HTML. Kelas: .s = materi singkat, .l = lengkap, .a = analogi.
MODUL.materi.push({kode:'t1-4b-inti-aktif-mr',tingkat:1,html:`<div class='kartu bm'>
<span class='tag'>T1.4 Contoh inti aktif MR</span>
<p class='s'>Inti aktif MR berspin dan bermuatan positif, sehingga punya momen magnetik dan cenderung menyelaraskan diri dengan medan magnet luar. Contoh: <sup>1</sup>H, <sup>13</sup>C, <sup>19</sup>F, <sup>23</sup>Na.</p>
<p class='l'>Inti aktif MR cenderung menyelaraskan sumbu putarnya dengan medan magnet luar. Inti ini berspin dan bermuatan positif, sehingga otomatis punya medan magnet sendiri. Dalam teori klasik medan itu disebut momen magnetik, sebuah vektor dengan besar dan arah. Dasarnya adalah induksi elektromagnetik (Faraday, 1833): muatan yang bergerak menimbulkan medan magnet.</p>
<p class='l'>Contoh inti aktif MR: <sup>1</sup>H (hidrogen), <sup>13</sup>C (karbon), <sup>19</sup>F (fluor), <sup>23</sup>Na (natrium).</p>
<p class='a'><b>Analogi:</b> kawat yang dililit lalu dialiri arus menjadi elektromagnet. Inti yang berspin dan bermuatan positif mirip itu: otomatis jadi magnet mini, sehingga mau menanggapi magnet luar. Inti yang spinnya saling batal seperti dua magnet yang ditempel berlawanan: dari luar tampak tidak magnetik.</p>
<div class='l'>
<b>Coba: apakah inti ini aktif MR?</b>
<label for='ipil'>Pilih inti</label>
<select id='ipil'><option value=''>(isi sendiri)</option><option value='1H'>1H</option><option value='12C'>12C</option><option value='13C'>13C</option><option value='6Li'>6Li</option><option value='14N'>14N</option><option value='19F'>19F</option><option value='23Na'>23Na</option></select>
<label for='iz'>Jumlah proton</label><input type='number' id='iz' min='1' step='1' value='1' inputmode='numeric'>
<label for='ineu'>Jumlah neutron</label><input type='number' id='ineu' min='0' step='1' value='0' inputmode='numeric'>
<p id='ihasil' role='status' aria-live='polite'></p>
</div>
</div>`});
