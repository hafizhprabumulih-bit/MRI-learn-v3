// A2 Kalkulator Larmor
MODUL.aktivitas.push({kode:'A2',setelah:'t1-6-presesi-larmor',html:`<div class='kartu'>
<b>A2. Kalkulator Larmor (T1.6, T3.2)</b>
<p class='lembut'>Frekuensi dihitung dari f = &gamma;&middot;B0. Klik batang atau pakai pilihan inti. Nilai &gamma; inti selain hidrogen berasal dari buku rujukan.</p>
<label for='nin'>Inti</label>
<select id='nin'><option value='1H'>1H (hidrogen)</option><option value='13C'>13C (karbon)</option><option value='19F'>19F (fluor)</option><option value='23Na'>23Na (natrium)</option></select>
<label for='nb'>B0: <span id='vnb'></span> T</label>
<input type='range' id='nb' min='0.5' max='3' step='0.1' value='1.5'>
<p>Frekuensi Larmor: <b id='nf'></b> MHz. Spin inti: <b id='nsp'></b>.</p>
<canvas id='kn' class='klik' width='520' height='160' role='img' aria-label='Batang frekuensi Larmor hidrogen, karbon, fluor, dan natrium pada B0 terpilih'></canvas>
<div class='baris'><button id='nreset'></button></div>
</div>`,init(){
(function(){
 let h='<table class="tabel"><thead><tr><th>Inti</th><th>Spin</th><th>&gamma; (MHz/T)</th><th>Larmor 1,5 T (MHz)</th></tr></thead><tbody>';
 NUK.forEach(k=>{h+='<tr><td><sup>'+k.slice(0,-1)+'</sup>'+k.slice(-1)+'</td><td>'+BUKU.spin[k]+'</td><td>'+fmt(gam(k),k==='1H'?2:4)+'</td><td>'+fmt(gam(k)*1.5)+'</td></tr>';});
 $('tabinti').innerHTML=h+'</tbody></table>';
})();
const kn=$('kn'),cn=kn.getContext('2d'),NW=NUK.map(()=>0);
function infoNuk(){
 const B=parseFloat($('nb').value),k=$('nin').value;
 $('vnb').textContent=fmt(B,1);$('nf').textContent=fmt(gam(k)*B);$('nsp').textContent=BUKU.spin[k];
}
function gambarNuk(){
 const B=parseFloat($('nb').value),W=kn.width,sel=$('nin').value,fmax=G*3;
 cn.clearRect(0,0,W,kn.height);cn.font='14px system-ui,sans-serif';cn.textBaseline='middle';cn.textAlign='left';
 NUK.forEach((k,i)=>{
  const f=gam(k)*B,y=24+i*36;
  NW[i]+=(f/fmax*(W-190)-NW[i])*0.12;
  cn.fillStyle=warna('--teks');cn.fillText(k,10,y);
  cn.fillStyle=k===sel?warna('--b0'):warna('--garis');cn.fillRect(60,y-11,Math.max(2,NW[i]),22);
  cn.fillStyle=warna('--teks');cn.fillText(fmt(f)+' MHz',66+NW[i],y);
 });
}
$('nin').onchange=infoNuk;$('nb').oninput=infoNuk;
kn.onclick=e=>{const b=kn.getBoundingClientRect(),my=(e.clientY-b.top)*kn.height/b.height,i=Math.floor((my-6)/36);
 if(i>=0&&i<NUK.length){$('nin').value=NUK[i];infoNuk();}};
tombolReset($('nreset'),()=>{$('nin').value='1H';$('nb').value='1.5';infoNuk();});
infoNuk();


LOOP.push(ts=>gambarNuk(ts));
}});
