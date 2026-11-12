// T1.3 Struktur atom hidrogen: model Bohr bergaya gambar acuan (nukleus, orbit, elektron, legenda)
MODUL.aktivitas.push({kode:'Atom-H',setelah:'t1-3-struktur-atom',html:`<div class='kartu'>
<b>Struktur atom hidrogen (T1.3)</b>
<p class='lembut'>Atom hidrogen adalah atom paling sederhana: satu proton di nukleus dan satu elektron yang mengorbit. Pilih isotop untuk menambah neutron, lepaskan elektronnya untuk melihat ion, atau ganti tampilan menjadi awan elektron. Seret elektron (sepanjang orbit), proton, atau neutron; klik untuk melihat sifatnya. Di pojok kiri atas, nomor massa (A) ditulis di atas dan nomor atom (Z) di bawah simbol unsur. Gambar tidak berskala: nukleus sebenarnya jauh lebih kecil daripada orbit.</p>
<canvas id='hat-cv' class='klik' width='520' height='300' role='img' aria-label='Model atom hidrogen'></canvas>
<div class='pilih' id='hat-iso' role='group' aria-label='Isotop hidrogen'>
<button type='button' data-n='0' aria-pressed='true'>Hidrogen-1</button>
<button type='button' data-n='1' aria-pressed='false'>Hidrogen-2 (deuterium)</button>
<button type='button' data-n='2' aria-pressed='false'>Hidrogen-3 (tritium)</button>
</div>
<div class='baris'>
<button type='button' id='hat-ion'>Lepaskan elektron (ionisasi)</button>
<button type='button' id='hat-mod'>Tampilan: orbit</button>
<button id='hat-jeda'></button>
<button id='hat-rs'></button>
</div>
<div class='gulir'><table class='tabel' id='hat-tab'></table></div>
<p><span id='hat-st' class='lencana'></span></p>
<p id='hat-spin' class='lembut'></p>
<p id='hat-info' role='status' aria-live='polite'></p>
</div>`,init(){
// TODO-VERIFIKASI: massa partikel (proton dan neutron sekitar 1 sma, elektron sekitar 1/1836 proton) adalah nilai umum buku teks. Kecepatan orbit, ukuran awan (A0), dan ukuran partikel hanyalah parameter gambar, bukan skala fisika.
const kc=$('hat-cv'),c=kc.getContext('2d'),W=kc.width,H=kc.height,CX=190,CY=150,R1=72,R2=125,A0=46,RP=16;
const WE='#2f9e44',WP='#339af0',WN='#e03131';   // warna mengikuti gambar acuan: elektron hijau, proton biru, neutron merah
const POS={1:[[0,0]],2:[[-11,4],[11,-4]],3:[[-12,9],[12,9],[0,-12]]};   // posisi nukleon, indeks 0 = proton
const ISO=[
 'Hidrogen-1 (protium): satu proton, tanpa neutron, ditambah satu elektron. Inilah hidrogen yang dipakai MRI klinis.',
 'Hidrogen-2 (deuterium): proton yang sama ditambah satu neutron. Jumlah proton tetap 1, jadi tetap hidrogen, tetapi nomor massanya menjadi 2. Atom berproton sama dan berneutron beda disebut isotop.',
 'Hidrogen-3 (tritium): satu proton dan dua neutron, nomor massa 3. Masih hidrogen karena jumlah protonnya tetap 1.'];
const SIFAT={
 p:'Proton: muatan +1, massa sekitar 1 sma (satuan massa atom). Jumlah proton adalah nomor atom, dan inilah yang menentukan jenis unsur.',
 n:'Neutron: tanpa muatan, massa sekitar 1 sma. Proton ditambah neutron adalah nomor massa.',
 e:'Elektron: muatan −1, massa hanya sekitar 1/1836 massa proton. Pada atom netral jumlahnya sama dengan jumlah proton.'};
const S={off:[[0,0],[0,0],[0,0]],n:0,ion:false,awan:false,jeda:false,ang:0,lepas:0,t:0,titik:[],hit:[]};
// titik awan: jarak radial mengikuti peluang 1s (r kuadrat kali e pangkat -2r/a0), arah acak lalu diproyeksikan ke bidang gambar
const sampel=()=>{const r=Math.min(A0*3.2,A0*0.5*-Math.log(Math.random()*Math.random()*Math.random())),z=2*Math.random()-1,p=Math.random()*6.2832,s=Math.sqrt(1-z*z);return[r*s*Math.cos(p),r*s*Math.sin(p)];};
for(let i=0;i<140;i++)S.titik.push(sampel());

function partikel(x,y,r,col,simbol){
 bola3d(c,x,y,r,col);
 if(simbol){c.fillStyle='#fff';c.font='bold '+Math.round(r*1.3)+'px system-ui,sans-serif';c.textAlign='center';c.textBaseline='middle';c.fillText(simbol,x,y+1);c.textBaseline='alphabetic';}
}
function gambar(){
 const tx=warna('--teks'),lm=warna('--lembut'),e=S.lepas,tot=1+S.n;
 c.clearRect(0,0,W,H);S.hit=[];c.textBaseline='alphabetic';
 // orbit atau awan elektron
 if(!S.awan){
  c.lineWidth=1.5;c.strokeStyle=lm;c.globalAlpha=.9;c.beginPath();c.arc(CX,CY,R1,0,6.2832);c.stroke();
  c.setLineDash([5,5]);c.globalAlpha=.4;c.beginPath();c.arc(CX,CY,R2,0,6.2832);c.stroke();c.setLineDash([]);c.globalAlpha=1;
  c.fillStyle=lm;c.font='12px system-ui,sans-serif';c.textAlign='center';c.fillText('orbit luar (kosong)',CX,CY+R2+16);
 }else{
  c.globalAlpha=1-e;
  const gr=c.createRadialGradient(CX,CY,0,CX,CY,A0*3);gr.addColorStop(0,'rgba(47,158,68,.35)');gr.addColorStop(1,'rgba(47,158,68,0)');
  c.fillStyle=gr;c.beginPath();c.arc(CX,CY,A0*3,0,6.2832);c.fill();
  c.fillStyle=WE;S.titik.forEach(q=>{c.beginPath();c.arc(CX+q[0],CY+q[1],1.8,0,6.2832);c.fill();});
  c.globalAlpha=1;c.fillStyle=lm;c.font='12px system-ui,sans-serif';c.textAlign='center';c.fillText(e<.5?'awan elektron: peluang menemukan elektron':'awan elektron hilang',CX,CY+R2+16);
 }
 // label Nukleus dengan panah
 const ax=CX+22,ay=CY-14,bx=330,by=56,an=Math.atan2(ay-by,ax-bx);
 c.strokeStyle=c.fillStyle=tx;c.lineWidth=1.2;c.beginPath();c.moveTo(bx,by);c.lineTo(ax,ay);c.stroke();
 c.beginPath();c.moveTo(ax,ay);c.lineTo(ax-9*Math.cos(an-.4),ay-9*Math.sin(an-.4));c.lineTo(ax-9*Math.cos(an+.4),ay-9*Math.sin(an+.4));c.closePath();c.fill();
 c.font='14px system-ui,sans-serif';c.textAlign='left';c.fillText('Nukleus',bx+4,by-4);
 // legenda (entri yang tidak ada di atom ini diredupkan)
 [['Elektron',WE,'−',!S.ion],['Proton',WP,'+',true],['Neutron',WN,'',S.n>0]].forEach((q,i)=>{
  const y=180+i*34;c.globalAlpha=q[3]?1:.35;partikel(380,y,10,q[1],q[2]);
  c.fillStyle=tx;c.font='14px system-ui,sans-serif';c.textAlign='left';c.fillText(q[0],398,y+5);c.globalAlpha=1;});
 // nukleus: neutron digambar dulu, proton paling atas
 const tp=['p'].concat(Array(S.n).fill('n')),pos=POS[tot];
 tp.map((k,i)=>({k,i})).sort((a,b)=>(a.k==='p')-(b.k==='p')).forEach(({k,i})=>{
  const x=CX+pos[i][0]+S.off[i][0]+1.5*Math.sin(S.t*6+i*2.1),y=CY+pos[i][1]+S.off[i][1]+1.5*Math.cos(S.t*5+i*1.7);
  partikel(x,y,RP,k==='p'?WP:WN,k==='p'?'+':'');S.hit.push({x:x,y:y,r:RP+3,t:k,i:i,bx:pos[i][0],by:pos[i][1]});});
 // elektron (mode orbit)
 if(!S.awan&&e<.995){
  const rr=R1+e*e*220,al=1-e;
  for(let k=5;k>=1;k--){const a=S.ang-k*.13;c.globalAlpha=al*(.42-k*.07);c.fillStyle=WE;c.beginPath();c.arc(CX+rr*Math.cos(a),CY+rr*Math.sin(a),3,0,6.2832);c.fill();}
  const x=CX+rr*Math.cos(S.ang),y=CY+rr*Math.sin(S.ang);c.globalAlpha=al;partikel(x,y,10,WE,'−');c.globalAlpha=1;
  S.hit.push({x:x,y:y,r:16,t:'e'});
 }
 if(S.awan&&e<.5)S.hit.push({x:CX,y:CY,r:A0*1.6,t:'e'});
 // notasi unsur: A di atas, Z di bawah
 c.fillStyle=tx;c.textAlign='left';c.font='bold 32px system-ui,sans-serif';c.fillText('H',24,64);
 c.font='bold 14px system-ui,sans-serif';c.fillText(String(tot),8,40);c.fillText('1',8,62);
 c.fillStyle=lm;c.font='12px system-ui,sans-serif';c.fillText('Hidrogen-'+tot,8,86);
}
function perbarui(){
 const p=1,n=S.n,A=p+n,el=S.ion?0:1,q=p-el;
 $('hat-tab').innerHTML='<tr><th>Proton (nomor atom, Z)</th><th>Neutron</th><th>Nomor massa (A)</th><th>Elektron</th><th>Muatan neto</th></tr><tr><td>'+p+'</td><td>'+n+'</td><td>'+A+'</td><td>'+el+'</td><td>'+(q>0?'+'+q:q)+'</td></tr>';
 const st=$('hat-st');st.innerHTML=S.ion?'Ion positif (H<sup>+</sup>)':'Atom netral';st.className='lencana'+(S.ion?'':' ok');
 $('hat-spin').textContent=n===0?'Satu proton adalah jumlah yang ganjil, jadi inti ini punya spin neto dan menjadi inti aktif MR (lihat T1.4). Inilah inti yang dipakai MRI klinis.':'Jumlah proton tetap 1 (ganjil), jadi inti ini juga punya spin neto (lihat T1.4). Meski begitu, MRI klinis memakai hidrogen-1.';
 $('hat-ion').textContent=S.ion?'Pasang kembali elektron':'Lepaskan elektron (ionisasi)';
 $('hat-mod').textContent='Tampilan: '+(S.awan?'awan elektron':'orbit');
 sJ();
 document.querySelectorAll('#hat-iso button').forEach(b=>b.setAttribute('aria-pressed',+b.dataset.n===S.n));
 kc.setAttribute('aria-label','Model atom hidrogen-'+A+': '+p+' proton dan '+n+' neutron di nukleus, '+(S.ion?'tanpa elektron (ion positif).':'satu elektron '+(S.awan?'berupa awan.':'mengorbit.')));
}
document.querySelectorAll('#hat-iso button').forEach(b=>b.onclick=()=>{S.n=+b.dataset.n;perbarui();$('hat-info').textContent=ISO[S.n];});
$('hat-ion').onclick=()=>{S.ion=!S.ion;perbarui();$('hat-info').textContent=S.ion?'Elektron dilepas (ionisasi): jumlah proton tidak berubah, tetapi tidak ada elektron lagi, sehingga atom menjadi ion bermuatan +1.':'Elektron kembali: jumlah elektron sama dengan jumlah proton lagi, atom netral.';};
$('hat-mod').onclick=()=>{S.awan=!S.awan;perbarui();$('hat-info').textContent=S.awan?'Awan elektron: tiap titik adalah satu kemungkinan posisi elektron. Menurut teori kuantum posisi elektron tidak bisa dipastikan (prinsip ketidakpastian Heisenberg), jadi awan lebih tepat daripada lintasan orbit.':'Model orbit: elektron digambar mengorbit seperti planet. Ini penyederhanaan yang berguna, tetapi bukan gambaran yang paling tepat.';};
const sJ=tombolJeda($('hat-jeda'),()=>S.jeda,v=>S.jeda=v);
tombolReset($('hat-rs'),()=>{S.n=0;S.ion=false;S.awan=false;S.jeda=false;S.lepas=0;S.off.forEach(o=>{o[0]=o[1]=0;});perbarui();$('hat-info').textContent=ISO[0];});
let dr=null;
seret(kc,{down:(x,y)=>{const h=[...S.hit].reverse().find(q=>Math.hypot(x-q.x,y-q.y)<=q.r);
 $('hat-info').textContent=h?SIFAT[h.t]:'Seret elektron, proton, atau neutron; klik untuk melihat sifatnya.';
 if(!h||(h.t==='e'&&S.awan))return false;dr=h;},
 move:(x,y)=>{if(dr.t==='e')S.ang=Math.atan2(y-CY,x-CX);else{S.off[dr.i][0]=Math.max(-45,Math.min(45,x-CX-dr.bx));S.off[dr.i][1]=Math.max(-45,Math.min(45,y-CY-dr.by));}},up:()=>{dr=null;}});
perbarui();$('hat-info').textContent=ISO[0];

LOOP.push((ts,dt)=>{
 S.lepas+=((S.ion?1:0)-S.lepas)*Math.min(1,2.5*dt);
 if(!S.jeda){if(!(dr&&dr.t==='e'))S.ang+=2.4*dt;S.t+=dt;if(S.awan)for(let k=0;k<4;k++)S.titik[Math.floor(Math.random()*S.titik.length)]=sampel();}
 gambar();
});
}});
