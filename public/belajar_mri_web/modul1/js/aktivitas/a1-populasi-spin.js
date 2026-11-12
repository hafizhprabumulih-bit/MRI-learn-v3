// A1 Populasi spin dan B0
MODUL.aktivitas.push({kode:'A1',setelah:'t1-5-populasi-nmv',html:`<div class='kartu'>
<b>A1. Populasi spin dan B0 (T1.5, T3.1)</b>
<p class='lembut'>Panah putus-putus biru besar di latar tengah adalah arah B0 (ke atas). Seret bola untuk memindahkannya. Tiap bola adalah satu proton dan panah putus-putus di dalamnya menunjuk arah sumbu spinnya. Bola biru: panah searah B0 (ke atas). Bola abu-abu: panah berlawanan B0 (ke bawah). Klik sebuah bola untuk mengusiknya. Selisihnya digambar sangat dilebih-lebihkan agar terlihat.</p>
<canvas id='kp' class='klik' width='520' height='270' role='img' aria-label='Banyak proton dengan panah putus-putus yang menunjuk arah spin. Panah B0 menunjuk ke atas. Makin besar B0, makin banyak panah yang searah B0'></canvas>
<label for='sp'>B0: <span id='vsp'></span> T</label>
<input type='range' id='sp' min='0.5' max='3' step='0.1' value='1.5'>
<p id='sptxt'></p>
<label><input type='checkbox' id='bzmode'> Mode Boltzmann (T3.1)</label>
<p id='bztxt' class='lembut' role='status' aria-live='polite' hidden></p>
<div class='meter' aria-hidden='true'><i id='mt'></i></div>
<p class='lembut'>Batang biru: besar selisih bersih (sumber sinyal).</p>
<div class='baris'><button id='ajp'></button><button id='preset'></button></div>
</div>`,init(){
// ---- Aktivitas 3: populasi proton ----
// TODO-VERIFIKASI: proporsi spin searah B0 di sini sangat dilebih-lebihkan agar terlihat. Nilai aslinya hanya orde per sejuta.
const NP=48,kp=$('kp'),cp=kp.getContext('2d');
// Geometri kanvas: lajur kiri (GX piksel) untuk panah B0, sisanya untuk kisi proton 12 kolom
const GW=kp.width,GH=kp.height,GX=0,KOL=12,SX=(GW-GX)/KOL,SY=GH/(NP/KOL);
// Panah dengan batang putus-putus dan kepala terisi di ujung (x1,y1)
function panahPutus(c,x0,y0,x1,y1,col,w,kep){
 c.strokeStyle=c.fillStyle=col;c.lineWidth=w;c.lineCap='butt';c.setLineDash([w*2.2,w*1.8]);
 c.beginPath();c.moveTo(x0,y0);c.lineTo(x1,y1);c.stroke();c.setLineDash([]);
 const an=Math.atan2(y1-y0,x1-x0),cs=Math.cos(an),sn=Math.sin(an);
 c.beginPath();c.moveTo(x1+cs*kep,y1+sn*kep);c.lineTo(x1-sn*kep*.6,y1+cs*kep*.6);c.lineTo(x1+sn*kep*.6,y1-cs*kep*.6);c.closePath();c.fill();
}
function infoPop(){
 const B=parseFloat($('sp').value),atas=Math.round(NP*(0.5+0.4*B/3));
 $('vsp').textContent=fmt(B,1);
 $('mt').style.width=Math.round((2*atas-NP)/NP*100)+'%';
 $('sptxt').textContent='Searah B0: '+atas+' dari '+NP+'. Selisih bersih (penghasil sinyal): '+(atas-(NP-atas))+'.';
}
const PJ={j:false,t:0};
const PD=Array.from({length:NP},(_,n)=>({d:n<30?1:-1,ping:-9999,ox:0,oy:0}));
function gambarPop(ts){
 const B=parseFloat($('sp').value),atas=Math.round(NP*(0.5+0.4*B/3));
 const L=SY*0.4,r=SY*0.17;
 const bl=warna('--b0'),lm=warna('--lembut'),tx=warna('--teks');
 cp.clearRect(0,0,GW,GH);
 // B0: panah putus-putus besar di lajur kiri, menunjuk ke atas
 cp.globalAlpha=.3;panahPutus(cp,GW/2,GH-20,GW/2,26,bl,7,20);cp.globalAlpha=1;
 cp.fillStyle=bl;cp.font='bold 16px system-ui,sans-serif';cp.textAlign='center';cp.fillText('B0',GW/2+26,22);
 for(let n=0;n<NP;n++){
  const p=PD[n];p.d+=((n<atas?1:-1)-p.d)*0.1;
  const x=GX+SX*(n%KOL+0.5)+p.ox,y=SY*(Math.floor(n/KOL)+0.5)+p.oy,pg=ts-p.ping,ak=pg<900?1-pg/900:0;
  const a=(0.4+0.9*ak)*Math.sin(ts/250+n*1.7),dx=L*Math.sin(a),dy=p.d*L*Math.cos(a);
  if(ak>0){cp.strokeStyle=p.d>0?bl:lm;cp.globalAlpha=ak;cp.lineWidth=2;cp.beginPath();cp.arc(x,y,r+(1-ak)*22,0,6.2832);cp.stroke();cp.globalAlpha=1;}
  bola3d(cp,x,y,r,p.d>0?bl:lm);
  // sumbu spin: batang putus-putus dari ekor ke kepala panah; d>0 menunjuk ke atas (searah B0)
  panahPutus(cp,x-dx,y+dy,x+dx,y-dy,tx,2.2,8);
 }
}
let dr=-1,gx=0,gy=0;
const sel=n=>[GX+SX*(n%KOL+.5)+PD[n].ox,SY*(Math.floor(n/KOL)+.5)+PD[n].oy];
seret(kp,{down:(x,y)=>{let bd=SY*0.7;dr=-1;PD.forEach((p,n)=>{const[px,py]=sel(n),d=Math.hypot(x-px,y-py);if(d<bd){bd=d;dr=n;}});
 if(dr<0)return false;PD[dr].ping=PJ.t;const[px,py]=sel(dr);gx=x-px;gy=y-py;},
 move:(x,y)=>{const p=PD[dr],cx=GX+SX*(dr%KOL+.5),cy=SY*(Math.floor(dr/KOL)+.5);p.ox=Math.max(10,Math.min(GW-10,x-gx))-cx;p.oy=Math.max(10,Math.min(GH-10,y-gy))-cy;},up:()=>{dr=-1;}});
$('sp').oninput=infoPop;
const sJ=tombolJeda($('ajp'),()=>PJ.j,v=>PJ.j=v);
tombolReset($('preset'),()=>{$('sp').value='1.5';PD.forEach(p=>{p.ox=p.oy=0;});PJ.j=false;sJ();infoPop();perbaruiBz();});
infoPop();
function boltz(B){const dE=6.62607015e-34*G*1e6*B,kT=1.380649e-23*310,r=Math.exp(-dE/kT);return{r,ex:(1-r)/(1+r)*1e6};}
$('b15').textContent=fmt(boltz(1.5).ex,1);$('b3').textContent=fmt(boltz(3).ex,1);
function perbaruiBz(){const t=$('bztxt'),on=$('bzmode').checked;t.hidden=!on;if(!on)return;const B=parseFloat($('sp').value),b=boltz(B);t.textContent='\u0394E = h\u03b3B0. N+/N\u2212 = '+b.r.toFixed(8).replace('.',',')+'. Kelebihan spin berenergi rendah kira-kira '+fmt(b.ex,1)+' per sejuta (T = 310 K).';}
$('bzmode').onchange=perbaruiBz;$('sp').addEventListener('input',perbaruiBz);

LOOP.push((ts,dt)=>{if(!PJ.j)PJ.t+=dt*1000;gambarPop(PJ.t);});
}});
