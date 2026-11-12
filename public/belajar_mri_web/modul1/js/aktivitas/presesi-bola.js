// T1.6 Presesi dan frekuensi Larmor: bola berspin yang sumbunya berpresesi mengelilingi B0
MODUL.aktivitas.push({kode:'Presesi',setelah:'t1-6-presesi-larmor',html:`<div class='kartu'>
<b>Presesi mengelilingi B0 dan frekuensi Larmor (T1.6)</b>
<p class='lembut'>Bola oranye adalah proton yang berspin pada sumbunya (titik putih di khatulistiwanya berputar). Sumbu spinnya (garis dengan panah) tidak diam: ia berputar membentuk kerucut mengelilingi arah B0 (biru). Gerak goyang kedua ini disebut presesi. Seret ujung panah sumbu spin untuk mengubah sudut miring, atau seret bagian lain untuk memutar pandangan. Ubah juga B0 atau jenis inti, lalu perhatikan apa yang berubah. Gerak digambar sekitar <span id='pre-lb'></span> juta kali lebih lambat daripada aslinya.</p>
<canvas id='pre-cv' width='520' height='290' role='img' aria-label='Bola proton berspin dengan sumbu yang berpresesi mengelilingi B0'></canvas>
<canvas id='pre-gr' width='520' height='150' style='margin-top:8px' role='img' aria-label='Grafik komponen x sumbu spin terhadap waktu'></canvas>
<label for='pre-in'>Inti</label>
<select id='pre-in'></select>
<label for='pre-b'>B0: <span id='pre-vb'></span> T</label>
<input type='range' id='pre-b' min='0.5' max='3' step='0.1' value='1.5'>
<label for='pre-th'>Sudut miring sumbu spin dari sumbu B0: <span id='pre-vth'></span>&deg;</label>
<input type='range' id='pre-th' min='10' max='90' step='1' value='35'>
<div class='pilih' id='pre-arah' role='group' aria-label='Arah spin'>
<button type='button' data-a='1' aria-pressed='true'>Spin atas (searah B0)</button>
<button type='button' data-a='-1' aria-pressed='false'>Spin bawah (berlawanan B0)</button>
</div>
<p>Frekuensi Larmor f = &gamma;&middot;B0 = <b id='pre-f'></b> MHz. Satu putaran presesi memakan <b id='pre-T'></b> ns. Pada animasi: <b id='pre-an'></b> putaran per detik.</p>
<div class='baris'>
<button id='pre-jeda'></button>
<button id='pre-rs'></button>
</div>
<p id='pre-ket' role='status' aria-live='polite' class='lembut'></p>
</div>`,init(){
// TODO-VERIFIKASI: SK (animasi 100 juta kali lebih lambat), PSI (kecepatan spin pada sumbunya), dan sudut miring adalah parameter didaktik, bukan nilai fisika. Nilai gamma inti selain 1H berasal dari BUKU di data.js.
// Arah presesi mengikuti persamaan dM/dt = gamma M x B untuk gamma positif: searah jarum jam bila dilihat dari atas (dari ujung panah B0).
const kp=$('pre-cv'),c=kp.getContext('2d'),kg=$('pre-gr'),g=kg.getContext('2d');
let AZ=0.5,EL=0.35;const L=0.95,RB=0.24,SK=0.01,PSI=3,WIN=8;   // SK: putaran animasi per detik untuk tiap MHz frekuensi Larmor
const NAMA={'1H':'hidrogen','13C':'karbon','19F':'fluor','23Na':'natrium'};
const S={tip:[0,0],phi:0,psi:0,t:0,arah:1,jeda:false,jejak:[],hist:[]};
const sel=$('pre-in');
NUK.forEach(k=>{const o=document.createElement('option');o.value=k;o.textContent=k+' ('+NAMA[k]+')';sel.appendChild(o);});
$('pre-lb').textContent=Math.round(1/SK);
const f0=()=>gam(sel.value)*parseFloat($('pre-b').value);
const th=()=>parseFloat($('pre-th').value)*Math.PI/180;
const vec=()=>[Math.sin(th())*Math.cos(S.phi),Math.sin(th())*Math.sin(S.phi),S.arah*Math.cos(th())];
const KET={
 awal:'Sumbu spin berputar mengelilingi B0, bukan jatuh ke arah B0. Putaran ini presesi.',
 inti:'Tiap inti punya rasio giromagnetik (γ) sendiri, jadi pada B0 yang sama frekuensi Larmornya berbeda.',
 b0:'B0 lebih kuat membuat presesi lebih cepat: frekuensi Larmor sebanding dengan B0. Lihat gelombang di grafik menyempit.',
 sudut:'Sudut miring hanya mengubah lebar kerucut. Waktu satu putaran, yaitu frekuensi Larmor, tidak berubah.',
 arah:'Spin atas maupun spin bawah berpresesi pada frekuensi Larmor yang sama dan searah. Hanya kerucutnya yang menghadap ke bawah.'};
function perbarui(k){
 const B=parseFloat($('pre-b').value),f=f0();
 $('pre-vb').textContent=fmt(B,1);$('pre-vth').textContent=$('pre-th').value;
 $('pre-f').textContent=fmt(f);$('pre-T').textContent=fmt(1000/f,1);$('pre-an').textContent=fmt(f*SK);
 document.querySelectorAll('#pre-arah button').forEach(b=>b.setAttribute('aria-pressed',+b.dataset.a===S.arah));
 sJ();
 $('pre-ket').textContent=KET[k||'awal'];
 kp.setAttribute('aria-label','Bola proton berspin '+(S.arah>0?'atas':'bawah')+' dengan sumbu miring '+$('pre-th').value+' derajat yang berpresesi mengelilingi B0. Frekuensi Larmor '+fmt(f)+' MHz.');
}
function panah(x0,y0,x1,y1,col,w){
 c.strokeStyle=c.fillStyle=col;c.lineWidth=w;c.lineCap='round';c.beginPath();c.moveTo(x0,y0);c.lineTo(x1,y1);c.stroke();
 const an=Math.atan2(y1-y0,x1-x0);c.beginPath();c.moveTo(x1+Math.cos(an)*9,y1+Math.sin(an)*9);c.lineTo(x1-Math.sin(an)*6,y1+Math.cos(an)*6);c.lineTo(x1+Math.sin(an)*6,y1-Math.cos(an)*6);c.closePath();c.fill();
}
function gambar(){
 const W=kp.width,H=kp.height,cx=W/2,cy=150,R=100;
 const pr=proyeksiBola(cx,cy,R,AZ,EL),P=pr.P,dep=pr.dep;
 const tx=warna('--teks'),lm=warna('--lembut'),b0=warna('--b0'),t1=warna('--t1'),t2=warna('--t2');
 const sn=Math.sin(th()),zc=S.arah*Math.cos(th()),a=vec(),tip=a.map(v=>v*L),[px,py]=P(...tip);
 const cone=q=>[L*sn*Math.cos(q),L*sn*Math.sin(q),L*zc];
 const busur=depan=>{c.beginPath();for(let k=0;k<72;k++){const q0=cone(k/72*6.2832),q1=cone((k+1)/72*6.2832),m=q0.map((v,i)=>(v+q1[i])/2);
  if((dep(...m)<=0)===depan){const[x0,y0]=P(...q0),[x1,y1]=P(...q1);c.moveTo(x0,y0);c.lineTo(x1,y1);}}c.stroke();};
 c.clearRect(0,0,W,H);c.font='14px system-ui,sans-serif';c.textAlign='left';c.lineCap='round';
 gambarBola(c,cx,cy,R,pr);
 S.tip=[px,py];if(!S.jeda){S.jejak.push([px,py]);if(S.jejak.length>50)S.jejak.shift();}
 // sumbu bidang spin: u dan v tegak lurus sumbu spin, dipakai untuk khatulistiwa bola dan titik penanda spin
 const h=Math.hypot(a[0],a[1])||1,u=[a[1]/h,-a[0]/h,0],v=[a[1]*u[2]-a[2]*u[1],a[2]*u[0]-a[0]*u[2],a[0]*u[1]-a[1]*u[0]];
 const kh=q=>[RB*(Math.cos(q)*u[0]+Math.sin(q)*v[0]),RB*(Math.cos(q)*u[1]+Math.sin(q)*v[1]),RB*(Math.cos(q)*u[2]+Math.sin(q)*v[2])];
 const it=[];   // digambar dari yang paling jauh (dep besar) ke yang paling dekat
 it.push({d:99,f:()=>{c.strokeStyle=t1;c.lineWidth=1;c.globalAlpha=.18;c.beginPath();for(let k=0;k<12;k++){const[x,y]=P(...cone(k/12*6.2832));c.moveTo(cx,cy);c.lineTo(x,y);}c.stroke();
  c.globalAlpha=.5;c.lineWidth=2;c.setLineDash([4,4]);busur(false);c.setLineDash([]);c.globalAlpha=1;}});
 it.push({d:-99,f:()=>{c.strokeStyle=t1;c.lineWidth=2;c.globalAlpha=.9;busur(true);c.globalAlpha=1;}});
 it.push({d:-98,f:()=>{c.lineWidth=3;c.strokeStyle=t1;for(let k=1;k<S.jejak.length;k++){c.globalAlpha=k/S.jejak.length*.7;c.beginPath();c.moveTo(S.jejak[k-1][0],S.jejak[k-1][1]);c.lineTo(S.jejak[k][0],S.jejak[k][1]);c.stroke();}c.globalAlpha=1;}});
 // B0 sepanjang z: separuh bawah di belakang bola, separuh atas di depan
 it.push({d:dep(0,0,-.5),f:()=>{const[x,y]=P(0,0,-1.15);c.strokeStyle=b0;c.lineWidth=3;c.beginPath();c.moveTo(x,y);c.lineTo(cx,cy);c.stroke();}});
 it.push({d:dep(0,0,.5),f:()=>{const[x,y]=P(0,0,1.4);panah(cx,cy,x,y,b0,3);c.fillStyle=b0;c.fillText('B0 (sumbu z)',x+12,y+6);}});
 // proyeksi sumbu spin ke bidang transversal
 it.push({d:dep(tip[0],tip[1],0),f:()=>{const[x,y]=P(tip[0],tip[1],0);c.strokeStyle=t2;c.lineWidth=2;c.setLineDash([4,4]);c.beginPath();c.moveTo(cx,cy);c.lineTo(x,y);c.stroke();
  c.globalAlpha=.5;c.strokeStyle=lm;c.lineWidth=1;c.setLineDash([2,3]);c.beginPath();c.moveTo(x,y);c.lineTo(px,py);c.stroke();c.setLineDash([]);c.globalAlpha=1;}});
 // sumbu spin: separuh selatan polos, separuh utara berpanah
 it.push({d:dep(-a[0],-a[1],-a[2]),f:()=>{const[x,y]=P(-a[0]*.6,-a[1]*.6,-a[2]*.6);c.strokeStyle=tx;c.lineWidth=3;c.beginPath();c.moveTo(cx,cy);c.lineTo(x,y);c.stroke();}});
 it.push({d:dep(...a),f:()=>{panah(cx,cy,px,py,tx,3);const dx=px-cx,dy=py-cy,n=Math.hypot(dx,dy)||1;c.fillStyle=tx;c.textAlign=dx>=0?'left':'right';c.fillText('sumbu spin',px+dx/n*14+(dx>=0?0:0),py+dy/n*14+4);c.textAlign='left';}});
 // bola proton: khatulistiwa dan titik penanda yang berputar pada sumbunya sendiri
 it.push({d:0,f:()=>{bola3d(c,cx,cy,RB*R,t1);
  c.strokeStyle='#fff';c.globalAlpha=.8;c.lineWidth=1.5;c.beginPath();
  for(let k=0;k<48;k++){const q0=kh(k/48*6.2832),q1=kh((k+1)/48*6.2832),m=q0.map((w,i)=>(w+q1[i])/2);if(dep(...m)<=0){const[x0,y0]=P(...q0),[x1,y1]=P(...q1);c.moveTo(x0,y0);c.lineTo(x1,y1);}}
  c.stroke();c.globalAlpha=1;
  const m=kh(S.psi);if(dep(...m)<=0){const[x,y]=P(...m);c.fillStyle='#fff';c.beginPath();c.arc(x,y,3.5,0,6.2832);c.fill();}}});
 it.sort((p,q)=>q.d-p.d).forEach(o=>o.f());
 // legenda di tengah bawah
 c.font='12px system-ui,sans-serif';const LG=[[b0,'B0 (sumbu z)',0],[tx,'sumbu spin proton',0],[t1,'lintasan presesi',1],[t2,'komponen transversal',1]],wd=LG.map(q=>c.measureText(q[1]).width+48);let lx=(W-wd.reduce((a,b)=>a+b,0))/2;
 LG.forEach((q,i)=>{const y=H-12;c.strokeStyle=q[0];c.lineWidth=2.5;c.setLineDash(q[2]?[4,3]:[]);c.beginPath();c.moveTo(lx,y);c.lineTo(lx+22,y);c.stroke();c.setLineDash([]);c.fillStyle=lm;c.textAlign='left';c.fillText(q[1],lx+28,y+4);lx+=wd[i];});
}
function gambarGrafik(){
 const W=kg.width,H=kg.height,m=70,A=38,lm=warna('--lembut');
 if(!S.jeda){S.hist.push([S.t,vec()[0]]);while(S.hist.length&&S.hist[0][0]<S.t-WIN)S.hist.shift();}
 g.clearRect(0,0,W,H);
 g.strokeStyle=warna('--garis');g.lineWidth=1;g.beginPath();g.moveTo(0,m);g.lineTo(W,m);g.stroke();
 g.fillStyle=lm;g.font='12px system-ui,sans-serif';g.textAlign='left';g.fillText('komponen x sumbu spin terhadap waktu animasi ('+WIN+' detik terakhir)',8,16);
 g.strokeStyle=warna('--t2');g.lineWidth=2;g.beginPath();
 S.hist.forEach((q,i)=>{const x=W-(S.t-q[0])/WIN*W,y=m-q[1]*A;i?g.lineTo(x,y):g.moveTo(x,y);});g.stroke();
 if(S.hist.length){const q=S.hist[S.hist.length-1];bola3d(g,W-(S.t-q[0])/WIN*W,m-q[1]*A,5,warna('--t2'));}
 // penanda satu putaran presesi (bila muat dalam grafik)
 const Tw=1/(f0()*SK)/WIN*W;
 if(Tw<W-40){const x1=W-14,x0=x1-Tw,y=H-10;g.strokeStyle=lm;g.lineWidth=1.5;g.beginPath();g.moveTo(x0,y-5);g.lineTo(x0,y);g.lineTo(x1,y);g.lineTo(x1,y-5);g.stroke();g.textAlign='center';g.fillText('1 putaran',(x0+x1)/2,y-9);}
}
sel.onchange=()=>{S.jejak=[];perbarui('inti');};
$('pre-b').oninput=()=>perbarui('b0');
$('pre-th').oninput=()=>{S.jejak=[];perbarui('sudut');};
document.querySelectorAll('#pre-arah button').forEach(b=>b.onclick=()=>{S.arah=+b.dataset.a;S.jejak=[];perbarui('arah');});
const sJ=tombolJeda($('pre-jeda'),()=>S.jeda,v=>{S.jeda=v;$('pre-ket').textContent=v?'Animasi dijeda.':KET.awal;});
tombolReset($('pre-rs'),()=>{AZ=0.5;EL=0.35;S.phi=0;S.psi=0;S.t=0;S.arah=1;S.jeda=false;S.jejak=[];S.hist=[];sel.value='1H';$('pre-b').value='1.5';$('pre-th').value='35';sJ();perbarui('awal');});
let mode=0,lx=0,ly=0;
seret(kp,{down:(x,y)=>{mode=Math.hypot(x-S.tip[0],y-S.tip[1])<24?1:2;lx=x;ly=y;},
 move:(x,y)=>{if(mode===1){const Pq=proyeksiBola(kp.width/2,150,100,AZ,EL).P;let bd=1e9,bt=35;
  for(let d=10;d<=90;d++){const t=d*Math.PI/180,q=Pq(L*Math.sin(t)*Math.cos(S.phi),L*Math.sin(t)*Math.sin(S.phi),S.arah*L*Math.cos(t)),e=Math.hypot(x-q[0],y-q[1]);if(e<bd){bd=e;bt=d;}}
  $('pre-th').value=bt;S.jejak=[];perbarui('sudut');}
  else{AZ-=(x-lx)*.01;EL=Math.max(-.2,Math.min(1.3,EL+(y-ly)*.01));lx=x;ly=y;S.jejak=[];}}});
perbarui('awal');

LOOP.push((ts,dt)=>{
 if(!S.jeda){S.phi-=2*Math.PI*f0()*SK*dt;S.psi+=2*Math.PI*PSI*dt;S.t+=dt;}
 gambar();gambarGrafik();
});
}});
