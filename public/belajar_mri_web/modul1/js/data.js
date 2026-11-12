// Data dan konstanta fisika. Ubah angka di sini, bukan di teks materi.
const DATA={
 FISIKA:{gamma_MHz_per_T:42.58,larmor_MHz:{1.5:63.87,3:127.74},geser_kimia_lemak_air_ppm:3.5},
 JARINGAN:[
  {id:'csf',nama:'Cairan serebrospinal',T1_ms:2400,T2_ms:180,PD:1.00},
  {id:'gm',nama:'Substansia grisea',T1_ms:925,T2_ms:100,PD:0.86},
  {id:'wm',nama:'Substansia alba',T1_ms:790,T2_ms:92,PD:0.77},
  {id:'fat',nama:'Lemak',T1_ms:260,T2_ms:85,PD:1.00},
  {id:'muscle',nama:'Otot',T1_ms:875,T2_ms:47},
  {id:'kidney',nama:'Ginjal',T1_ms:650,T2_ms:58},
  {id:'liver',nama:'Hati',T1_ms:500,T2_ms:43}],
 SKALA_B0:{1.5:{T1:1,T2:1},3:{T1:1.17,T2:0.9}},
 INSTRUMEN:{suhu_K:4.2,gradien_mT_per_m:[20,80],slew_T_per_m_per_s:[100,200],bore_cm:[60,70]},
 KESELAMATAN:{sar_tubuh_W_per_kg:{normal:2,tingkat1:4},sar_kepala_normal_W_per_kg:3.2,garis_gauss_mT:{standar:0.5,alternatif:0.9}}
};
function waktuRelaksasi(j,B0){const s=DATA.SKALA_B0[B0];return{T1:Math.round(j.T1_ms*s.T1),T2:Math.round(j.T2_ms*s.T2)};}
const G=DATA.FISIKA.gamma_MHz_per_T;
const fmt=(x,d=2)=>x.toFixed(d).replace('.',',');
const larmor=B=>G*B; // f = gamma*B0, gamma/2pi dari DATA
const isi={gamma:fmt(G),l15:fmt(DATA.FISIKA.larmor_MHz[1.5]),l3:fmt(DATA.FISIKA.larmor_MHz[3])};
// TODO-VERIFIKASI: angka di bawah berasal dari buku rujukan (buku rujukan), bukan dari DATA. Periksa sebelum dipakai.
const BUKU={gamma:{'13C':10.7084,'19F':40.0776,'23Na':11.2620},spin:{'1H':'1/2','13C':'1/2','19F':'1/2','23Na':'3/2'},lebih_per_juta:{1.5:4.5,3:10}};
const NUK=['1H','13C','19F','23Na']; // TODO-VERIFIKASI: gamma 19F dan 23Na ditambahkan sesuai pohon M01; cek ke buku rujukan.
const gam=k=>k==='1H'?G:BUKU.gamma[k];
const bk={lebih15:fmt(BUKU.lebih_per_juta[1.5],1),lebih3:fmt(BUKU.lebih_per_juta[3],0),l05:fmt(larmor(0.5)),l10:fmt(larmor(1))};
function isiAngkaTeks(){
 document.querySelectorAll('[data-isi]').forEach(e=>{e.textContent=isi[e.dataset.isi];});
 document.querySelectorAll('[data-buku]').forEach(e=>{e.textContent=bk[e.dataset.buku];});
}
