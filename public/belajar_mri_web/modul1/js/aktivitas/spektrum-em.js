// Peta spektrum EM di kartu T1.7 (HTML-nya ada di materi/t1-7-spektrum-em.js)
MODUL.init.push(function(){
// TODO-VERIFIKASI: batas pita adalah perkiraan; rujukan berbeda memberi batas berbeda. Gambar buku memakai tonggak 10^4, 10^8, 10^12, 10^14, 10^15, 10^16, 10^18, 10^20 Hz. Batas pengion dipasang di 10^16 Hz mengikuti gambar buku. Bahaya dan contoh pemakaian mengikuti gambar buku, hanya untuk belajar.
const EMB=[
 {n:'Radio',k:'Radio',a:4,b:8.5,pakai:'telekomunikasi, radar, dan MRI',bhy:'gangguan pada alat implan'},
 {n:'Gelombang mikro',k:'Mikro',a:8.5,b:12,pakai:'telekomunikasi dan memasak',bhy:'luka bakar'},
 {n:'Inframerah',k:'Inframerah',a:12,b:14.6,pakai:'pemanasan',bhy:'luka bakar'},
 {n:'Cahaya tampak',k:'Tampak',a:14.6,b:14.9,pakai:'fotografi',bhy:'kerusakan retina'},
 {n:'Ultraviolet (UV)',k:'UV',a:14.9,b:16,pakai:'lampu penyamak kulit',bhy:'luka bakar'},
 {n:'Sinar-X',k:'Sinar-X',a:16,b:18,pakai:'radiografi dan CT',bhy:'luka bakar serta efek deterministik dan stokastik'},
 {n:'Sinar gamma',k:'Gamma',a:18,b:20.01,pakai:'radioterapi dan PET',bhy:'luka bakar serta efek deterministik dan stokastik'}
];
// Gradasi warna spektrum: [log10 frekuensi, warna]. Pita cahaya tampak berisi pelangi (merah -> ungu).
const EMS=[[4,'#2f6bff'],[6.25,'#2f6bff'],[10.25,'#0b7285'],[13.3,'#e8590c'],[14.6,'#e03131'],[14.67,'#fab005'],[14.74,'#40c057'],[14.82,'#228be6'],[14.9,'#7048e8'],[15.45,'#7048e8'],[17,'#c92a2a'],[19,'#862e9c'],[20,'#862e9c']];
const hex=h=>[1,3,5].map(i=>parseInt(h.substr(i,2),16));
function warnaEM(f){
 if(f<=EMS[0][0])return EMS[0][1];
 for(let i=1;i<EMS.length;i++)if(f<=EMS[i][0]){
  const [f0,c0]=EMS[i-1],[f1,c1]=EMS[i],u=f1===f0?1:(f-f0)/(f1-f0),a=hex(c0),b=hex(c1);
  return 'rgb('+a.map((v,k)=>Math.round(v+(b[k]-v)*u)).join(',')+')';}
 return EMS[EMS.length-1][1];
}
const EMK=Math.log(25)/480,x0=20,sc=30,X=f=>x0+(f-4)*sc,SLOT=480/EMB.length,IKON_Y=96;
const WARNA_GEL=[];for(let u=0;u<=480;u++)WARNA_GEL.push(warnaEM(4+u/sc));
const kem=$('kem'),cem=kem.getContext('2d'),ST={target:null,tarik:false,jeda:false,t:0};
function logMRI(){return Math.log10(G*parseFloat($('bem').value)*1e6);}
function pitaEM(x){for(let i=0;i<EMB.length;i++)if(x>=EMB[i].a&&x<EMB[i].b)return EMB[i];return EMB[EMB.length-1];}
function infoEM(){
 const x=parseFloat($('sem').value),p=pitaEM(x),B=parseFloat($('bem').value),m=logMRI(),pm=pitaEM(m);
 $('vbem').textContent=fmt(B,1);
 $('emtxt').innerHTML='Frekuensi sekitar 10<sup>'+fmt(x,1)+'</sup> Hz. Pita: <b>'+p.n+'</b> ('+(x>=16?'pengion':'non-pengion')+'). Contoh pemakaian: '+p.pakai+'. Potensi bahaya menurut gambar buku: '+p.bhy+'.';
 $('emmri').innerHTML='Frekuensi Larmor hidrogen pada '+fmt(B,1)+' T adalah '+fmt(G*B)+' MHz, sekitar 10<sup>'+fmt(m,1)+'</sup> Hz, yaitu di pita '+pm.n+'.';
}
// ---- ikon sederhana tiap pita (digambar dengan kanvas) ----
const IKON=[
 (c,x,y,w)=>{c.lineWidth=2;c.beginPath();c.moveTo(x-8,y+14);c.lineTo(x,y-6);c.lineTo(x+8,y+14);c.moveTo(x-4,y+6);c.lineTo(x+4,y+6);c.stroke();
  c.beginPath();c.arc(x,y-9,2.5,0,6.2832);c.fill();
  for(const r of [8,13]){c.beginPath();c.arc(x,y-9,r,-0.75,0.75);c.stroke();c.beginPath();c.arc(x,y-9,r,Math.PI-0.75,Math.PI+0.75);c.stroke();}},
 (c,x,y,w)=>{c.lineWidth=2;c.strokeRect(x-15,y-10,30,20);c.strokeRect(x-11,y-6,16,12);c.beginPath();c.arc(x+10,y-3,2,0,6.2832);c.arc(x+10,y+4,2,0,6.2832);c.fill();},
 (c,x,y,w)=>{c.lineWidth=2;c.lineCap='round';for(const k of [-1,0,1]){c.beginPath();for(let t=0;t<=24;t++){const xx=x+k*9+3*Math.sin(t/3),yy=y+13-t;t?c.lineTo(xx,yy):c.moveTo(xx,yy);}c.stroke();}c.lineCap='butt';},
 (c,x,y,w)=>{const rb=['#e03131','#fab005','#40c057','#228be6','#7048e8'];c.lineWidth=3;rb.forEach((col,i)=>{c.strokeStyle=col;c.beginPath();c.arc(x,y+11,17-i*3,Math.PI,6.2832);c.stroke();});},
 (c,x,y,w)=>{c.lineWidth=2;c.beginPath();c.arc(x,y,6,0,6.2832);c.stroke();for(let k=0;k<8;k++){const a=k*Math.PI/4;c.beginPath();c.moveTo(x+9*Math.cos(a),y+9*Math.sin(a));c.lineTo(x+14*Math.cos(a),y+14*Math.sin(a));c.stroke();}},
 (c,x,y,w)=>{c.save();c.translate(x,y);c.rotate(-0.6);c.fillRect(-10,-2.5,20,5);for(const sx of [-11,11])for(const sy of [-3.8,3.8]){c.beginPath();c.arc(sx,sy,3.3,0,6.2832);c.fill();}c.restore();},
 (c,x,y,w)=>{for(let k=0;k<3;k++){const a=-Math.PI/2+k*2*Math.PI/3;c.beginPath();c.moveTo(x,y);c.arc(x,y,14,a-Math.PI/6,a+Math.PI/6);c.closePath();c.fill();}
  c.fillStyle=w;c.beginPath();c.arc(x,y,4.5,0,6.2832);c.fill();c.fillStyle=c.strokeStyle;c.beginPath();c.arc(x,y,2.5,0,6.2832);c.fill();}
];
function gambarEM(ts){
 // animasi halus menuju target (klik ikon, tombol "Ke frekuensi MRI")
 if(ST.target!==null){const cur=parseFloat($('sem').value);let n=cur+(ST.target-cur)*0.2;if(Math.abs(ST.target-n)<0.01){n=ST.target;ST.target=null;}$('sem').value=n.toFixed(2);infoEM();}
 const W=kem.width,H=kem.height,tx=warna('--teks'),lm=warna('--lembut'),gr=warna('--garis'),ak=warna('--aksen'),t1=warna('--t1'),pm=warna('--permukaan');
 const xv=parseFloat($('sem').value),aktif=EMB.indexOf(pitaEM(xv));
 cem.clearRect(0,0,W,H);cem.textAlign='center';cem.font='12px system-ui,sans-serif';
 // 1. balok non-pengion / pengion
 cem.globalAlpha=0.25;cem.fillStyle=warna('--benar');cem.fillRect(X(4),6,X(16)-X(4),22);cem.fillStyle=warna('--salah');cem.fillRect(X(16),6,X(20)-X(16),22);cem.globalAlpha=1;
 cem.fillStyle=tx;cem.fillText('Non-pengion',(X(4)+X(16))/2,21);cem.fillText('Pengion',(X(16)+X(20))/2,21);
 // 2. ikon tiap pita (di antara balok pengion/non-pengion dan strip warna)
 EMB.forEach((p,i)=>{
  const sx=x0+SLOT*(i+0.5),col=warnaEM((p.a+Math.min(p.b,20))/2),on=i===aktif;
  if(on){cem.globalAlpha=0.14;cem.fillStyle=ak;cem.fillRect(sx-SLOT/2+2,32,SLOT-4,60);cem.globalAlpha=1;cem.strokeStyle=ak;cem.lineWidth=1.5;cem.strokeRect(sx-SLOT/2+2,32,SLOT-4,60);}
  cem.strokeStyle=col;cem.fillStyle=col;IKON[i](cem,sx,56,pm);
  cem.fillStyle=tx;cem.font=(on?'bold ':'')+'12px system-ui,sans-serif';cem.textAlign='center';cem.fillText(p.k,sx,86);
  // garis penghubung ke pita pada strip
  const cb=X((p.a+Math.min(p.b,20))/2);cem.strokeStyle=lm;cem.lineWidth=1;cem.globalAlpha=0.7;cem.beginPath();cem.moveTo(sx,93);cem.lineTo(cb,108);cem.stroke();cem.globalAlpha=1;
 });
 // 3. strip warna bergradasi
 const g=cem.createLinearGradient(X(4),0,X(20),0);EMS.forEach(([f,c])=>g.addColorStop((f-4)/16,c));
 cem.fillStyle=g;cem.fillRect(X(4),108,X(20)-X(4),26);
 cem.strokeStyle='rgba(255,255,255,0.8)';cem.lineWidth=1;
 EMB.forEach(p=>{const xx=X(p.a);cem.beginPath();cem.moveTo(xx,108);cem.lineTo(xx,114);cem.moveTo(xx,128);cem.lineTo(xx,134);cem.stroke();});
 // batas pengion / non-pengion
 cem.strokeStyle=tx;cem.lineWidth=1;cem.setLineDash([3,3]);cem.beginPath();cem.moveTo(X(16),4);cem.lineTo(X(16),30);cem.moveTo(X(16),96);cem.lineTo(X(16),136);cem.stroke();cem.setLineDash([]);
 // 4. keterangan arah dan gelombang
 cem.fillStyle=lm;cem.font='12px system-ui,sans-serif';cem.textAlign='left';cem.fillText('\u25c4 panjang gelombang makin besar',x0,154);cem.textAlign='right';cem.fillText('energi makin besar \u25ba',W-x0,154);cem.textAlign='center';
 const yw=182,A=17;cem.lineWidth=2;let py=null;
 for(let u=0;u<=480;u++){
  const ph=0.06*(Math.exp(u*EMK)-1)/EMK-ts/150,y=yw-A*Math.sin(ph);
  if(py!==null){cem.strokeStyle=WARNA_GEL[u];cem.beginPath();cem.moveTo(x0+u-1,py);cem.lineTo(x0+u,y);cem.stroke();}
  py=y;
 }
 // 5. sumbu log
 const ya=236;cem.strokeStyle=gr;cem.lineWidth=1;cem.beginPath();cem.moveTo(X(4),ya);cem.lineTo(X(20),ya);cem.stroke();
 for(let n=4;n<=20;n++){const big=n%4===0;cem.beginPath();cem.moveTo(X(n),ya);cem.lineTo(X(n),ya+(big?6:3));cem.stroke();
  if(big){cem.fillStyle=tx;cem.textAlign='right';cem.font='12px system-ui,sans-serif';cem.fillText('10',X(n)+2,ya+19);cem.textAlign='left';cem.font='9px system-ui,sans-serif';cem.fillText(String(n),X(n)+3,ya+13);}}
 cem.font='12px system-ui,sans-serif';cem.textAlign='center';
 // 6. penanda frekuensi (titik bisa diseret) dan penanda MRI
 const xs=X(xv);cem.strokeStyle=ak;cem.lineWidth=2;cem.setLineDash([4,4]);cem.beginPath();cem.moveTo(xs,134);cem.lineTo(xs,ya);cem.stroke();cem.setLineDash([]);
 cem.fillStyle=ak;cem.beginPath();cem.arc(xs,ya,ST.tarik?9:7,0,6.2832);cem.fill();cem.strokeStyle=pm;cem.lineWidth=2;cem.stroke();
 const xm=X(logMRI());cem.fillStyle=t1;cem.beginPath();cem.moveTo(xm,ya-4);cem.lineTo(xm-6,ya-15);cem.lineTo(xm+6,ya-15);cem.closePath();cem.fill();cem.fillText('MRI',xm,ya-19);
}
// ---- interaksi: slider, klik, dan seret titik saling terhubung ----
function titikKanvas(e){const b=kem.getBoundingClientRect();return[(e.clientX-b.left)*kem.width/b.width,(e.clientY-b.top)*kem.height/b.height];}
function aksi(e){
 const [mx,my]=titikKanvas(e);
 if(my<IKON_Y){const i=Math.min(EMB.length-1,Math.max(0,Math.floor((mx-x0)/SLOT))),p=EMB[i];ST.target=(p.a+Math.min(p.b,20))/2;}   // klik ikon: loncat halus ke pita
 else{ST.target=null;const f=Math.min(20,Math.max(4,(mx-x0)/sc+4));$('sem').value=f.toFixed(2);infoEM();}                         // seret: ikut pointer langsung
}
kem.addEventListener('pointerdown',e=>{kem.setPointerCapture(e.pointerId);ST.tarik=true;kem.style.cursor='grabbing';aksi(e);});
kem.addEventListener('pointermove',e=>{if(ST.tarik)aksi(e);});
['pointerup','pointercancel'].forEach(n=>kem.addEventListener(n,()=>{ST.tarik=false;kem.style.cursor='grab';}));
$('sem').oninput=()=>{ST.target=null;infoEM();};
$('bem').oninput=infoEM;
$('embtn').onclick=()=>{ST.target=logMRI();};
const sJ=tombolJeda($('emjeda'),()=>ST.jeda,v=>ST.jeda=v);
tombolReset($('emreset'),()=>{$('bem').value='1.5';ST.target=logMRI();ST.jeda=false;sJ();infoEM();});
$('sem').value=logMRI().toFixed(2);infoEM();

LOOP.push((ts,dt)=>{if(!ST.jeda)ST.t+=dt*1000;gambarEM(ST.t);});
});
