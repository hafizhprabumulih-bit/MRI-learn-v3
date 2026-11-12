// A6 TR dengan pulsa berulang: pembobotan T1, T2, PD (solusi eksak relaksasi, spoiling Mxy sisa)
MODUL.aktivitas.push({kode:'A6',setelah:'t2-4-tr-te',html:`<div class='kartu'>
<b>A6. TR dengan pulsa berulang: pembobotan T1, T2, PD (T2.4)</b>
<p class='lembut'>Dua jaringan diberi pulsa RF yang sama, berulang setiap TR. Setelah tiap pulsa, Mz pulih menurut T1 dan Mxy meluruh menurut T2; sinyal dibaca pada TE. Relaksasi dihitung dari solusi eksak persamaan Bloch tanpa offset, dengan asumsi Mxy sisa dihapus (spoiling) sebelum pulsa berikutnya.</p>
<div class='ul'>
<div class='baris'><span>Preset:</span>
<button type='button' data-ulp='500,15'>Bobot T1</button><button type='button' data-ulp='3000,100'>Bobot T2</button><button type='button' data-ulp='3000,15'>Bobot PD</button></div>
<div class='gdua2'>
<div><label for='ul-ja' style='color:var(--t1)'>Jaringan A</label><select id='ul-ja'></select></div>
<div><label for='ul-jb' style='color:var(--t2)'>Jaringan B</label><select id='ul-jb'></select></div>
</div>
<div class='baris'><label for='ul-b0'>B0</label><select id='ul-b0'><option value='1.5'>1,5 T</option><option value='3'>3 T</option></select></div>
<div class='gdua2'>
<div><label for='ul-tr'>TR: <span id='ul-vtr'></span> ms</label><input type='range' id='ul-tr' min='100' max='5000' step='50' value='500'></div>
<div><label for='ul-te'>TE: <span id='ul-vte'></span> ms</label><input type='range' id='ul-te' min='5' max='200' step='5' value='15'></div>
<div><label for='ul-fa'>Sudut flip: <span id='ul-vfa'></span>&deg;</label><input type='range' id='ul-fa' min='10' max='90' step='5' value='90'></div>
<div><label for='ul-sp'>Kecepatan: <span id='ul-vsp'></span> ms waktu-simulasi per detik</label><input type='range' id='ul-sp' min='200' max='3000' step='100' value='1000'></div>
</div>
<canvas id='ul-cz' width='560' height='260' role='img' aria-label='Mz dan Mxy dua jaringan terhadap waktu, dengan penanda pulsa RF'></canvas>
<canvas id='ul-cs' width='560' height='220' role='img' aria-label='Sinyal pada TE setelah tiap pulsa untuk dua jaringan'></canvas>
<div class='baris'><button id='ul-jeda'></button><button id='ul-reset'></button></div>
<div class='gulir'><table class='tabel'><thead><tr><th>Keadaan tunak (rumus)</th><th style='color:var(--t1)'>A</th><th style='color:var(--t2)'>B</th></tr></thead><tbody id='ul-tb'></tbody></table></div>
<p id='ul-ps' role='status' aria-live='polite' class='lembut'></p>
</div>
<p class='lembut'>Catatan: nilai T1, T2, PD dan skala B0 berasal dari tabel jaringan modul (hanya jaringan yang punya PD). Penamaan bobot (T1/T2/PD) memakai ambang kasar untuk 1,5 T dan hanya sebagai petunjuk belajar. Pada TE lebih besar dari TR, sinyal tidak dibaca.</p>
</div>`,init(){
// TODO-VERIFIKASI: ambang penamaan bobot (TR, TE) hanya petunjuk belajar; preset 500/15, 3000/100, 3000/15 ms perlu dicek ke buku rujukan.
const JAR=DATA.JARINGAN.filter(j=>j.PD!==undefined);   // hanya jaringan yang punya PD
['ul-ja','ul-jb'].forEach(id=>{$(id).innerHTML=JAR.map((j,i)=>"<option value='"+i+"'>"+j.nama+'</option>').join('');});
$('ul-ja').value=JAR.findIndex(j=>j.id==='fat');$('ul-jb').value=JAR.findIndex(j=>j.id==='csf');
const S={X:null,t:0,lastP:-1e9,teDone:true,sig:[],hist:[],lastPush:-1e9,jeda:false};
const par=id=>{const j=JAR[+$(id).value],w=waktuRelaksasi(j,parseFloat($('ul-b0').value));return{T1:w.T1,T2:w.T2,PD:j.PD};};
let PA=par('ul-ja'),PB=par('ul-jb');
const cz=$('ul-cz'),cs=$('ul-cs'),dpr=Math.min(window.devicePixelRatio||1,2);
[cz,cs].forEach(c=>{const w=c.width,h=c.height;c.width=w*dpr;c.height=h*dpr;c.getContext('2d').scale(dpr,dpr);});
const gz=cz.getContext('2d'),gs=cs.getContext('2d');
const val=id=>parseFloat($(id).value);
function reset(){PA=par('ul-ja');PB=par('ul-jb');S.X=[{z:1,xy:0},{z:1,xy:0}];S.t=0;S.lastP=-1e9;S.teDone=true;S.sig=[];S.hist=[];S.lastPush=-1e9;pulsa();tabel();}
function pulsa(){const a=val('ul-fa')*Math.PI/180;S.X.forEach(m=>{m.xy=m.z*Math.sin(a);m.z=m.z*Math.cos(a);});   // Mxy sisa dihapus (spoiling)
 S.lastP=S.t;S.teDone=false;S.sig.push({k:(S.sig.length?S.sig[S.sig.length-1].k:0)+1,s:[0,0],pend:true});if(S.sig.length>10)S.sig.shift();}
function rel(h){[PA,PB].forEach((P,i)=>{const m=S.X[i];m.xy*=Math.exp(-h/P.T2);m.z=1+(m.z-1)*Math.exp(-h/P.T1);});}
function push(f){const TR=val('ul-tr'),win=Math.max(4*TR,600);
 if(f||S.t-S.lastPush>TR*4/500){S.hist.push([S.t,S.X[0].z,S.X[0].xy,S.X[1].z,S.X[1].xy]);S.lastPush=S.t;}
 while(S.hist.length&&S.hist[0][0]<S.t-win*1.1)S.hist.shift();}   // riwayat dipangkas agar tidak membengkak
function maju(d){const TR=val('ul-tr'),TE=val('ul-te');
 while(d>1e-9){let nx=S.lastP+TR-S.t,ev=0;
  if(!S.teDone&&TE<TR&&S.lastP+TE-S.t<nx-1e-9){nx=S.lastP+TE-S.t;ev=1;}
  if(S.teDone===false&&TE>=TR)S.teDone=true;
  const h=Math.max(1e-6,Math.min(d,nx,5));rel(h);S.t+=h;d-=h;
  if(ev&&Math.abs(S.lastP+TE-S.t)<1e-6){const r=S.sig[S.sig.length-1];r.s=[PA.PD*S.X[0].xy,PB.PD*S.X[1].xy];r.pend=false;S.teDone=true;push(1);}
  else if(S.t>=S.lastP+TR-1e-6){push(1);pulsa();push(1);}
  else push(0);}}
function tunak(P,TR,TE,a){const E=Math.exp(-TR/P.T1),M=(1-E)/(1-E*Math.cos(a));return{mz:M,s:P.PD*M*Math.sin(a)*Math.exp(-TE/P.T2)};}
function tabel(){const TR=val('ul-tr'),TE=val('ul-te'),a=val('ul-fa')*Math.PI/180,A=tunak(PA,TR,TE,a),B=tunak(PB,TR,TE,a);
 const ct=(A.s+B.s)>0?Math.abs(A.s-B.s)/(A.s+B.s):0;
 $('ul-tb').innerHTML='<tr><td>Mz sebelum pulsa</td><td>'+fmt(A.mz)+'</td><td>'+fmt(B.mz)+'</td></tr><tr><td>Sinyal pada TE</td><td>'+fmt(A.s,3)+'</td><td>'+fmt(B.s,3)+'</td></tr><tr><td>Kontras |A&minus;B|/(A+B)</td><td colspan="2">'+fmt(ct,3)+'</td></tr>';
 let w='campuran';if(TR<=800&&TE<=30)w='kuat ke bobot T1';else if(TR>=2000&&TE>=80)w='kuat ke bobot T2';else if(TR>=2000&&TE<=30)w='kuat ke bobot PD';
 $('ul-ps').textContent='Pengaturan ini condong '+w+'. TR '+TR+' ms terhadap T1 A '+Math.round(PA.T1)+' ms dan T1 B '+Math.round(PB.T1)+' ms: '+(TR<Math.min(PA.T1,PB.T1)?'kedua jaringan belum sempat pulih penuh.':TR>3*Math.max(PA.T1,PB.T1)?'kedua jaringan hampir pulih penuh, perbedaan T1 menghilang.':'jaringan ber-T1 panjang pulih lebih lambat dan tampak lebih gelap.');}
function gambar(C){
 const W=560,H=260,L=34,R=W-8,T=14,B=H-24,TR=val('ul-tr'),win=Math.max(4*TR,600),y=v=>B-v*(B-T),x=tt=>L+(tt-(S.t-win))/win*(R-L);
 gz.clearRect(0,0,W,H);gz.font='12px system-ui,sans-serif';gz.textAlign='left';gz.lineWidth=1;gz.strokeStyle=C.g;gz.fillStyle=C.l;
 [0,.5,1].forEach(v=>{gz.beginPath();gz.moveTo(L,y(v));gz.lineTo(R,y(v));gz.stroke();gz.fillText(fmt(v,1),4,y(v)+4);});
 gz.fillText('waktu-simulasi (jendela '+Math.round(win)+' ms); garis putus-putus = pulsa RF',L,H-6);
 gz.save();gz.beginPath();gz.rect(L,0,R-L,H);gz.clip();
 gz.setLineDash([3,4]);for(let p=S.lastP;p>S.t-win;p-=TR){gz.strokeStyle=C.l;gz.beginPath();gz.moveTo(x(p),T);gz.lineTo(x(p),B);gz.stroke();}gz.setLineDash([]);
 const ln=(k,col,w,dash)=>{gz.strokeStyle=col;gz.lineWidth=w;gz.setLineDash(dash||[]);gz.beginPath();let f=1;S.hist.forEach(h=>{const px=x(h[0]);if(px<L-20)return;const py=y(Math.min(1.02,Math.max(0,h[k])));f?gz.moveTo(px,py):gz.lineTo(px,py);f=0;});
  const cur=k===1?S.X[0].z:k===2?S.X[0].xy:k===3?S.X[1].z:S.X[1].xy;gz.lineTo(x(S.t),y(Math.min(1,Math.max(0,cur))));gz.stroke();gz.setLineDash([]);};
 ln(1,C.a,2.5);ln(3,C.b,2.5);ln(2,C.a,1,[2,3]);ln(4,C.b,1,[2,3]);gz.restore();
 gz.fillStyle=C.bg;gz.fillRect(R-196,T-2,194,34);gz.fillStyle=C.a;gz.fillText('Mz A',R-190,T+11);gz.fillStyle=C.b;gz.fillText('Mz B',R-140,T+11);gz.fillStyle=C.l;gz.fillText('garis putus-putus halus: |Mxy|',R-190,T+26);
 const H2=220,b2=H2-44,t2=14,bw=(W-L-8)/10;gs.clearRect(0,0,W,H2);gs.font='12px system-ui,sans-serif';gs.textAlign='left';gs.strokeStyle=C.g;gs.fillStyle=C.l;
 gs.beginPath();gs.moveTo(L,b2);gs.lineTo(W-8,b2);gs.stroke();gs.fillText('Sinyal pada TE setelah tiap pulsa (pulsa ke-)',L,H2-6);gs.fillText('1',6,t2+8);gs.fillText('0',6,b2);
 S.sig.forEach((r,i)=>{const bx=L+i*bw+bw*.12,w=bw*.38;[0,1].forEach(k=>{const v=Math.min(1,r.s[k]),hh=v*(b2-t2);gs.fillStyle=k?C.b:C.a;gs.globalAlpha=r.pend?.3:1;gs.fillRect(bx+k*w,b2-hh,w-1,hh);});gs.globalAlpha=1;gs.fillStyle=C.l;gs.fillText(r.k,bx+w*.6,b2+13);});
}
['tr','te','fa','sp'].forEach(k=>{$('ul-'+k).oninput=()=>{$('ul-v'+k).textContent=$('ul-'+k).value;tabel();};$('ul-v'+k).textContent=$('ul-'+k).value;});
['ul-ja','ul-jb','ul-b0'].forEach(id=>$(id).onchange=reset);
const sJ=tombolJeda($('ul-jeda'),()=>S.jeda,v=>S.jeda=v);
tombolReset($('ul-reset'),()=>{S.jeda=false;sJ();reset();});
document.querySelectorAll('[data-ulp]').forEach(b=>b.onclick=()=>{const[a,c]=b.dataset.ulp.split(',');$('ul-tr').value=a;$('ul-te').value=c;['tr','te'].forEach(k=>$('ul-v'+k).textContent=$('ul-'+k).value);reset();});
reset();
LOOP.push((ts,dt)=>{if(!S.jeda)maju(dt*val('ul-sp'));gambar({bg:warna('--permukaan'),g:warna('--garis'),l:warna('--lembut'),a:warna('--t1'),b:warna('--t2')});});
}});
