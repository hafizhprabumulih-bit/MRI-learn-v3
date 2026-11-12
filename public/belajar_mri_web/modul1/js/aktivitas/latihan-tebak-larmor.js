// Latihan: tebak frekuensi Larmor
MODUL.aktivitas.push({kode:'Latihan',setelah:'t1-6-presesi-larmor',html:`<div class='kartu'>
<b>Latihan: tebak frekuensi Larmor (T1.6)</b>
<p>Hitung f = &gamma;&middot;B0 dari B0 acak berikut.</p>
<p>B0 = <b><span id='gb0'></span> T</b></p>
<label for='gjwb'>Jawaban (MHz)</label>
<input type='number' id='gjwb' step='0.01' inputmode='decimal'>
<div class='baris'>
<button type='button' id='gcek'>Periksa</button>
<button type='button' id='gbaru'>Soal baru</button>
<button id='greset'></button>
</div>
<p id='ghasil' role='status' aria-live='polite'></p>
<p class='lembut'>Skor: <span id='gskor'>0 / 0</span></p>
</div>`,init(){
// ---- Aktivitas 2: tebak frekuensi Larmor ----
const Q={B:1.5,benar:0,total:0};
function soalBaru(){
 Q.B=Math.round((0.5+Math.random()*2.5)*10)/10; // B0 acak, kelipatan 0,1 T
 $('gb0').textContent=fmt(Q.B,1);$('gjwb').value='';$('ghasil').textContent='';$('ghasil').className='';
}
function skorG(){$('gskor').textContent=Q.benar+' / '+Q.total;}
$('gcek').onclick=()=>{
 const v=parseFloat(String($('gjwb').value).replace(',','.'));
 if(isNaN(v)){$('ghasil').className='';$('ghasil').textContent='Isi jawaban dulu.';return;}
 const f=larmor(Q.B);Q.total++;
 // TODO-VERIFIKASI: toleransi jawaban 0,5 MHz adalah pilihan didaktik.
 const ok=Math.abs(v-f)<=0.5;if(ok)Q.benar++;
 $('ghasil').className=ok?'ok':'no';
 $('ghasil').textContent=(ok?'Benar. ':'Belum tepat. ')+'f = '+fmt(G)+' \u00d7 '+fmt(Q.B,1)+' = '+fmt(f)+' MHz.';
 skorG();
};
$('gbaru').onclick=soalBaru;
tombolReset($('greset'),()=>{Q.benar=0;Q.total=0;skorG();soalBaru();});
soalBaru();



}});
