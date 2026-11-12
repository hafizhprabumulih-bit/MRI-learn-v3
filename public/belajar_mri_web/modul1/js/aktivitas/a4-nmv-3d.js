// A4 NMV 3D: kerangka lab vs berputar
MODUL.aktivitas.push({kode:'A4',setelah:'t2-2a-nutasi',html:`<div class='kartu'>
<b>A4. NMV 3D: kerangka laboratorium vs kerangka berputar (T1.6, T2.2, T3.5; juga bagian A3)</b>
<p class='lembut'>Gerak presesi digambar jauh lebih lambat daripada aslinya. Siklus pulsa 90°, FID, lalu M kembali ke sumbu z berjalan otomatis (kembali ke sumbu z hanya ilustrasi; relaksasi T1 dibahas di Modul 2). Seret bola M untuk memiringkannya, seret bagian lain untuk memutar bola.</p>
<canvas id='kv' width='520' height='300' role='img' aria-label='Vektor magnetisasi pada permukaan bola, berpresesi mengelilingi sumbu z (arah B0)'></canvas>
<label for='sb0'>B0: <span id='vb0'></span> T</label>
<input type='range' id='sb0' min='0.5' max='3' step='0.1' value='1.5'>
<label for='srf'>Frekuensi RF: <span id='vrf'></span> MHz</label>
<input type='range' id='srf' min='20' max='140' step='0.1' value='63.9'>
<p>Frekuensi Larmor: <b><span id='vl'></span> MHz</b>. Status: <span id='res' class='lencana'></span></p>
<div class='baris'>
<button type='button' id='bsama'>Samakan RF dengan Larmor</button>
<span id='tunggu' class='lembut'>Pilihan pulsa muncul setelah frekuensi RF sama dengan Larmor.</span>
<button type='button' id='b90'>Pulsa 90&deg;</button>
<button type='button' id='b180'>Pulsa 180&deg;</button>
<button id='bjeda'></button>
<button type='button' id='brot' aria-pressed='false'>Kerangka berputar: mati</button>
<button id='breset'></button>
</div>
<p id='pesan' role='status' aria-live='polite' class='lembut'></p>
<canvas id='kf' width='520' height='140' role='img' aria-label='Grafik sinyal FID setelah pulsa 90 derajat'></canvas>
<p class='lembut'>Grafik FID: tampil setelah pulsa 90&deg; pada kondisi resonansi.</p>
</div>`,init(){
// ---- Aktivitas 1: vektor presesi ----
// TODO-VERIFIKASI: toleransi resonansi 0,5 MHz dan faktor perlambatan animasi hanyalah parameter didaktik, bukan nilai fisika.
let AZ=0.5,EL=0.35; // sudut pandang kamera bola
const TOL=0.5,LAMBAT=0.02,T_FID=2,F_FID=8,TAU_FID=0.6;
const S={phi:0,theta:0,env:1,pulse:null,fid:null,jeda:false,jejak:[],rot:false,drag:false,tip:[0,0],wait:0};
const kv=$('kv'),cv=kv.getContext('2d'),kf=$('kf'),cf=kf.getContext('2d');
function f0(){return larmor(parseFloat($('sb0').value));}
function resonan(){return Math.abs(parseFloat($('srf').value)-f0())<=TOL;}
function perbarui(){
 $('vb0').textContent=fmt(parseFloat($('sb0').value),1);
 $('vrf').textContent=fmt(parseFloat($('srf').value),1);
 $('vl').textContent=fmt(f0());
 const r=resonan(),e=$('res');
 e.textContent=r?'Resonan':'Tidak resonan';e.className='lencana '+(r?'ok':'no');
 // pilihan pulsa hanya tampil bila RF = Larmor
 $('b90').hidden=$('b180').hidden=!r;$('tunggu').hidden=r;
}
function gambarVektor(){
 const W=kv.width,H=kv.height,cx=W/2,cy=H*0.55,R=105;
 const pr=proyeksiBola(cx,cy,R,AZ,EL),P=pr.P;
 const tinta=warna('--teks'),lembut=warna('--lembut'),b0=warna('--b0'),t1=warna('--t1'),t2=warna('--t2');
 cv.clearRect(0,0,W,H);
 gambarBola(cv,cx,cy,R,pr);
 cv.lineWidth=1;cv.strokeStyle=lembut;cv.fillStyle=lembut;cv.font='14px system-ui,sans-serif';
  let [ax,ay]=P(1.2,0,0);cv.beginPath();cv.moveTo(cx,cy);cv.lineTo(ax,ay);cv.stroke();cv.fillText('x',ax+4,ay+4);
 [ax,ay]=P(0,1.2,0);cv.beginPath();cv.moveTo(cx,cy);cv.lineTo(ax,ay);cv.stroke();cv.fillText('y',ax+4,ay+4);
 // B0 sepanjang z
 cv.strokeStyle=b0;cv.fillStyle=b0;cv.lineWidth=3;
 [ax,ay]=P(0,0,-1.1);cv.beginPath();cv.moveTo(ax,ay);[ax,ay]=P(0,0,1.35);cv.lineTo(ax,ay);cv.stroke();
 cv.beginPath();cv.moveTo(ax,ay-8);cv.lineTo(ax-6,ay+4);cv.lineTo(ax+6,ay+4);cv.closePath();cv.fill();
 cv.fillText('B0 (sumbu z)',ax+10,ay+4);
 // vektor magnetisasi
 const st=Math.sin(S.theta)*S.env,z=Math.cos(S.theta);
 const [mx,my]=P(st*Math.cos(S.phi),st*Math.sin(S.phi),z);
 cv.strokeStyle=t1;cv.fillStyle=t1;cv.lineWidth=4;
 cv.beginPath();cv.moveTo(cx,cy);cv.lineTo(mx,my);cv.stroke();
 bola3d(cv,mx,my,8,t1);S.tip=[mx,my];
 if(S.pulse){const u=(performance.now()%700)/700;cv.strokeStyle=t1;cv.globalAlpha=1-u;cv.lineWidth=2;cv.beginPath();cv.arc(mx,my,8+u*28,0,6.2832);cv.stroke();cv.globalAlpha=1;}
 cv.fillStyle=tinta;cv.fillText('M',mx+10,my-6);
 // jejak presesi
 if(!S.jeda){S.jejak.push([mx,my]);if(S.jejak.length>70)S.jejak.shift();}
 cv.lineWidth=2;cv.strokeStyle=t1;
 for(let k=1;k<S.jejak.length;k++){cv.globalAlpha=k/S.jejak.length*0.6;cv.beginPath();cv.moveTo(S.jejak[k-1][0],S.jejak[k-1][1]);cv.lineTo(S.jejak[k][0],S.jejak[k][1]);cv.stroke();}
 cv.globalAlpha=1;
 // gelombang RF saat pulsa
 if(S.pulse){const w=warna('--aksen');cv.strokeStyle=w;cv.fillStyle=w;cv.lineWidth=2;cv.beginPath();for(let x=0;x<=150;x++){const y=30+12*Math.sin(x/6-performance.now()/60);x?cv.lineTo(185+x,y):cv.moveTo(185+x,y);}cv.stroke();cv.fillText('Pulsa RF',185,58);}
 if(S.rot){cv.fillStyle=lembut;cv.textAlign='center';cv.fillText('Kerangka berputar: aktif',W/2,H-6);cv.textAlign='left';}
 // proyeksi transversal
 if(st>0.02){const [px,py]=P(st*Math.cos(S.phi),st*Math.sin(S.phi),0);cv.setLineDash([4,4]);cv.strokeStyle=t2;cv.lineWidth=2;cv.beginPath();cv.moveTo(cx,cy);cv.lineTo(px,py);cv.stroke();cv.setLineDash([]);}
}
function gambarFID(el){
 const W=kf.width,H=kf.height;
 cf.clearRect(0,0,W,H);
 cf.strokeStyle=warna('--garis');cf.lineWidth=1;cf.beginPath();cf.moveTo(0,H/2);cf.lineTo(W,H/2);cf.stroke();
 if(!S.fid)return;
 const amp=S.fid.amp;
 cf.strokeStyle=warna('--t2');cf.lineWidth=2;cf.beginPath();let lx,ly;
 for(let x=0;x<=W;x++){const t=x/W*T_FID;if(t>el)break;const y=H/2-(H/2-8)*amp*Math.exp(-t/TAU_FID)*Math.cos(2*Math.PI*F_FID*t);x?cf.lineTo(x,y):cf.moveTo(x,y);lx=x;ly=y;}
 cf.stroke();
 if(lx!==undefined&&el<T_FID)bola3d(cf,lx,ly,5,warna('--t2'));
}
function langkah(ts,dt){
 if(!S.jeda)S.phi+=2*Math.PI*(S.rot?f0()-parseFloat($('srf').value):f0())*LAMBAT*dt;
 if(S.pulse){
  S.theta+=2*dt;
  if(S.theta>=S.pulse.target){S.theta=S.pulse.target;S.pulse=null;
   const a=Math.abs(Math.sin(S.theta));
   if(a>0.05){S.fid={amp:a,t0:ts};S.env=1;}else{S.fid=null;S.env=1;}
   $('pesan').textContent=a>0.05?'Ada komponen transversal. RF coil menerima sinyal FID yang meluruh.':'Magnetisasi sejajar sumbu z, tidak ada komponen transversal, sehingga tidak ada sinyal FID.';}
 }
 let el=0;
 if(S.fid){el=Math.min(T_FID,(ts-S.fid.t0)/1000);S.env=Math.exp(-el/TAU_FID);}
 if(!S.jeda&&!S.pulse&&!S.drag&&(!S.fid||el>=T_FID)){   // siklus otomatis: pulihkan M ke sumbu z, tunggu, beri pulsa 90° lagi
  if(Math.abs(S.theta)>0.01)S.theta*=Math.exp(-2.5*dt);else{S.theta=0;S.wait+=dt;if(S.wait>1){S.wait=0;if(resonan())pulsa(90);}}
 }
 return el;
}
function pulsa(derajat){
 if(S.pulse)return;
 if(!resonan()){$('pesan').textContent='Pulsa tidak berpengaruh karena frekuensi RF di luar resonansi. Samakan RF dengan frekuensi Larmor.';return;}
 S.fid=null;S.env=1;
 S.pulse={target:S.theta+derajat*Math.PI/180};
 $('pesan').textContent='Pulsa '+derajat+'\u00b0 diberikan.';
}
$('sb0').oninput=perbarui;$('srf').oninput=perbarui;
$('bsama').onclick=()=>{$('srf').value=f0().toFixed(1);perbarui();$('pesan').textContent='RF disamakan dengan frekuensi Larmor.';};
$('b90').onclick=()=>pulsa(90);
$('b180').onclick=()=>pulsa(180);
const sJ=tombolJeda($('bjeda'),()=>S.jeda,v=>S.jeda=v);
tombolReset($('breset'),()=>{S.phi=0;S.theta=0;S.env=1;S.pulse=null;S.fid=null;S.jeda=false;S.jejak=[];S.rot=false;S.wait=0;AZ=0.5;EL=0.35;
 $('sb0').value='1.5';$('srf').value='63.9';sJ();
 $('brot').textContent='Kerangka berputar: mati';$('brot').setAttribute('aria-pressed','false');
 $('pesan').textContent='Kembali ke keadaan awal.';perbarui();});
let dm=0,lx=0,ly=0;
seret(kv,{down:(x,y)=>{dm=Math.hypot(x-S.tip[0],y-S.tip[1])<24?1:2;lx=x;ly=y;if(dm===1){S.drag=true;S.pulse=null;S.fid=null;S.env=1;}},
 move:(x,y)=>{if(dm===1){const Pq=proyeksiBola(kv.width/2,kv.height*.55,105,AZ,EL).P;let bd=1e9,bt=0;
  for(let d=0;d<=180;d+=1.5){const t=d*Math.PI/180,q=Pq(Math.sin(t)*Math.cos(S.phi),Math.sin(t)*Math.sin(S.phi),Math.cos(t)),e=Math.hypot(x-q[0],y-q[1]);if(e<bd){bd=e;bt=t;}}S.theta=bt;}
  else{AZ-=(x-lx)*.01;EL=Math.max(-.2,Math.min(1.3,EL+(y-ly)*.01));lx=x;ly=y;S.jejak=[];}},
 up:()=>{if(dm===1){S.drag=false;const a=Math.abs(Math.sin(S.theta));if(a>.05)S.fid={amp:a,t0:performance.now()};}dm=0;}});
perbarui();
$('brot').onclick=e=>{S.rot=!S.rot;S.jejak=[];e.target.setAttribute('aria-pressed',S.rot);e.target.textContent='Kerangka berputar: '+(S.rot?'aktif':'mati');
 $('pesan').textContent=S.rot?'Kita ikut berputar pada frekuensi RF. Jika RF sama dengan Larmor, presesi tampak berhenti. Jika berbeda, vektor melayang pelan.':'Kembali ke kerangka laboratorium.';};

LOOP.push((ts,dt)=>{const el=langkah(ts,dt);gambarVektor();gambarFID(el);});
}});
