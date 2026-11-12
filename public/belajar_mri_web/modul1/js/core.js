// Inti: registri modul, loop animasi bersama, pembantu gambar, pilihan materi per kartu.
const MODUL={materi:[],aktivitas:[],init:[],soal:{1:[],2:[],3:[]}};
const LOOP=[];   // tiap aktivitas mendaftarkan fungsi (ts, dt) di sini
const $=id=>document.getElementById(id);
const warna=n=>getComputedStyle(document.documentElement).getPropertyValue(n).trim();
let terakhir=0;
function tick(ts){
 const dt=Math.min(0.05,(ts-terakhir)/1000||0);terakhir=ts;
 for(let i=LOOP.length-1;i>=0;i--){try{LOOP[i](ts,dt);}catch(e){console.error('Loop dihentikan karena galat:',e);LOOP.splice(i,1);}}
 requestAnimationFrame(tick);
}
// ---- Pembantu gambar: bola 3D, garis tengah, panah ----
function bola3d(c,x,y,r,col){
 c.fillStyle=col;c.beginPath();c.arc(x,y,r,0,6.2832);c.fill();
 const g=c.createRadialGradient(x-r*.35,y-r*.4,r*.05,x,y,r);
 g.addColorStop(0,'rgba(255,255,255,.65)');g.addColorStop(.5,'rgba(255,255,255,0)');g.addColorStop(1,'rgba(0,0,0,.35)');
 c.fillStyle=g;c.beginPath();c.arc(x,y,r,0,6.2832);c.fill();
}
// bola dengan garis tengah; (dx,dy) = setengah panjang garis; kepala=true menambah mata panah di ujung
function garisBola(c,x,y,r,dx,dy,col,kepala){
 const tx=warna('--teks'),n=Math.hypot(dx,dy)||1,ux=dx/n,uy=dy/n;
 bola3d(c,x,y,r,col);
 c.lineCap='round';c.strokeStyle=c.fillStyle=tx;c.lineWidth=2.5;
 c.beginPath();c.moveTo(x-dx,y-dy);c.lineTo(x+dx,y+dy);c.stroke();
 if(kepala){const ex=x+dx,ey=y+dy;c.beginPath();c.moveTo(ex+ux*9,ey+uy*9);c.lineTo(ex-uy*6,ey+ux*6);c.lineTo(ex+uy*6,ey-ux*6);c.closePath();c.fill();}
}

function bangunModeKartu(){
document.querySelectorAll('.bm').forEach(k=>{
 k.dataset.mode='singkat';
 const g=document.createElement('div');g.className='pilih';g.setAttribute('role','group');g.setAttribute('aria-label','Kedalaman materi kartu ini');
 [['singkat','Materi singkat'],['lengkap','Materi lengkap'],['analogi','Analogi']].forEach(([m,t])=>{
  const b=document.createElement('button');b.type='button';b.textContent=t;b.setAttribute('aria-pressed',m==='singkat');
  b.onclick=()=>{k.dataset.mode=m;g.querySelectorAll('button').forEach(x=>x.setAttribute('aria-pressed',x===b));};
  g.appendChild(b);});
 k.querySelector('.tag').after(g);});
}

// ---- Bola 3D (proyeksi ortografik), dipakai A3 sudut flip dan A4 ----
// x ke kanan, y menjauhi pengamat, z ke atas. AZ = putaran kamera sekitar z, EL = kemiringan ke atas (rad).
function proyeksiBola(cx,cy,R,AZ,EL){
 const cA=Math.cos(AZ),sA=Math.sin(AZ),cE=Math.cos(EL),sE=Math.sin(EL);
 return{
  P:(x,y,z)=>{const yp=x*sA+y*cA;return [cx+R*(x*cA-y*sA),cy-R*(z*cE+yp*sE)];},
  dep:(x,y,z)=>(x*sA+y*cA)*cE-z*sE   // >0 = sisi belakang bola
 };
}
function gambarBola(c,cx,cy,R,pr){
 const {P,dep}=pr,lm=warna('--lembut');
 const g=c.createRadialGradient(cx-R*0.35,cy-R*0.4,R*0.1,cx,cy,R);
 g.addColorStop(0,'rgba(255,255,255,0.35)');g.addColorStop(1,'rgba(120,140,200,0.12)');
 c.fillStyle=g;c.beginPath();c.arc(cx,cy,R,0,6.2832);c.fill();
 c.strokeStyle=lm;c.lineWidth=1.2;c.beginPath();c.arc(cx,cy,R,0,6.2832);c.stroke();
 const lingkaran=f=>{for(const depan of [false,true]){
  c.setLineDash(depan?[]:[3,4]);c.globalAlpha=depan?0.9:0.45;c.beginPath();
  for(let a=0;a<96;a++){const q0=f(a/96*6.2832),q1=f((a+1)/96*6.2832),m=q0.map((v,i)=>(v+q1[i])/2);
   if((dep(...m)<=0)===depan){const [x0,y0]=P(...q0),[x1,y1]=P(...q1);c.moveTo(x0,y0);c.lineTo(x1,y1);}}
  c.stroke();}c.setLineDash([]);c.globalAlpha=1;};
 c.lineWidth=1;c.strokeStyle=lm;
 lingkaran(t=>[Math.cos(t),Math.sin(t),0]);   // khatulistiwa = bidang transversal (x-y)
 lingkaran(t=>[Math.cos(t),0,Math.sin(t)]);   // meridian bidang x-z
 lingkaran(t=>[0,Math.cos(t),Math.sin(t)]);   // meridian bidang y-z
}

// ---- Ikon tombol, tombol jeda/reset, dan pembantu seret (pointer) ----
const SVGI={
 play:"<svg viewBox='0 0 24 24' width='20' height='20' aria-hidden='true'><path d='M7 4.5v15l13-7.5z' fill='currentColor'/></svg>",
 jeda:"<svg viewBox='0 0 24 24' width='20' height='20' aria-hidden='true'><rect x='6' y='4.5' width='4.5' height='15' rx='1' fill='currentColor'/><rect x='13.5' y='4.5' width='4.5' height='15' rx='1' fill='currentColor'/></svg>",
 reset:"<svg viewBox='0 0 24 24' width='20' height='20' aria-hidden='true'><path d='M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z' fill='currentColor'/></svg>"};
// Toggle jeda/lanjut: ikon jeda saat berjalan, ikon putar saat dijeda. Mengembalikan fungsi penyegar tampilan.
function tombolJeda(el,get,set){el.type='button';el.classList.add('ik');el.setAttribute('aria-label','Jeda animasi');
 const seg=()=>{const j=get();el.innerHTML=j?SVGI.play:SVGI.jeda;el.setAttribute('aria-pressed',j);el.title=j?'Lanjutkan animasi':'Jeda animasi';};
 el.onclick=()=>{set(!get());seg();};seg();return seg;}
function tombolReset(el,fn){el.type='button';el.classList.add('ik');el.innerHTML=SVGI.reset;el.title='Reset';el.setAttribute('aria-label','Reset');el.onclick=fn;}
// Seret dengan pointer (mouse dan sentuh). h.down(x,y) boleh mengembalikan false bila tidak mengenai apa pun.
function seret(cv,h){let on=false;cv.classList.add('seret');
 const pt=e=>{const b=cv.getBoundingClientRect();return[(e.clientX-b.left)*cv.width/b.width,(e.clientY-b.top)*cv.height/b.height];};
 cv.addEventListener('pointerdown',e=>{const[x,y]=pt(e);if(h.down(x,y,e)===false)return;on=true;cv.setPointerCapture(e.pointerId);cv.style.cursor='grabbing';e.preventDefault();});
 cv.addEventListener('pointermove',e=>{if(on){const[x,y]=pt(e);h.move(x,y,e);}});
 ['pointerup','pointercancel'].forEach(n=>cv.addEventListener(n,e=>{if(!on)return;on=false;cv.style.cursor='';h.up&&h.up(e);}));}
