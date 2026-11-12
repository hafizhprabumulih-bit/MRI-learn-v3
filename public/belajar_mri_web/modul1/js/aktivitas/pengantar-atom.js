// Pengantar: dari air ke proton
MODUL.aktivitas.push({kode:'Pengantar',setelah:'t1-4b-inti-aktif-mr',html:`<div class='kartu'>
<b>Pengantar: dari air ke proton (mendukung T1.3, T1.4)</b>
<p class='lembut'>Jelajahi empat langkah, dari molekul air sampai magnet kecil di dalam B0. Gambar disederhanakan dan tidak berskala.</p>
<div class='pilih' id='pil-at' role='group' aria-label='Kedalaman penjelasan Pengantar'>
<button type='button' data-mode='singkat' aria-pressed='true'>Materi singkat</button>
<button type='button' data-mode='lengkap' aria-pressed='false'>Materi lengkap</button>
<button type='button' data-mode='analogi' aria-pressed='false'>Analogi</button>
</div>
<canvas id='ka' class='klik' width='520' height='260' role='img' aria-label='Animasi molekul air, atom hidrogen, spin proton, dan proton di dalam medan magnet B0'></canvas>
<p><b id='alangkah'></b></p>
<p id='atxt' role='status' aria-live='polite'></p>
<div class='baris'>
<button type='button' id='aprev'>&larr; Sebelumnya</button>
<button type='button' id='anext'>Berikutnya &rarr;</button>
<button type='button' id='ab0' aria-pressed='false'>Nyalakan B0</button>
<button id='ajeda'></button><button id='areset'></button>
</div>
</div>`,init(){
let ATM='singkat';
// ---- Pengantar: dari air ke proton ----
const AT={l:0,b0:false,k:0,f:0,kick:[0,0,0,0,0],jeda:false,T:0,ox:[0,0,0,0,0],oy:[0,0,0,0,0],alfa:[-2.2,0.9,2.6,-0.7,1.7]};
const ATT=[
 ['Molekul air','Tubuh banyak mengandung air dan lemak. Satu molekul air punya dua atom hidrogen (H). Dari atom inilah sinyal MRI berasal.','Hidrogen adalah atom paling melimpah di tubuh manusia. Selain di air (dua H dan satu O), hidrogen juga ada di lemak, bersama atom karbon dan oksigen.'],
 ['Atom hidrogen','Atom hidrogen paling sederhana: satu proton di inti dan satu elektron di sekelilingnya. Elektron berupa awan, bukan lintasan tetap. Inti sebenarnya jauh lebih kecil daripada awannya.','Inti sangat kecil tetapi memuat hampir seluruh massa atom. Isinya proton (bermuatan positif) dan neutron (tanpa muatan), sedangkan elektron bermuatan negatif. Jumlah proton adalah nomor atom, jumlah proton ditambah neutron adalah nomor massa.'],
 ['Proton berspin','Proton punya sifat spin, seolah berputar pada sumbunya. Karena bermuatan, spin membuatnya berperilaku seperti magnet kecil dengan kutub utara (U) dan selatan (S). MRI memanfaatkan magnet kecil ini.','Jika proton dan neutron sama-sama genap, putarannya saling meniadakan dan tidak ada spin neto (contoh karbon-12). Inti bernomor massa ganjil, seperti hidrogen-1, punya spin neto. Karena bermuatan, spin itu menghasilkan momen magnetik.'],
 ['Proton di dalam B0','Tanpa B0, magnet-magnet kecil menghadap arah sembarang. Dengan B0, mereka cenderung menyearah dan berpresesi mengelilingi arah B0 pada frekuensi Larmor. Ingat, kelebihan yang searah sebenarnya sangat kecil dan di sini digambar disederhanakan.','Inti aktif MR cenderung menyelaraskan sumbu putarnya dengan medan magnet luar. Itulah yang Anda lihat saat B0 dinyalakan.']
];
const ATA=[
 'Tubuh seperti kota besar yang penuh penduduk bernama hidrogen. MRI tidak menghitung seluruh kota, hanya mendengarkan "suara" penduduk H itu.',
 'Inti ibarat kelereng di tengah stadion. Elektron seperti baling-baling kipas yang berputar cepat: yang terlihat lingkaran kabur (awan), bukan satu titik pasti.',
 'Proton ibarat gasing kecil bermuatan. Muatan yang berputar seperti arus dalam kumparan, sehingga gasing itu menjadi magnet mini.',
 'Magnet kecil seperti jarum kompas di dekat magnet besar. Ia cenderung menyearah, tetapi tidak diam: ia bergoyang mengelilingi arah medan.'
];
const ka=$('ka'),ca=ka.getContext('2d');
function setAtom(){
 $('alangkah').textContent='Langkah '+(AT.l+1)+' dari '+ATT.length+': '+ATT[AT.l][0];
 $('atxt').textContent=ATT[AT.l][1]+(ATM==='lengkap'?' '+ATT[AT.l][2]:'')+(ATM==='analogi'?' Analogi: '+ATA[AT.l]:'')+(AT.l===3?(AT.b0?' B0 menyala: magnet kecil berpresesi mengelilingi arah B0. Seret atau klik sebuah proton untuk mengusiknya.':' Tekan tombol Nyalakan B0, lalu amati perubahannya. Klik sebuah proton untuk mengacak arahnya.'):'');
 $('aprev').disabled=AT.l===0;$('anext').disabled=AT.l===ATT.length-1;if(AT.l!==3)AT.b0=false;AT.f=0;$('ab0').hidden=AT.l!==3;$('ab0').className=(AT.l===3&&!AT.b0)?'denyut muncul':(AT.l===3?'muncul':'');
 $('ab0').textContent=AT.b0?'Matikan B0':'Nyalakan B0';$('ab0').setAttribute('aria-pressed',AT.b0);ka.style.cursor=AT.l===3?'pointer':'default';
}
function gambarAtom(ts){
 AT.k+=((AT.b0?1:0)-AT.k)*0.06;
 const W=ka.width,H=ka.height,cx=W/2,cy=H/2,t=ts/1000;
 const t1=warna('--t1'),t2=warna('--t2'),b0=warna('--b0'),tx=warna('--teks'),lm=warna('--lembut');
 ca.clearRect(0,0,W,H);ca.font='14px system-ui,sans-serif';ca.textAlign='center';
 const bola=(x,y,r,c,l)=>{ca.fillStyle=c;ca.beginPath();ca.arc(x,y,r,0,2*Math.PI);ca.fill();if(l){ca.fillStyle='#fff';ca.fillText(l,x,y+5);}};
 if(AT.l===0){
  const g=Math.sin(t*6)*2;
  ca.strokeStyle=lm;ca.lineWidth=5;
  [-1,1].forEach(s=>{ca.beginPath();ca.moveTo(cx,cy-25);ca.lineTo(cx+s*(70+g),cy+35);ca.stroke();});
  bola(cx,cy-25,34,t2,'O');
  [-1,1].forEach(s=>{const x=cx+s*(70+g),y=cy+35;ca.strokeStyle=t1;ca.lineWidth=2;ca.globalAlpha=0.5+0.5*Math.sin(t*3);ca.beginPath();ca.arc(x,y,25,0,2*Math.PI);ca.stroke();ca.globalAlpha=1;bola(x,y,17,t1,'H');});
  ca.fillStyle=tx;ca.fillText('H\u2082O: tiap H membawa satu proton',cx,H-16);
 }else if(AT.l===1){
  ca.fillStyle=b0;ca.globalAlpha=0.12;ca.beginPath();ca.arc(cx,cy,100,0,2*Math.PI);ca.fill();ca.globalAlpha=0.12;ca.beginPath();ca.arc(cx,cy,65,0,2*Math.PI);ca.fill();ca.globalAlpha=1;
  for(let k=6;k>=0;k--){const a=t*4-k*0.18,r=62+28*Math.sin(a*0.33),x=cx+r*Math.cos(a),y=cy+r*0.8*Math.sin(a);ca.globalAlpha=k?0.5-k*0.06:1;bola(x,y,k?4:6,b0);if(!k){ca.globalAlpha=1;ca.fillStyle=tx;ca.fillText('elektron',x,y-12);}}
  ca.globalAlpha=1;bola(cx,cy,12,t1,'+');
  ca.fillStyle=tx;ca.fillText('proton (inti)',cx,cy+32);ca.fillStyle=lm;ca.fillText('awan elektron',cx,H-16);
 }else if(AT.l===2){
  const px=cx-90;
  bola(px,cy,58,t1);
  ca.strokeStyle='#fff';ca.lineWidth=2;ca.globalAlpha=0.6;ca.beginPath();ca.ellipse(px,cy,58,16,0,0,2*Math.PI);ca.stroke();ca.globalAlpha=1;
  const a=t*3;bola(px+58*Math.cos(a),cy+16*Math.sin(a),6,'#fff');
  ca.strokeStyle=tx;ca.fillStyle=tx;ca.lineWidth=2;ca.beginPath();ca.moveTo(px,cy+95);ca.lineTo(px,cy-95);ca.stroke();
  ca.beginPath();ca.moveTo(px,cy-105);ca.lineTo(px-6,cy-92);ca.lineTo(px+6,cy-92);ca.closePath();ca.fill();
  ca.fillText('sumbu spin',px,H-16);
  ca.fillStyle=lm;ca.fillText('setara dengan',cx+40,cy+5);
  ca.lineCap='butt';ca.lineWidth=36;ca.strokeStyle=t1;ca.beginPath();ca.moveTo(cx+150,cy-50);ca.lineTo(cx+150,cy);ca.stroke();
  ca.strokeStyle=b0;ca.beginPath();ca.moveTo(cx+150,cy);ca.lineTo(cx+150,cy+50);ca.stroke();
  ca.fillStyle='#fff';ca.fillText('U',cx+150,cy-20);ca.fillText('S',cx+150,cy+30);
  ca.fillStyle=tx;ca.fillText('magnet kecil',cx+150,H-16);
 }else{
  const L=34,r=15,top=40,bot=H-52;
  if(AT.k>0.02){ // garis medan B0 dengan panah mengalir ke atas
   ca.strokeStyle=ca.fillStyle=b0;ca.lineWidth=1.5;ca.globalAlpha=AT.k*0.45;ca.setLineDash([4,5]);
   for(let i=0;i<5;i++){const x=W*(i+1)/6+10;ca.beginPath();ca.moveTo(x,bot);ca.lineTo(x,top);ca.stroke();}
   ca.setLineDash([]);ca.globalAlpha=AT.k*0.8;
   for(let i=0;i<5;i++){const x=W*(i+1)/6+10;for(let m=0;m<4;m++){const y=bot-((m*44+t*40)%176);ca.beginPath();ca.moveTo(x,y-6);ca.lineTo(x-5,y+3);ca.lineTo(x+5,y+3);ca.closePath();ca.fill();}}
   ca.globalAlpha=1;
  }
  for(let i=0;i<5;i++){
   AT.kick[i]*=0.97;const kk=AT.kick[i];
   const x=W*(i+1)/6+10+AT.ox[i],y=cy+AT.oy[i],a=AT.alfa[i]+(0.35+0.8*kk)*Math.sin(t*(1.5+3*kk)+i),tilt=0.5+0.5*kk,ph=t*4+i*1.3;
   const ox=Math.sin(a),oy=-Math.cos(a),nx=Math.sin(tilt)*Math.cos(ph),ny=-Math.cos(tilt)+0.15*Math.sin(tilt)*Math.sin(ph);
   const dx=(1-AT.k)*ox+AT.k*nx,dy=(1-AT.k)*oy+AT.k*ny;
   if(AT.k>0.3){ // lintasan kerucut presesi
    ca.strokeStyle=b0;ca.globalAlpha=AT.k*0.5;ca.lineWidth=1.5;ca.beginPath();ca.ellipse(x,y-Math.cos(tilt)*L,Math.sin(tilt)*L,0.15*Math.sin(tilt)*L,0,0,6.2832);ca.stroke();ca.globalAlpha=1;
   }
   if(kk>0.05){ca.strokeStyle=t1;ca.globalAlpha=Math.min(1,kk);ca.lineWidth=2;ca.beginPath();ca.arc(x,y,r+(1-kk)*30,0,6.2832);ca.stroke();ca.globalAlpha=1;}
   garisBola(ca,x,y,r,dx*L,dy*L,t1,true);
  }
  ca.globalAlpha=0.25+0.75*AT.k;ca.strokeStyle=b0;ca.fillStyle=b0;ca.lineWidth=4;ca.beginPath();ca.moveTo(26,H-40);ca.lineTo(26,38);ca.stroke();
  ca.beginPath();ca.moveTo(26,26);ca.lineTo(19,40);ca.lineTo(33,40);ca.closePath();ca.fill();ca.fillText('B0',26,H-22);ca.globalAlpha=1;
  ca.fillStyle=tx;ca.fillText(AT.b0?'Presesi mengelilingi arah B0':'Tanpa B0: arah acak',cx+10,H-16);
 }
 AT.f=Math.min(1,AT.f+0.07); // transisi halus antarlangkah
 if(AT.f<1){ca.globalAlpha=1-AT.f;ca.fillStyle=warna('--permukaan');ca.fillRect(0,0,W,H);ca.globalAlpha=1;}
}
$('aprev').onclick=()=>{if(AT.l>0){AT.l--;setAtom();}};
$('anext').onclick=()=>{if(AT.l<ATT.length-1){AT.l++;setAtom();}};
$('ab0').onclick=()=>{AT.b0=!AT.b0;setAtom();};
const sJ=tombolJeda($('ajeda'),()=>AT.jeda,v=>AT.jeda=v);
tombolReset($('areset'),()=>{AT.l=0;AT.b0=false;AT.k=0;AT.kick.fill(0);AT.ox.fill(0);AT.oy.fill(0);AT.jeda=false;sJ();setAtom();});
let dr=-1;
seret(ka,{down:(x,y)=>{if(AT.l!==3)return false;dr=-1;let bd=45;for(let i=0;i<5;i++){const d=Math.hypot(x-(ka.width*(i+1)/6+10+AT.ox[i]),y-(ka.height/2+AT.oy[i]));if(d<bd){bd=d;dr=i;}}
 if(dr<0)return false;AT.kick[dr]=1;if(!AT.b0)AT.alfa[dr]=Math.random()*6.28-3.14;},
 move:(x,y)=>{AT.ox[dr]=Math.max(20,Math.min(ka.width-20,x))-(ka.width*(dr+1)/6+10);AT.oy[dr]=Math.max(40,Math.min(ka.height-40,y))-ka.height/2;},up:()=>{dr=-1;}});
setAtom();
document.querySelectorAll('#pil-at button').forEach(b=>{b.onclick=()=>{ATM=b.dataset.mode;
 document.querySelectorAll('#pil-at button').forEach(x=>x.setAttribute('aria-pressed',x===b));setAtom();};});

LOOP.push((ts,dt)=>{if(!AT.jeda)AT.T+=dt;gambarAtom(AT.T*1000);});
}});
