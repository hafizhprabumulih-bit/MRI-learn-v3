// Tambahan: TR dan TE
MODUL.aktivitas.push({kode:'Tambahan',setelah:'t2-4-tr-te',html:`<div class='kartu'>
<b>Tambahan: urutan pulsa dasar TR dan TE (T2.4; tidak ada di daftar simulasi pohon)</b>
<p class='lembut'>Model sederhana. Pemulihan longitudinal dan peluruhan transversal dianggap eksponensial. Waktu relaksasi jaringan diambil dari DATA modul.</p>
<canvas id='ktr' width='520' height='210' role='img' aria-label='Garis waktu pulsa RF dan sinyal dengan penanda TR dan TE'></canvas>
<label for='jtr'>Jaringan</label>
<select id='jtr'></select>
<label for='btr'>B0</label>
<select id='btr'><option value='1.5'>1,5 T</option><option value='3'>3 T</option></select>
<label for='str'>TR: <span id='vtr'></span> ms</label>
<input type='range' id='str' min='50' max='3000' step='50' value='500'>
<label for='ste'>TE: <span id='vte'></span> ms</label>
<input type='range' id='ste' min='5' max='300' step='5' value='30'>
<p>T1 = <b id='tt1'></b> ms, T2 = <b id='tt2'></b> ms.</p>
<p>Pemulihan longitudinal saat pulsa berikutnya: <b id='tp1'></b></p>
<div class='meter' aria-hidden='true'><i id='mtr1'></i></div>
<p>Sisa magnetisasi transversal saat sinyal dibaca: <b id='tp2'></b></p>
<div class='meter' aria-hidden='true'><i id='mtr2'></i></div>
<div class='baris'><button id='tjeda'></button><button id='treset'></button></div>
</div>`,init(){
// ---- Aktivitas 7: TR dan TE ----
// TODO-VERIFIKASI: model eksponensial sederhana. Sinyal kedua memakai pemulihan 1-exp(-TR/T1) (anggap pulsa 90 derajat, magnetisasi longitudinal nol sesudah pulsa). T2* dan keadaan tunak diabaikan.
const ktr=$('ktr'),ctr=ktr.getContext('2d'),TRS={jeda:false,t:0,last:0};
$('jtr').innerHTML=DATA.JARINGAN.map(j=>'<option>'+j.nama+'</option>').join('');
function relaksTR(){const j=DATA.JARINGAN[$('jtr').selectedIndex];return waktuRelaksasi(j,parseFloat($('btr').value));}
function infoTR(){
 const w=relaksTR(),TR=parseFloat($('str').value);
 if(parseFloat($('ste').value)>TR-5)$('ste').value=String(Math.max(5,TR-5));
 const TE=parseFloat($('ste').value),rec=1-Math.exp(-TR/w.T1),sisa=Math.exp(-TE/w.T2);
 $('vtr').textContent=TR;$('vte').textContent=TE;$('tt1').textContent=w.T1;$('tt2').textContent=w.T2;
 $('tp1').textContent=Math.round(rec*100)+'%';$('tp2').textContent=Math.round(sisa*100)+'%';
 $('mtr1').style.width=Math.round(rec*100)+'%';$('mtr2').style.width=Math.round(sisa*100)+'%';
}
function gambarTR(ts){
 const dt=Math.min(0.05,(ts-(TRS.last||ts))/1000);TRS.last=ts;if(!TRS.jeda)TRS.t=(TRS.t+dt/7)%1;
 const w=relaksTR(),TR=parseFloat($('str').value),TE=parseFloat($('ste').value),W=ktr.width,H=ktr.height;
 const x0=16,L=W-32,tot=2*TR,X=t=>x0+t/tot*L,rec=1-Math.exp(-TR/w.T1),sisa=Math.exp(-TE/w.T2);
 const tx=warna('--teks'),lm=warna('--lembut'),gr=warna('--garis'),t1=warna('--t1'),t2=warna('--t2'),ak=warna('--aksen'),ph=TRS.t*tot;
 ctr.clearRect(0,0,W,H);ctr.font='13px system-ui,sans-serif';ctr.textAlign='center';
 ctr.strokeStyle=gr;ctr.lineWidth=1;ctr.setLineDash([4,4]);
 [0,TR,tot].forEach(t=>{ctr.beginPath();ctr.moveTo(X(t),22);ctr.lineTo(X(t),H-30);ctr.stroke();});
 ctr.setLineDash([]);ctr.beginPath();ctr.moveTo(x0,120);ctr.lineTo(x0+L,120);ctr.stroke();
 ctr.fillStyle=lm;ctr.textAlign='left';ctr.fillText('Pulsa RF',x0,50);ctr.fillText('Sinyal',x0,106);ctr.textAlign='center';
 [0,TR,tot].forEach(t=>{const x=Math.min(X(t),x0+L-12);ctr.globalAlpha=ph>=t||t===tot?1:0.35;ctr.fillStyle=t1;ctr.fillRect(x,56,12,22);});
 ctr.globalAlpha=1;
 ctr.strokeStyle=tx;ctr.fillStyle=tx;ctr.lineWidth=1.5;
 [[0,TR],[TR,tot]].forEach(([a,b])=>{ctr.beginPath();ctr.moveTo(X(a)+2,32);ctr.lineTo(X(b)-2,32);ctr.stroke();ctr.fillText('TR',(X(a)+X(b))/2,26);});
 ctr.strokeStyle=t2;ctr.fillStyle=t2;ctr.beginPath();ctr.moveTo(X(0)+2,H-14);ctr.lineTo(X(TE),H-14);ctr.stroke();ctr.fillText('TE',(X(0)+X(TE))/2,H-18);
 [0,1].forEach(k=>{
  const tc=k*TR+TE,A=62*sisa*(k?rec:1),cx=X(tc);ctr.globalAlpha=ph>=tc?1:0.3;ctr.strokeStyle=t2;ctr.lineWidth=2;ctr.beginPath();
  for(let d=-34;d<=34;d++){const y=120-A*Math.exp(-(d/14)*(d/14))*Math.cos(d*0.9);d>-34?ctr.lineTo(cx+d,y):ctr.moveTo(cx+d,y);}
  ctr.stroke();ctr.globalAlpha=1;
 });
 ctr.strokeStyle=ak;ctr.lineWidth=2;ctr.beginPath();ctr.moveTo(X(ph),20);ctr.lineTo(X(ph),H-30);ctr.stroke();
}
$('jtr').onchange=$('btr').onchange=infoTR;$('str').oninput=$('ste').oninput=infoTR;
const sJ=tombolJeda($('tjeda'),()=>TRS.jeda,v=>TRS.jeda=v);
tombolReset($('treset'),()=>{$('jtr').selectedIndex=0;$('btr').value='1.5';$('str').value='500';$('ste').value='30';TRS.t=0;TRS.jeda=false;sJ();infoTR();});
infoTR();


LOOP.push(ts=>gambarTR(ts));
}});
