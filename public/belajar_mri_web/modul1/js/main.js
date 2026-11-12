// Perakit halaman: render materi dan aktivitas, lalu jalankan semua init.
const JUDUL_TINGKAT={1:'Tingkat 1 (Dasar)',2:'Tingkat 2 (Menengah)',3:'Tingkat 3 (Lanjut, boleh dilewati)'};
(function(){
 // Tiap aktivitas punya properti setelah (kode materi). Aktivitas disisipkan tepat di bawah kartu materi itu.
 // Aktivitas tanpa setelah yang valid ditaruh di bagian Aktivitas di akhir materi.
 const akt=MODUL.aktivitas,pasang=a=>a.html.replace("<div class='kartu'>","<div class='kartu akt' id='akt-"+a.kode+"-"+a.setelah+"'>");
 const kartu=m=>m.html.replace(/<div class='kartu( lanjut)? bm/,s=>"<div id='mat-"+m.kode+"' "+s.slice(5))+akt.filter(a=>a.setelah===m.kode).map(pasang).join('');
 let h='';
 MODUL.materi.filter(m=>m.tingkat===0).forEach(m=>h+=kartu(m));
 [1,2,3].forEach(t=>{h+='<h2>'+JUDUL_TINGKAT[t]+'</h2>';MODUL.materi.filter(m=>m.tingkat===t).forEach(m=>h+=kartu(m));});
 $('materi').innerHTML=h;
 const sisa=akt.filter(a=>!MODUL.materi.some(m=>m.kode===a.setelah));
 $('aktivitas').innerHTML=sisa.map(pasang).join('');
 $('h-aktivitas').hidden=!sisa.length;
 isiAngkaTeks();
 bangunModeKartu();
 MODUL.aktivitas.forEach(a=>a.init&&a.init());
 MODUL.init.forEach(f=>f());
 mulaiKuis();
 requestAnimationFrame(tick);
})();

// Loncat ke jangkar (#akt-..., #t1-6-...) setelah halaman dirakit. Jangkar baru ada setelah JS jalan,
// jadi lompatan bawaan peramban tidak cukup. Dipakai tautan dari aplikasi Android dan versi web.
(function(){
 const loncat=()=>{const id=decodeURIComponent(location.hash.slice(1));if(!id)return;
  const el=document.getElementById(id)||document.getElementById('mat-'+id);
  if(el)setTimeout(()=>el.scrollIntoView({behavior:'auto',block:'start'}),60);};
 window.addEventListener('hashchange',loncat);loncat();
})();
