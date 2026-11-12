function mulaiKuis(){
 const TK=document.querySelectorAll('#tabkuis button');
 function pilih(n){TK.forEach(b=>b.setAttribute('aria-pressed',b.dataset.t==n));jalankanKuis($('kuis'),MODUL.soal[n]);}
 TK.forEach(b=>b.onclick=()=>pilih(b.dataset.t));pilih(1);
}
