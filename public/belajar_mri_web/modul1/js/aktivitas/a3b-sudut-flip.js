// A3 Eksitasi dan FID: sudut flip
MODUL.aktivitas.push({kode:'A3',setelah:'t2-2b-sudut-flip',html:`<div class='kartu'>
<b>A3. Eksitasi dan FID: sudut flip &theta; = &gamma;&middot;B1&middot;&tau; (T2.2)</b>
<p class='lembut'>Atur kekuatan B1 dan lama pulsa, atau seret bola NMV untuk mengubah sudut flip. Seret bagian lain untuk memutar bola. Pulsa diulang otomatis. Relaksasi diabaikan.</p>
<canvas id='kfl' width='520' height='310' role='img' aria-label='Vektor NMV pada bola berputar sejauh sudut flip dari sumbu z'></canvas>
<label for='sb1'>B1: <span id='vb1'></span> &micro;T</label>
<input type='range' id='sb1' min='1' max='15' step='0.1' value='5.9'>
<label for='stau'>Lama pulsa &tau;: <span id='vtau'></span> ms</label>
<input type='range' id='stau' min='0.1' max='3' step='0.05' value='1'>
<p>Sudut flip: <b><span id='vth'></span>&deg;</b>. Bagian NMV di bidang transversal: <b id='ftr'></b>. Bagian longitudinal: <b id='flo'></b>.</p>
<div class='baris'>
<button type='button' id='f90'>Atur 90&deg;</button>
<button type='button' id='f180'>Atur 180&deg;</button>
<button id='fljeda'></button><button id='flreset'></button>
</div>
<p id='flpesan' role='status' aria-live='polite' class='lembut'></p>
</div>`,init(){
// ---- Aktivitas 6: kalkulator sudut flip ----
// TODO-VERIFIKASI: satuan B1 (mikrotesla) dan tau (milidetik) dipilih agar hasil masuk akal; periksa terhadap sumber rujukan.
const kfl=$('kfl'),cfl=kfl.getContext('2d'),FL={th:0,u:0,az:1.0,el:0.3,jeda:false,drag:0,tip:[0,0],P:null,lx:0,ly:0};
function sudut(){return 360*G*parseFloat($('sb1').value)*parseFloat($('stau').value)/1000;}
function infoFlip(){
 const t=sudut();$('vb1').textContent=fmt(parseFloat($('sb1').value),1);$('vtau').textContent=fmt(parseFloat($('stau').value),2);
 $('vth').textContent=fmt(t,0);$('ftr').textContent=fmt(Math.abs(Math.sin(t*Math.PI/180))*100,0)+'%';$('flo').textContent=fmt(Math.cos(t*Math.PI/180)*100,0)+'%';
}
function aturFlip(d){
 const t=d/(360*G*parseFloat($('sb1').value)/1000),mn=parseFloat($('stau').min),mx=parseFloat($('stau').max);
 if(t<mn||t>mx){$('flpesan').textContent='Dengan B1 ini, lama pulsa yang dibutuhkan ('+fmt(t,2)+' ms) di luar jangkauan. Ubah B1 dulu.';return;}
 $('stau').value=(Math.round(t*100)/100).toString();infoFlip();$('flpesan').textContent='Lama pulsa diatur agar sudut flip mendekati '+d+'\u00b0. Perhatikan: 180\u00b0 butuh \u03c4 dua kali 90\u00b0.';
}
function gambarFlip(dt){
 const W=kfl.width,H=kfl.height,cx=W/2,cy=135,R=100,tx=warna('--teks'),lm=warna('--lembut'),t1=warna('--t1'),b0=warna('--b0'),ak=warna('--aksen');
 const pr=proyeksiBola(cx,cy,R,FL.az,FL.el),P=pr.P;FL.P=P;
 if(!FL.jeda&&!FL.drag)FL.u=(FL.u+dt/4)%1;FL.th+=(sudut()*Math.min(1,FL.u*1.6)-FL.th)*0.25;const a=FL.th*Math.PI/180;
 cfl.clearRect(0,0,W,H);cfl.font='14px system-ui,sans-serif';cfl.textAlign='left';
 gambarBola(cfl,cx,cy,R,pr);
 // sumbu z (B0)
 cfl.strokeStyle=b0;cfl.fillStyle=b0;cfl.lineWidth=3;
 let [ax,ay]=P(0,0,-1.1);cfl.beginPath();cfl.moveTo(ax,ay);[ax,ay]=P(0,0,1.3);cfl.lineTo(ax,ay);cfl.stroke();
 cfl.beginPath();cfl.moveTo(ax,ay-8);cfl.lineTo(ax-6,ay+4);cfl.lineTo(ax+6,ay+4);cfl.closePath();cfl.fill();cfl.fillText('B0 (z)',ax+10,ay+4);
 // sumbu x (B1 dalam kerangka berputar)
 cfl.strokeStyle=ak;cfl.fillStyle=ak;cfl.lineWidth=2;cfl.setLineDash([5,4]);
 [ax,ay]=P(1.3,0,0);cfl.beginPath();cfl.moveTo(cx,cy);cfl.lineTo(ax,ay);cfl.stroke();cfl.setLineDash([]);cfl.fillText('B1 (x)',ax+4,ay+14);
 // jalur NMV: busur lingkaran besar dari sumbu z sampai sudut flip
 cfl.strokeStyle=t1;cfl.lineWidth=2;cfl.globalAlpha=0.5;cfl.beginPath();
 for(let k=0;k<=40;k++){const t=a*k/40,[px,py]=P(0,-Math.sin(t),Math.cos(t));k?cfl.lineTo(px,py):cfl.moveTo(px,py);}
 cfl.stroke();cfl.globalAlpha=1;
 // vektor NMV (diputar mengelilingi sumbu x)
 const [ex,ey]=P(0,-Math.sin(a),Math.cos(a));FL.tip=[ex,ey];
 cfl.strokeStyle=t1;cfl.fillStyle=t1;cfl.lineWidth=4;cfl.beginPath();cfl.moveTo(cx,cy);cfl.lineTo(ex,ey);cfl.stroke();bola3d(cfl,ex,ey,8,t1);
 cfl.fillStyle=tx;cfl.fillText('NMV',ex+10,ey-6);
 cfl.fillStyle=lm;
 cfl.textAlign='center';['Lingkaran tengah = bidang transversal','90\u00b0: NMV di bidang transversal','180\u00b0: NMV berbalik ke -z'].forEach((t,k)=>cfl.fillText(t,W/2,262+k*18));cfl.textAlign='left';
}
$('sb1').oninput=$('stau').oninput=infoFlip;
$('f90').onclick=()=>aturFlip(90);$('f180').onclick=()=>aturFlip(180);
const sJ=tombolJeda($('fljeda'),()=>FL.jeda,v=>FL.jeda=v);
tombolReset($('flreset'),()=>{$('sb1').value='5.9';$('stau').value='1';FL.th=0;FL.u=0;FL.az=1.0;FL.el=0.3;FL.jeda=false;sJ();infoFlip();$('flpesan').textContent='';});
seret(kfl,{down:(x,y)=>{FL.drag=Math.hypot(x-FL.tip[0],y-FL.tip[1])<22?1:2;FL.lx=x;FL.ly=y;if(FL.drag===1)FL.u=.7;},
 move:(x,y)=>{if(FL.drag===1){let bt=0,bd=1e9;for(let d=0;d<=360;d+=2){const r=d*Math.PI/180,q=FL.P(0,-Math.sin(r),Math.cos(r)),e=Math.hypot(x-q[0],y-q[1]);if(e<bd){bd=e;bt=d;}}
  $('stau').value=Math.min(3,Math.max(.1,bt/(360*G*parseFloat($('sb1').value)/1000))).toFixed(2);infoFlip();}
  else{FL.az-=(x-FL.lx)*.01;FL.el=Math.max(-.2,Math.min(1.3,FL.el+(y-FL.ly)*.01));FL.lx=x;FL.ly=y;}},up:()=>{FL.drag=0;}});
infoFlip();


LOOP.push((ts,dt)=>gambarFlip(dt));
}});
