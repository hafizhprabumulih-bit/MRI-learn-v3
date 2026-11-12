// T2.3 Sinyal MR dan FID
// Edit teks di dalam HTML. Kelas: .s = materi singkat, .l = lengkap, .a = analogi.
MODUL.materi.push({kode:'t2-3a-sinyal-fid',tingkat:2,html:`<div class='kartu bm'>
<span class='tag'>T2.3 Sinyal MR dan FID</span>
<p class='s'>Magnetisasi sefase yang berpresesi di bidang transversal mengubah medan magnet di sekitar koil, sehingga menginduksi tegangan pada koil penerima. Tegangan itu adalah sinyal. Setelah pulsa dimatikan, sinyalnya meluruh: itulah FID.</p>
<p class='l'><b>Sinyal MR.</b> Berkat resonansi, magnetisasi koheren berpresesi di bidang transversal. Medan magnet yang berubah ini menimbulkan arus listrik, sesuai hukum induksi Faraday: perubahan fluks magnetik melalui rangkaian tertutup menginduksi gaya gerak listrik (ggl). Ggl adalah energi per satuan muatan yang mengitari satu putaran kawat, dan ia mendorong arus.</p>
<ul class='l'>
<li>Ggl induksi sebanding dengan laju perubahan medan magnet dan luas rangkaian.</li>
<li>Ggl sebanding dengan jumlah lilitan kawat.</li>
<li>Arah ggl melawan perubahan medan yang menyebabkannya (hukum Lenz).</li>
</ul>
<p class='l'>Persamaannya <b>&epsilon; = &minus;N d&Phi;/dt</b>, dengan &epsilon; ggl (volt), N jumlah lilitan, d&Phi;/dt laju perubahan fluks magnetik, dan t waktu (detik). Bila koil penerima berada di dekat magnetisasi yang berpresesi di bidang transversal, tegangan yang timbul disebut <b>sinyal</b>. Frekuensi sinyal sama dengan frekuensi putaran medan magnet, dan besarnya bergantung pada banyaknya magnetisasi koheren di bidang transversal.</p>
<p class='l'><b>FID.</b> Ketika pulsa RF dimatikan, NMV hanya dipengaruhi B0 dan berusaha menyelaraskan diri kembali dengannya. Proses hilangnya energi hidrogen ini disebut <b>relaksasi</b>. NMV kembali searah B0 karena sebagian spin berenergi tinggi kembali ke populasi rendah. Pada saat yang sama, tetapi terpisah, momen magnetik kehilangan koherensi (dephasing) akibat ketidakhomogenan B0 dan interaksi antarspin. Magnetisasi transversal koheren menurun, begitu pula tegangan di koil. Tegangan yang meluruh ini disebut sinyal <b>free induction decay (FID)</b>: spin berpresesi bebas (free) hanya karena B0, sinyal meluruh (decay), dan momen magnetik menginduksi (induction) arus di koil.</p>
<p class='l'>Besar dan waktu pulsa RF menjadi bagian dari urutan pulsa (pulse sequence), yang menjadi dasar pembentukan kontras di MRI (dibahas di modul lain).</p>
<p class='a'><b>Analogi 1, dinamo sepeda:</b> magnet yang berputar dekat kumparan menimbulkan listrik. Magnetisasi yang berpresesi di dekat koil penerima bekerja serupa, dan listriknya adalah sinyal MR.</p>
<p class='a'><b>Analogi 2, paduan suara:</b> di awal semua penyanyi serempak (koheren), suaranya keras. Lalu tiap penyanyi sedikit berbeda tempo, sehingga suara makin kacau dan melemah. Dephasing membuat sinyal FID meluruh. Relaksasi seperti ayunan yang perlahan kembali ke bawah setelah didorong.</p>
</div>`});
