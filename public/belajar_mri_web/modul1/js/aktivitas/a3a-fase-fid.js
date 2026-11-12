// A3 Eksitasi dan FID: fase dan sinyal koil
MODUL.aktivitas.push({kode:'A3',setelah:'t2-3a-sinyal-fid',html:`<div class='kartu'>
<b>A3. Eksitasi dan FID: fase dan sinyal di koil (T2.1, T2.3)</b>
<p class='lembut'>Tampak atas bidang transversal. Delapan spin berpresesi (seret sebuah bola untuk mengubah fase dan panjangnya). Beri pulsa RF 90&deg; agar mereka sefase, lalu lihat sinyal di koil meluruh karena dephasing. Garis putus-putus menghubungkan ujung resultan dengan sinyal: tinggi ujung resultan pada sumbu y adalah nilai sinyal yang dicatat koil saat itu.</p>
<canvas id='kfs' width='520' height='240' role='img' aria-label='Delapan panah spin berputar pada bidang transversal dan grafik sinyal koil'></canvas>
<label for='sinh'>Ketidakhomogenan B0: <span id='vinh'></span></label>
<input type='range' id='sinh' min='0' max='1' step='0.05' value='0.5'>
<p id='fstat'></p>
<div class='baris'>
<button type='button' id='fpulsa'>Pulsa RF 90&deg;</button>
<button id='fjeda'></button><button id='freset'></button>
</div>
<p id='fpesan' role='status' aria-live='polite' class='lembut'></p>
</div>`,init(){
// ---- Aktivitas 5: fase dan koherensi ----
// TODO-VERIFIKASI: laju putar dan sebaran frekuensi spin hanyalah parameter didaktik.
const kfs=$('kfs'),cfs=kfs.getContext('2d'),NS=8,DW=[-1,0.6,-0.3,0.9,-0.7,0.2,1,-0.5];
const FS={ph:[],len:[],mode:'awal',tp:0,ref:0,hist:[],last:0,jeda:false,dr:-1};
function resetFase(){FS.ph=DW.map((_,i)=>i*2*Math.PI/NS);FS.len=DW.map(()=>0.35);FS.mode='awal';FS.hist=[];}
resetFase();
function gambarFase(ts){
 const dt=FS.jeda?0:Math.min(0.05,(ts-(FS.last||ts))/1000);FS.last=ts;
 const inh=parseFloat($('sinh').value);$('vinh').textContent=fmt(inh,2);
 for(let i=0;i<NS;i++){
  if(FS.mode==='pulsa'){
   let d=FS.ref-FS.ph[i];d=Math.atan2(Math.sin(d),Math.cos(d));FS.ph[i]+=d*Math.min(1,dt*6);
   FS.len[i]+=(1-FS.len[i])*Math.min(1,dt*4);
  }
  if(i!==FS.dr)FS.ph[i]+=(3+inh*2.5*DW[i])*dt;
 }
 if(FS.mode==='pulsa'){FS.ref+=3*dt;FS.tp-=dt;if(FS.tp<=0)FS.mode='bebas';}
 let sx=0,sy=0;
 for(let i=0;i<NS;i++){sx+=FS.len[i]*Math.cos(FS.ph[i]);sy+=FS.len[i]*Math.sin(FS.ph[i]);}
 sx/=NS;sy/=NS;
 if(!FS.jeda||!FS.hist.length){FS.hist.push(sy);if(FS.hist.length>250)FS.hist.shift();}
 const co=Math.hypot(sx,sy);$('fstat').textContent='Koherensi (besar magnetisasi transversal): '+Math.round(co*100)+'%';
 const W=kfs.width,H=kfs.height,cx=115,cy=110,R=90,tx=warna('--teks'),lm=warna('--lembut'),t1=warna('--t1'),t2=warna('--t2'),gr=warna('--garis');
 cfs.clearRect(0,0,W,H);cfs.font='14px system-ui,sans-serif';
 cfs.strokeStyle=gr;cfs.lineWidth=1;cfs.beginPath();cfs.arc(cx,cy,R,0,6.2832);cfs.stroke();
 cfs.beginPath();cfs.moveTo(cx-R,cy);cfs.lineTo(cx+R,cy);cfs.moveTo(cx,cy-R);cfs.lineTo(cx,cy+R);cfs.stroke();
 for(let i=0;i<NS;i++){const x=cx+R*FS.len[i]*Math.cos(FS.ph[i]),y=cy-R*FS.len[i]*Math.sin(FS.ph[i]);
  cfs.strokeStyle=t1;cfs.lineWidth=2;cfs.beginPath();cfs.moveTo(cx,cy);cfs.lineTo(x,y);cfs.stroke();bola3d(cfs,x,y,6,t1);}
 cfs.strokeStyle=t2;cfs.lineWidth=5;cfs.beginPath();cfs.moveTo(cx,cy);cfs.lineTo(cx+R*sx,cy-R*sy);cfs.stroke();
 cfs.fillStyle=tx;cfs.textAlign='center';cfs.fillText('Resultan (biru-hijau)',cx,H-8);
 const x0=250,w=260,ym=110;
 cfs.strokeStyle=gr;cfs.lineWidth=1;cfs.strokeRect(x0,20,w,180);cfs.beginPath();cfs.moveTo(x0,ym);cfs.lineTo(x0+w,ym);cfs.stroke();
 cfs.strokeStyle=t2;cfs.lineWidth=2;cfs.beginPath();
 FS.hist.forEach((v,k)=>{const x=x0+w-(FS.hist.length-1-k)*w/250,y=ym-v*R;k?cfs.lineTo(x,y):cfs.moveTo(x,y);});cfs.stroke();
 cfs.fillStyle=lm;cfs.textAlign='left';cfs.fillText('Sinyal di koil (waktu bergerak ke kiri)',x0,14);
 // garis putus-putus: ujung resultan -> titik sinyal terbaru.
 // Keduanya memakai satu nilai (v = komponen y resultan pada frame ini), jadi selalu seirama.
 const v=FS.hist[FS.hist.length-1],tipX=cx+R*sx,tipY=cy-R*v,gx=x0+w,gy=ym-v*R;
 cfs.setLineDash([5,4]);cfs.strokeStyle=tx;cfs.lineWidth=1.5;
 cfs.beginPath();cfs.moveTo(tipX,tipY);cfs.lineTo(gx,gy);cfs.stroke();
 cfs.setLineDash([]);
 bola3d(cfs,tipX,tipY,4,tx);bola3d(cfs,gx,gy,5,t2);
 cfs.fillStyle=tx;cfs.textAlign='left';cfs.fillText('y',cx-14,cy-R+14);
 // keterangan diam di bawah kotak sinyal
 cfs.setLineDash([5,4]);cfs.strokeStyle=tx;cfs.lineWidth=1.5;cfs.beginPath();cfs.moveTo(x0+28,H-13);cfs.lineTo(x0+58,H-13);cfs.stroke();cfs.setLineDash([]);
 cfs.fillStyle=lm;cfs.textAlign='left';cfs.fillText('nilai yang dicatat koil',x0+66,H-8);
}
const sJ=tombolJeda($('fjeda'),()=>FS.jeda,v=>FS.jeda=v);
seret(kfs,{down:(x,y)=>{FS.dr=-1;let bd=16;for(let i=0;i<NS;i++){const d=Math.hypot(x-(115+90*FS.len[i]*Math.cos(FS.ph[i])),y-(110-90*FS.len[i]*Math.sin(FS.ph[i])));if(d<bd){bd=d;FS.dr=i;}}if(FS.dr<0)return false;},
 move:(x,y)=>{const dx=x-115,dy=110-y;FS.ph[FS.dr]=Math.atan2(dy,dx);FS.len[FS.dr]=Math.min(1,Math.max(.1,Math.hypot(dx,dy)/90));},up:()=>{FS.dr=-1;}});
$('fpulsa').onclick=()=>{FS.mode='pulsa';FS.tp=0.7;FS.ref=FS.ph[0];$('fpesan').textContent='Pulsa RF 90\u00b0: spin menjadi sefase dan magnetisasi transversal membesar. Sinyal muncul di koil. Naikkan ketidakhomogenan B0 agar sinyal lebih cepat meluruh.';};
tombolReset($('freset'),()=>{resetFase();FS.jeda=false;sJ();$('sinh').value='0.5';$('fpesan').textContent='Kembali ke keadaan awal: spin tidak sefase, sinyal hampir nol.';});

$('fpulsa').onclick(); // default: langsung diberi pulsa agar sinyal tampak
LOOP.push(ts=>gambarFase(ts));
}});
