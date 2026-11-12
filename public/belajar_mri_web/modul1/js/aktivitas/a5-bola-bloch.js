// A5 Bola Bloch: pulsa RF, T1 dan T2 dari persamaan Bloch (integrasi RK4 dalam kerangka berputar)
MODUL.aktivitas.push({kode:'A5',setelah:'t2-3b-komponen-nmv',html:`<div class='kartu'>
<b>A5. Bola Bloch: pulsa RF, T1 dan T2 (T2.3; pratinjau relaksasi Modul 2)</b>
<p class='lembut'>Posisi M dihitung dari persamaan Bloch (integrasi RK4) dalam kerangka berputar, bukan kurva gambar. T1 dan T2 diambil dari tabel jaringan modul dan diskalakan menurut B0. Beri pulsa 90&deg;, lalu amati Mz pulih (T1) dan Mxy meluruh (T2). Seret bola atau pakai tombol panah untuk memutar pandangan.</p>
<div class='bl'>
<div class='baris'>
<label for='bl-jr'>Jaringan</label><select id='bl-jr'></select>
<label for='bl-b0'>B0</label><select id='bl-b0'><option value='1.5'>1,5 T</option><option value='3'>3 T</option></select>
</div>
<div class='gdua'>
<canvas id='bl-sf' width='360' height='360' tabindex='0' role='img' aria-label='Bola Bloch 3D. Seret atau gunakan tombol panah untuk memutar pandangan.'></canvas>
<canvas id='bl-pl' width='560' height='360' role='img' aria-label='Grafik Mz, besar Mxy, dan sinyal Mx terhadap waktu'></canvas>
</div>
<div class='baris'>
<button type='button' id='bl-p90'>Pulsa 90&deg;</button><button type='button' id='bl-p180'>Pulsa 180&deg;</button><button id='bl-jeda'></button><button id='bl-reset'></button>
</div>
<div class='gdua2'>
<div><label for='bl-of'>Ketidakhomogenan medan: offset <span id='bl-vof'>20</span> Hz</label><input type='range' id='bl-of' min='-60' max='60' step='1' value='20'></div>
<div><label for='bl-sp'>Kecepatan: <span id='bl-vsp'>300</span> ms waktu-simulasi per detik</label><input type='range' id='bl-sp' min='50' max='1500' step='50' value='300'></div>
</div>
<p class='ang'><span style='color:var(--t1)'>Mz = <b id='bl-rz'>1,00</b></span><span style='color:var(--t2)'>|Mxy| = <b id='bl-rx'>0,00</b></span><span style='color:var(--aksen)'>t = <b id='bl-rt'>0</b> ms</span><span>T1 = <b id='bl-t1'></b> ms, T2 = <b id='bl-t2'></b> ms</span></p>
<p id='bl-ps' role='status' aria-live='polite' class='lembut'></p>
</div>
<p class='lembut'>Catatan didaktik: durasi pulsa dibuat 1 ms agar terlihat; offset frekuensi dibatasi &plusmn;60 Hz supaya putaran Mxy bisa diikuti mata; kecepatan putar tampilan diperlambat. Tidak ada derau, dan seluruh spin dianggap satu NMV (peluruhan T2 sudah mencakup dephasing).</p>
</div>`,init(){
// TODO-VERIFIKASI: durasi pulsa 1 ms, batas offset dan kecepatan tampilan adalah parameter didaktik, bukan nilai fisika.
const TAU=1,WIN=1500,R=135,CX=180,CY=190;
const S={M:[0,0,1],t:0,hist:[],trail:[],pulse:null,jeda:false,auto:0.8,T1:1,T2:1};
let AZ=-0.6,EL=0.35;
const sf=$('bl-sf'),pl=$('bl-pl'),dpr=Math.min(window.devicePixelRatio||1,2);
[sf,pl].forEach(c=>{const w=c.width,h=c.height;c.width=w*dpr;c.height=h*dpr;c.getContext('2d').scale(dpr,dpr);});
const cs=sf.getContext('2d'),cp=pl.getContext('2d');
$('bl-jr').innerHTML=DATA.JARINGAN.map((j,i)=>"<option value='"+i+"'>"+j.nama+'</option>').join('');
$('bl-jr').value=1;
function jaringan(){const w=waktuRelaksasi(DATA.JARINGAN[+$('bl-jr').value],parseFloat($('bl-b0').value));S.T1=w.T1;S.T2=w.T2;$('bl-t1').textContent=w.T1;$('bl-t2').textContent=w.T2;}
// Persamaan Bloch dalam kerangka berputar: B1 sepanjang x, offset dw (rad/ms), relaksasi T1 dan T2.
function rk4(m,h,w1,dw){
 const f=a=>[dw*a[1]-a[0]/S.T2,-dw*a[0]+w1*a[2]-a[1]/S.T2,-w1*a[1]-(a[2]-1)/S.T1];
 const ad=(a,b,c)=>a.map((v,i)=>v+b[i]*c);
 const k1=f(m),k2=f(ad(m,k1,h/2)),k3=f(ad(m,k2,h/2)),k4=f(ad(m,k3,h));
 return m.map((v,i)=>v+h/6*(k1[i]+2*k2[i]+2*k3[i]+k4[i]));
}
function maju(d,w1){const dw=2*Math.PI*parseFloat($('bl-of').value)/1000,n=Math.max(1,Math.ceil(d/0.5)),h=d/n;for(let i=0;i<n;i++)S.M=rk4(S.M,h,w1,dw);S.t+=d;}
function aksi(a){if(S.pulse)return;S.auto=null;S.pulse={rem:TAU,w1:a*Math.PI/180/TAU};$('bl-ps').textContent='Pulsa '+a+'\u00b0 (B1 sepanjang sumbu x) sedang diberikan.';}
$('bl-p90').onclick=()=>aksi(90);$('bl-p180').onclick=()=>aksi(180);
const sJ=tombolJeda($('bl-jeda'),()=>S.jeda,v=>S.jeda=v);
tombolReset($('bl-reset'),()=>{S.M=[0,0,1];S.t=0;S.hist=[];S.trail=[];S.pulse=null;S.jeda=false;S.auto=0.8;AZ=-0.6;EL=0.35;sJ();$('bl-ps').textContent='Kembali ke kesetimbangan: M sejajar B0.';});
$('bl-jr').onchange=$('bl-b0').onchange=jaringan;
$('bl-of').oninput=()=>$('bl-vof').textContent=$('bl-of').value;
$('bl-sp').oninput=()=>$('bl-vsp').textContent=$('bl-sp').value;
// Putar pandangan: seret (pembantu seret dari core.js) atau tombol panah.
let lx=0,ly=0;
seret(sf,{down:(x,y)=>{lx=x;ly=y;},move:(x,y)=>{AZ-=(x-lx)*0.01/dpr;EL=Math.max(-1.4,Math.min(1.4,EL+(y-ly)*0.01/dpr));lx=x;ly=y;}});
sf.onkeydown=e=>{const k={ArrowLeft:[.1,0],ArrowRight:[-.1,0],ArrowUp:[0,.1],ArrowDown:[0,-.1]}[e.key];if(k){e.preventDefault();AZ+=k[0];EL=Math.max(-1.4,Math.min(1.4,EL+k[1]));}};
function panah(pr,a,b,col,w,lab){const p=pr.P(...a),q=pr.P(...b);cs.strokeStyle=cs.fillStyle=col;cs.lineWidth=w;cs.beginPath();cs.moveTo(p[0],p[1]);cs.lineTo(q[0],q[1]);cs.stroke();cs.beginPath();cs.arc(q[0],q[1],w+1.5,0,6.2832);cs.fill();if(lab){cs.font='13px system-ui,sans-serif';cs.fillText(lab,q[0]+7,q[1]-6);}}
function gambarBolaBloch(C){
 const pr=proyeksiBola(CX,CY,R,AZ,EL),P=pr.P,M=S.M;
 cs.clearRect(0,0,360,360);
 gambarBola(cs,CX,CY,R,pr);
 panah(pr,[0,0,0],[0,0,1.2],C.b,3,'B0 / z');panah(pr,[0,0,0],[1.15,0,0],C.l,1.5,'x');panah(pr,[0,0,0],[0,1.15,0],C.l,1.5,'y');
 if(S.trail.length>1){cs.strokeStyle=C.t1;cs.lineWidth=2;for(let k=1;k<S.trail.length;k++){cs.globalAlpha=k/S.trail.length*.5;const a=P(...S.trail[k-1]),b=P(...S.trail[k]);cs.beginPath();cs.moveTo(a[0],a[1]);cs.lineTo(b[0],b[1]);cs.stroke();}cs.globalAlpha=1;}
 const o=P(0,0,0),q=P(M[0],M[1],0),m=P(...M);
 cs.strokeStyle=C.t2;cs.lineWidth=2;cs.setLineDash([4,4]);cs.beginPath();cs.moveTo(o[0],o[1]);cs.lineTo(q[0],q[1]);cs.lineTo(m[0],m[1]);cs.stroke();cs.setLineDash([]);
 panah(pr,[0,0,0],M,C.t1,4,'M');
}
function gambarGrafik(C){
 const W=560,H=360,L=36,Rr=W-10,T=14,B=H-26,y=v=>(T+B)/2-v*(B-T)/2.1,x=tt=>L+(tt-(S.t-WIN))/WIN*(Rr-L);
 cp.clearRect(0,0,W,H);cp.font='12px system-ui,sans-serif';cp.textAlign='left';cp.strokeStyle=C.g;cp.fillStyle=C.l;cp.lineWidth=1;
 [-1,0,1].forEach(v=>{cp.beginPath();cp.moveTo(L,y(v));cp.lineTo(Rr,y(v));cp.stroke();cp.fillText(v,6,y(v)+4);});
 cp.fillText('waktu-simulasi (jendela '+WIN+' ms)',L,H-8);
 const ln=(k,col,w)=>{cp.strokeStyle=col;cp.lineWidth=w;cp.beginPath();S.hist.forEach((h,i)=>{const px=x(h[0]),py=y(h[k]);i?cp.lineTo(px,py):cp.moveTo(px,py);});cp.stroke();};
 if(S.hist.length>1){ln(3,C.a,1);ln(2,C.t2,2.5);ln(1,C.t1,2.5);}
 cp.fillStyle=C.bg;cp.fillRect(Rr-128,T-2,128,32);cp.fillStyle=C.t1;cp.fillText('Mz',Rr-120,T+10);cp.fillStyle=C.t2;cp.fillText('|Mxy|',Rr-92,T+10);cp.fillStyle=C.a;cp.fillText('Mx (sinyal)',Rr-60,T+24);
}
jaringan();
$('bl-ps').textContent='Pulsa 90\u00b0 akan diberikan otomatis. Setelah itu coba ubah jaringan, B0, atau offset.';
LOOP.push((ts,dt)=>{
 if(!S.jeda){
  if(S.auto!==null){S.auto-=dt;if(S.auto<=0){S.auto=null;aksi(90);}}
  if(S.pulse){const d=Math.min(S.pulse.rem,TAU*dt/0.6);maju(d,S.pulse.w1);S.pulse.rem-=d;
   if(S.pulse.rem<=1e-9){S.pulse=null;const mxy=Math.hypot(S.M[0],S.M[1]);
    $('bl-ps').textContent=mxy>0.05?'Ada komponen transversal ('+fmt(mxy)+'). Mx berosilasi: itulah FID yang ditangkap koil, dan meluruh dengan T2.':'M sejajar sumbu z: tidak ada komponen transversal, jadi tidak ada sinyal.';}}
  else maju(dt*parseFloat($('bl-sp').value),0);
  S.hist.push([S.t,S.M[2],Math.hypot(S.M[0],S.M[1]),S.M[0]]);while(S.hist.length&&S.t-S.hist[0][0]>WIN)S.hist.shift();
  S.trail.push(S.M.slice());if(S.trail.length>80)S.trail.shift();
 }
 const C={bg:warna('--permukaan'),g:warna('--garis'),l:warna('--lembut'),b:warna('--b0'),t1:warna('--t1'),t2:warna('--t2'),a:warna('--aksen')};
 gambarBolaBloch(C);gambarGrafik(C);
 $('bl-rz').textContent=fmt(S.M[2]);$('bl-rx').textContent=fmt(Math.hypot(S.M[0],S.M[1]));$('bl-rt').textContent=Math.round(S.t);
});
}});
