// Latihan di kartu T1.4 (HTML-nya ada di materi/t1-4b-inti-aktif-mr.js)
MODUL.init.push(function(){
// ---- Latihan inti aktif MR ----
const INTI={'1H':[1,0],'12C':[6,6],'13C':[6,7],'6Li':[3,3],'14N':[7,7],'19F':[9,10],'23Na':[11,12]};
function cekInti(){
 const z=parseInt($('iz').value),n=parseInt($('ineu').value),o=$('ihasil');
 if(!(z>=1)||!(n>=0)){o.className='';o.textContent='Isi jumlah proton (minimal 1) dan neutron (0 atau lebih).';return;}
 const a=z+n;let t,ok=false;
 if(z%2===0&&n%2===0)t='Proton dan neutron sama-sama genap, spinnya saling meniadakan. Tidak ada spin neto, jadi tidak aktif MR.';
 else if(a%2===1){ok=true;t='Nomor massa ganjil, sehingga ada spin neto setengah-bulat dan momen magnetik. Inti ini aktif MR.';}
 else t='Proton dan neutron sama-sama ganjil. Ada spin neto (bulat), tetapi hampir tidak teramati di MRI, jadi tidak dipakai.';
 o.className=ok?'ok':'no';o.textContent='Nomor massa = '+a+'. '+t;
}
$('ipil').onchange=()=>{const v=INTI[$('ipil').value];if(v){$('iz').value=v[0];$('ineu').value=v[1];}cekInti();};
$('iz').oninput=$('ineu').oninput=()=>{$('ipil').value='';cekInti();};
cekInti();
});
