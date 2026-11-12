/* Belajar MRI (MRI Edu) - app.js */

// Global Constants & Physics Reference Data
const KONSTANTA = {
  gamma_1H: 42.58,
  jaringan: {
    fat: { nama: "Adiposa / Lemak (Fat)", t1: 260, t2: 80, pd: 0.90, desc: "T1 sangat pendek karena gerak molekul lipid dekat dengan frekuensi Larmor. T2 sedang.", klinis: "Sangat terang (hiperintens) pada T1w." },
    wm: { nama: "Substansia Alba (White Matter)", t1: 780, t2: 90, pd: 0.70, desc: "Kaya mielin lipidik aksonal, relaksasi T1 lebih cepat dibanding gray matter.", klinis: "Terang pada T1w, gelap pada T2w." },
    gm: { nama: "Substansia Grisea (Gray Matter)", t1: 920, t2: 100, pd: 0.82, desc: "Kandungan air bebas lebih tinggi dibanding Substansia Alba.", klinis: "Intermediet pada T1w dan T2w." },
    csf: { nama: "Cairan Serebrospinal (CSF)", t1: 4000, t2: 2000, pd: 1.0, desc: "Cairan bebas murni. T1 dan T2 sangat panjang.", klinis: "Sangat gelap pada T1w, sangat terang benderang pada T2w." },
    muscle: { nama: "Otot Skelet (Muscle)", t1: 870, t2: 45, pd: 0.75, desc: "T2 pendek karena ikatan hidrasi protein miofibril yang rapat.", klinis: "Gelap pada sekuens T2 standar." },
    edema: { nama: "Edema Patologis / Lesi", t1: 2200, t2: 300, pd: 0.95, desc: "Akumulasi air bebas akibat inflamasi atau tumor.", klinis: "Sangat menonjol (hiperintens) pada T2w dan STIR." }
  }
};

const DAFTAR_MODUL = [
  {
    id: "M01",
    kode: "M01",
    judul: "Fisika Dasar MRI & Presesi Spin",
    sub: "Atom Hidrogen, Vektor Magnetisasi Netto, dan Larmor",
    level: "T1 - T3 Dasar",
    simpul: [
      {
        kode: "M01.T1.1",
        tingkat: 1,
        judul: "Peran MRI Klinis & Proton Hidrogen (¹H)",
        singkat: "Atom Hidrogen (¹H) memiliki satu proton tunggal (bilangan ganjil) yang menghasilkan momentum magnetik netto dan jumlahnya paling melimpah dalam tubuh manusia.",
        lengkap: "Inti atom dengan nomor massa ganjil memiliki momen magnetik intrinsik yang disebut spin. Hidrogen adalah pilihan ideal karena tubuh manusia terdiri dari ~65-70% air dan lemak, menghasilkan densitas proton tinggi untuk sinyal RF tanpa radiasi pengion.",
        analogi: "Setiap proton seperti jarum kompas mikro yang berputar bebas ketika tidak ada medan magnet luar."
      },
      {
        kode: "M01.T1.5",
        tingkat: 1,
        judul: "Presesi dan Persamaan Frekuensi Larmor",
        singkat: "Dalam medan B0, spin berputar presesi dengan frekuensi sebanding: f₀ = γ · B₀. Untuk ¹H, γ = 42.58 MHz/Tesla.",
        lengkap: "Pengaruh medan magnet B0 memaksa sumbu rotasi spin mengalami presesi. Pada medan 1.5 Tesla, frekuensi presesi hidrogen adalah ~63.87 MHz (gelombang radio VHF). Pada 3.0 Tesla menjadi ~127.74 MHz.",
        analogi: "Seperti gasing mainan yang miring dan berputar mengitari tarikan gravitasi bumi."
      },
      {
        kode: "M01.T2.1",
        tingkat: 2,
        judul: "Vektor Magnetisasi Netto (NMV / M₀)",
        singkat: "Kelebihan sedikit spin pada status energi rendah sejajar paralel dengan B0 menghasilkan Net Magnetization Vector makroskopik.",
        lengkap: "Menurut distribusi Boltzmann, terdapat sedikit kelebihan spin pada status paralel searah B0 dibanding antiparalel. Akumulasi triliunan spin ini membentuk vektor magnetisasi makroskopik sepanjang sumbu longitudinal (z).",
        analogi: "Kerumunan di stadion: selisih jumlah penonton yang berdiri vs duduk menentukan arah gemuruh total."
      }
    ]
  },
  {
    id: "M02",
    kode: "M02",
    judul: "Relaksasi T1/T2 & Pembobotan Kontras",
    sub: "Pemulihan Longitudinal, Peluruhan Transversal, dan Kontras",
    level: "T1 - T2 Menengah",
    simpul: [
      {
        kode: "M02.T1.2",
        tingkat: 1,
        judul: "Dua Proses Relaksasi: T1 vs T2",
        singkat: "T1 adalah waktu pemulihan magnetisasi longitudinal (~63%), sedangkan T2 adalah waktu peluruhan koherensi transversal (~37%).",
        lengkap: "Relaksasi T1 (spin-lattice) melibatkan pertukaran energi panas spin ke kisi molekuler. Relaksasi T2 (spin-spin) terjadi akibat interaksi medan magnet lokal antar molekul tetangga yang menghilangkan keseragaman fase presesi.",
        analogi: "T1 seperti mengisi ulang baterai; T2 seperti sekelompok pelari yang start bersama lalu tercerai-berai kecepatannya."
      },
      {
        kode: "M02.T1.4",
        tingkat: 1,
        judul: "Pembobotan Citra: T1w, T2w, dan PDw",
        singkat: "T1w (TR pendek, TE pendek), T2w (TR panjang, TE panjang), PDw (TR panjang, TE pendek).",
        lengkap: "Rumus sinyal Spin Echo: S = PD · (1 - e^(-TR/T1)) · e^(-TE/T2). TR mengontrol kontras pemulihan T1; TE mengontrol kontras peluruhan T2.",
        analogi: "Dua kenop pengatur kontras foto: satu mengatur pemulihan cahaya latar, satu mengatur peluruhan bayangan objek."
      }
    ]
  },
  {
    id: "M03A",
    kode: "M03A",
    judul: "Sekuens Spin Echo & Inversion Recovery",
    sub: "Konvensional SE, Turbo Spin Echo (TSE/FSE), STIR, dan FLAIR",
    level: "T1 - T3",
    simpul: [
      {
        kode: "M03A.T1.1",
        tingkat: 1,
        judul: "Sekuens Spin Echo (SE) Standar",
        singkat: "Pulsa eksitasi 90° diikuti pulsa refokusing 180° pada interval TE/2 untuk membentuk echo di waktu TE.",
        lengkap: "SE adalah baku emas kontras karena mengoreksi dephasing akibat ketidakhomogenan medan statis B0.",
        analogi: "Satu kali ayunan bandul, satu kali pantulan gema suara."
      },
      {
        kode: "M03A.T1.3",
        tingkat: 1,
        judul: "Inversion Recovery: STIR & FLAIR",
        singkat: "Pulsa inversi 180° membalikkan magnetisasi. Eksitasi 90° dikirim saat jaringan melewati titik nol (null point TI ≈ 0.693 · T1).",
        lengkap: "STIR memadamkan lemak dengan TI pendek (~150-180 ms), FLAIR memadamkan cairan CSF dengan TI panjang (~2000-2500 ms).",
        analogi: "Menembak foto saat ayunan bandul tepat melintasi garis tengah nol kecepatan."
      }
    ]
  },
  {
    id: "M04B",
    kode: "M04B",
    judul: "Ruang-k (k-Space) & Rekonstruksi FFT",
    sub: "Matriks Data Mentah, Frekuensi Nyquist, dan Rekonstruksi Citra",
    level: "T2 Menengah",
    simpul: [
      {
        kode: "M04B.T2.1",
        tingkat: 2,
        judul: "Arsitektur Matriks Ruang-k",
        singkat: "Ruang-k adalah matriks data domain frekuensi spasial mentah sebelum direkonstruksi menjadi citra visual oleh Fast Fourier Transform (FFT).",
        lengkap: "Sumbu horizontal mewakili pengodean frekuensi (kx), sumbu vertikal mewakili pengodean fase (ky). Setiap TR mengisi satu baris ruang-k.",
        analogi: "Lemari arsip dengan ratusan laci bernomor baris dan kolom yang menyimpan catatan gelombang."
      },
      {
        kode: "M04B.T2.10",
        tingkat: 2,
        judul: "Pusat (Kontras) vs Tepi (Resolusi Detail)",
        singkat: "Pusat ruang-k mengandung kontras dan amplitudo sinyal tinggi. Tepi mengandung detail tepi dan resolusi spasial tajam.",
        lengkap: "Jika pusat ruang-k ditutup, citra menjadi sketsa garis kontur. Jika tepi ditutup, citra tampak kabur (blur) namun kontras utuh.",
        analogi: "Pusat adalah warna cat dasar lukisan; tepi adalah garis kontur pensil penegas detail."
      }
    ]
  },
  {
    id: "M07",
    kode: "M07",
    judul: "Keselamatan MR (MR Safety Standards)",
    sub: "Zona ACR, Bahaya Efek Proyektil, SAR & Standar ASTM Implan",
    level: "T1 - T3 Wajib",
    simpul: [
      {
        kode: "M07.T1.1",
        tingkat: 1,
        judul: "Empat Zona Keselamatan Fasilitas MRI (ACR)",
        singkat: "Zona I (Publik Bebas), Zona II (Pendaftaran/Skrining), Zona III (Area Terkontrol), Zona IV (Ruang Scanner Magnet Berisiko Tinggi).",
        lengkap: "Pedoman ketat American College of Radiology untuk mencegah kecelakaan fatal proyektil dan luka bakar RF.",
        analogi: "Pos pengamanan bandara: dari lobi tiket umum hingga ke dalam kokpit pesawat."
      },
      {
        kode: "M07.T2.2",
        tingkat: 2,
        judul: "Specific Absorption Rate (SAR) & Pemanasan RF",
        singkat: "Laju penyerapan daya RF oleh jaringan tubuh (W/kg). Batas normal whole-body menurut IEC 60601-2-33 adalah 2.0 W/kg.",
        lengkap: "Pulsa RF 180° berulang mentransfer energi termal. Kenaikan suhu tubuh inti tidak boleh melampaui 1°C dalam kondisi normal.",
        analogi: "Microwave berdaya rendah yang menghangatkan secangkir air secara bertahap."
      }
    ]
  }
];

const KAMUS_DATA = [
  { istilah: "Presesi Larmor", lambang: "f₀ = γ · B₀", mod: "M01", arti: "Gerakan rotasi presesi momen magnetik inti atom di sekeliling arah vektor medan magnet eksternal B0.", klinis: "Menentukan frekuensi pemancar pulsa RF agar terjadi resonansi (misal 63.87 MHz pada 1.5T)." },
  { istilah: "Net Magnetization Vector (NMV)", lambang: "M₀", mod: "M01", arti: "Vektor resultan makroskopik dari selisih kelebihan jumlah spin proton yang berada pada status energi paralel terhadap B0.", klinis: "Besarnya M0 menentukan ketersediaan Signal-to-Noise Ratio (SNR) awal." },
  { istilah: "Relaksasi T1 (Spin-Lattice)", lambang: "T1", mod: "M02", arti: "Waktu yang dibutuhkan magnetisasi longitudinal (Mz) untuk pulih hingga ~63% dari nilai setimbangnya (M0).", klinis: "Lemak memiliki T1 sangat pendek (tampak terang/hiperintens), sedangkan air/CSF memiliki T1 panjang (gelap)." },
  { istilah: "Relaksasi T2 (Spin-Spin)", lambang: "T2", mod: "M02", arti: "Waktu yang dibutuhkan magnetisasi transversal (Mxy) untuk meluruh koherensi fasenya hingga tersisa ~37%.", klinis: "Air murni/CSF memiliki T2 panjang (tampak terang/hiperintens), sedangkan jaringan padat/otot tampak gelap." },
  { istilah: "Free Induction Decay (FID)", lambang: "FID", mod: "M01", arti: "Sinyal radiofrekuensi transien yang diinduksi pada koil penerima tepat setelah pulsa eksitasi 90° dimatikan.", klinis: "Meluruh sangat cepat oleh dephasing medan magnet mikroskopik lokal (T2*)." },
  { istilah: "Repetition Time (TR)", lambang: "TR", mod: "M02", arti: "Interval waktu antara penerapan dua pulsa eksitasi RF 90° yang berurutan pada irisan anatomis yang sama.", klinis: "TR pendek mengendalikan pembobotan T1; TR panjang meniadakan perbedaan pemulihan T1." },
  { istilah: "Echo Time (TE)", lambang: "TE", mod: "M02", arti: "Interval waktu dari titik tengah pulsa eksitasi 90° hingga puncak terbentuknya sinyal echo pada koil.", klinis: "TE panjang menonjolkan dephasing transversal T2; TE pendek meminimalkan efek peluruhan T2." },
  { istilah: "Sekuens Spin Echo (SE)", lambang: "90° - 180°", mod: "M03A", arti: "Sekuens yang menggunakan pulsa refokusing 180° pada interval TE/2 untuk membalikkan dephasing magnetik statis.", klinis: "Baku emas kontras anatomi dan tidak rentan terhadap artefak suseptibilitas magnetik." },
  { istilah: "Ruang-k (k-Space)", lambang: "kx, ky", mod: "M04B", arti: "Matriks representasi frekuensi spasial mentah data MRI sebelum diproses menjadi citra melalui Fast Fourier Transform.", klinis: "Bagian tengah menentukan kontras & kecerahan, tepi luar menentukan resolusi dan ketajaman kontur tepi." },
  { istilah: "Specific Absorption Rate (SAR)", lambang: "W/kg", mod: "M07", arti: "Laju penyerapan daya radiofrekuensi oleh massa tubuh pasien dalam satuan watt per kilogram.", klinis: "Batas mode normal IEC adalah 2.0 W/kg untuk mencegah peningkatan suhu tubuh lebih dari 1.0°C." },
  { istilah: "Inversion Recovery (STIR/FLAIR)", lambang: "TI", mod: "M03A", arti: "Sekuens dengan pulsa inversi 180° di awal untuk memadamkan sinyal jaringan target pada waktu TI (Null Point).", klinis: "STIR memadamkan lemak (TI pendek ~150ms), FLAIR memadamkan CSF cairan otak (TI panjang ~2200ms)." },
  { istilah: "Efek Proyektil (Missile Effect)", lambang: "Zone IV", mod: "M07", arti: "Gaya tarik magnetik statis yang menarik benda feromagnetik secara akseleratif ke dalam lubang scanner gantry.", klinis: "Penyebab insiden keselamatan paling fatal di MRI. Benda feromagnetik dilarang masuk Zona IV." }
];

// Initial Study Notes Data
let CATATAN_LIST = [
  {
    id: "N01",
    judul: "Rumus Cepat Penentuan Kontras TR/TE",
    kategori: "Fisika Dasar",
    isi: "1. T1-Weighted (T1w): TR Pendek (< 600 ms) & TE Pendek (< 20 ms). Lemak hiperintens, CSF hipointens.\n2. T2-Weighted (T2w): TR Panjang (> 2000 ms) & TE Panjang (> 80 ms). CSF & cairan edema sangat terang benderang.\n3. Proton Density (PDw): TR Panjang (> 2000 ms) & TE Pendek (< 20 ms). Menggambarkan jumlah absolut proton hidrogen.",
    tanggal: "Hari ini"
  },
  {
    id: "N02",
    judul: "Protokol Strategi Reduksi SAR Klinis",
    kategori: "Safety MR",
    isi: "Jika SAR melebihi 2.0 W/kg pada pasien obesitas atau anak-anak:\n- Turunkan flip angle refokusing (misal 180° -> 135° atau 120°).\n- Perpanjang Repetition Time (TR) untuk memberi jeda termal dissipasi panas tubuh.\n- Kurangi Echo Train Length (ETL) pada sekuens TSE/FSE.",
    tanggal: "Kemarin"
  },
  {
    id: "N03",
    judul: "Karakteristik Ruang-k: Pusat vs Tepi",
    kategori: "Sekuens Klinis",
    isi: "- Pusat Ruang-k (Frekuensi Rendah): Mengandung amplitudo energi sinyal terbesar. Mengendalikan kontras citra dan SNR.\n- Tepi Ruang-k (Frekuensi Tinggi): Mengandung variasi spasial tajam. Menentukan ketajaman tepi batas organ dan resolusi detail.",
    tanggal: "2 hari lalu"
  }
];

const KUNCI_CATATAN = "belajar_mri_catatan_v1";

function escapeHtml(teks) {
  return String(teks ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function simpanCatatanKeStorage() {
  try {
    localStorage.setItem(KUNCI_CATATAN, JSON.stringify(CATATAN_LIST));
  } catch (e) {
    console.warn("Catatan gagal disimpan ke localStorage", e);
  }
}

function muatCatatanDariStorage() {
  try {
    const mentah = localStorage.getItem(KUNCI_CATATAN);
    if (!mentah) return;
    const data = JSON.parse(mentah);
    if (Array.isArray(data)) CATATAN_LIST = data;
  } catch (e) {
    console.warn("Catatan tersimpan rusak, memakai catatan awal", e);
  }
}

function idCatatanBaru() {
  return (window.crypto && crypto.randomUUID)
    ? "N" + crypto.randomUUID()
    : "N" + Date.now() + "-" + Math.random().toString(36).slice(2, 8);
}

// App State
let tabAktif = "beranda";
let modulAktifId = "M01";
let filterTingkatModul = 0;
let modeBelajarModul = "singkat"; // 'singkat' | 'lengkap' | 'analogi'

// Avatar State
let currentAvatar = "male"; // 'male' | 'female' | 'neuro' | 'physicist'
let currentUserName = "Dr. Radiografer";

// Streak State
let streakDays = 3;

// Learning Animation State
let animTopicIndex = 0; // 0: Spin Echo, 1: T1/T2, 2: Ruang-k
let animStep = 0;
let animIsPlaying = false;
let animTimer = null;

// Simulator State
let b0Terpilih = 1.5;
let gammaTerpilih = 42.58;
let intiTerpilih = "¹H";
let pulsaRfAktif = false;
let presesiAnimId = null;
let jaringanTerpilih = "fat";
let kspaceFilter = "full";

// Notes Filter State
let filterKategoriCatatan = "Semua";

/* ============================================================
   1. NAVIGATION & TAB SWITCHING
============================================================ */
function pindahTab(namaTab) {
  tabAktif = namaTab;

  document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));
  const target = document.getElementById(`tab-${namaTab}`);
  if (target) target.classList.add('active');

  // Update Bottom Nav Highlighting
  const navIds = ['beranda', 'animasi', 'simulasi', 'catatan', 'glosarium'];
  navIds.forEach(id => {
    const btn = document.getElementById(`nav-${id}`);
    if (btn) {
      if (id === namaTab) {
        btn.className = "nav-item flex-1 py-1 flex flex-col items-center justify-center text-cyan-400 transition";
      } else {
        btn.className = "nav-item flex-1 py-1 flex flex-col items-center justify-center text-slate-400 hover:text-slate-200 transition";
      }
    }
  });

  window.scrollTo({ top: 0, behavior: 'smooth' });

  // Tab-specific hooks
  if (namaTab === 'animasi') {
    renderAnimasiCanvas();
  } else if (namaTab === 'simulasi') {
    inisialisasiCanvasPresesi();
    renderChipsJaringan();
    hitungKontrasWeb();
    inisialisasiCanvasKSpace();
    hitungSarWeb();
  } else if (namaTab === 'catatan') {
    renderDaftarCatatan();
  } else if (namaTab === 'glosarium') {
    renderKamusWeb(KAMUS_DATA);
  }
}

/* ============================================================
   2. FLAME STREAK DIALOG & MANAGEMENT
============================================================ */
function bukaModalStreak() {
  document.getElementById('modal-streak').classList.remove('hidden');
  renderStreakModalUI();
}

function tutupModalStreak() {
  document.getElementById('modal-streak').classList.add('hidden');
}

function renderStreakModalUI() {
  document.getElementById('streak-modal-count').innerText = `${streakDays} Hari`;
  document.getElementById('header-streak-badge').innerText = `${streakDays} Hari`;

  const container = document.getElementById('wadah-hari-streak');
  if (!container) return;
  container.innerHTML = "";

  const hariLabels = ["Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"];
  hariLabels.forEach((hari, i) => {
    const isCompleted = i < (streakDays % 7 || 7);
    const dayBox = document.createElement('div');
    dayBox.className = "flex flex-col items-center";
    dayBox.innerHTML = `
      <span class="text-[9px] text-slate-400 mb-1">${hari}</span>
      <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold ${
        isCompleted ? 'bg-amber-500 text-slate-950 shadow-sm' : 'bg-slate-800 text-slate-500'
      }">
        ${isCompleted ? '✓' : '·'}
      </div>
    `;
    container.appendChild(dayBox);
  });
}

function tambahHariStreak() {
  streakDays++;
  renderStreakModalUI();
}

/* ============================================================
   3. AVATAR & USER PROFILE MANAGEMENT
============================================================ */
function bukaModalAvatar() {
  document.getElementById('input-modal-username').value = currentUserName;
  updateAvatarSelectionUI();
  document.getElementById('modal-avatar').classList.remove('hidden');
}

function tutupModalAvatar() {
  document.getElementById('modal-avatar').classList.add('hidden');
}

function pilihAvatarPreset(type) {
  currentAvatar = type;
  updateAvatarSelectionUI();
}

function updateAvatarSelectionUI() {
  const options = ['male', 'female', 'neuro', 'physicist'];
  options.forEach(opt => {
    const el = document.getElementById(`av-opt-${opt}`);
    if (el) {
      if (opt === currentAvatar) {
        el.className = "p-2.5 rounded-xl border border-cyan-500 bg-cyan-950/60 flex items-center gap-2.5 cursor-pointer transition shadow";
      } else {
        el.className = "p-2.5 rounded-xl border border-slate-800 bg-slate-950 flex items-center gap-2.5 cursor-pointer transition hover:border-slate-700";
      }
    }
  });
}

function simpanProfilAvatar() {
  const nameInput = document.getElementById('input-modal-username').value.trim();
  if (nameInput) currentUserName = nameInput;

  document.getElementById('header-user-name').innerText = currentUserName;
  const avatarImg = document.getElementById('header-avatar-img');

  if (currentAvatar === 'male') {
    avatarImg.src = 'user_avatar_male.jpg';
  } else if (currentAvatar === 'female') {
    avatarImg.src = 'user_avatar_female.jpg';
  } else if (currentAvatar === 'neuro') {
    avatarImg.src = 'user_avatar_male.jpg'; // fallback with image
  } else {
    avatarImg.src = 'user_avatar_female.jpg';
  }

  tutupModalAvatar();
}

/* ============================================================
   4. ANIMASI BELAJAR (LEARNING ANIMATIONS ENGINE)
============================================================ */
const ANIM_DATA = [
  {
    topik: "Sekuens Spin Echo (SE)",
    totalSteps: 4,
    steps: [
      {
        subJudul: "Langkah 1: Vektor Longitudinal (M0) Sejajar B0",
        desc: "Spin proton hidrogen berada dalam keseimbangan termal di bawah medan magnet statis B0, membentuk vektor magnetisasi netto M0 di sepanjang sumbu longitudinal (sumbu Z).",
        badge: "Keseimbangan M0",
        overlay: "M0 Longitudinal (Z)",
        type: "se_step1"
      },
      {
        subJudul: "Langkah 2: Pulsa Eksitasi RF 90° (Nutasi ke Bidang XY)",
        desc: "Pulsa radiofrekuensi 90° beresonansi dengan frekuensi Larmor, memutar vektor magnetisasi 90 derajat ke bidang transversal (XY). Fase presesi spin menjadi koheren (in-phase).",
        badge: "Eksitasi 90°",
        overlay: "Fase Koheren di Transversal (XY)",
        type: "se_step2"
      },
      {
        subJudul: "Langkah 3: Dephasing Transversal Akibat Inhomogenitas Medan",
        desc: "Setelah pulsa 90° dimatikan, spin-spin mulai berputar dengan kecepatan sedikit berbeda (dephasing) akibat ketidakhomogenan medan lokal. Sinyal transversal meluruh (T2* decay).",
        badge: "Dephasing T2*",
        overlay: "Dephasing (Spin Cepat vs Lambat)",
        type: "se_step3"
      },
      {
        subJudul: "Langkah 4: Pulsa Refokusing 180° & Terbentuknya Spin Echo",
        desc: "Pada interval TE/2, pulsa 180° membalikkan posisi spin sebesar 180 derajat. Spin yang cepat sekarang berada di belakang dan mengejar spin yang lambat, menghasilkan refokusing sempurna di waktu TE (Spin Echo).",
        badge: "Spin Echo di TE",
        overlay: "Refokusing Koheren (Echo Maksimal)",
        type: "se_step4"
      }
    ]
  },
  {
    topik: "Kurva Relaksasi T1 & T2",
    totalSteps: 3,
    steps: [
      {
        subJudul: "Tahap 1: Keadaan Awal Pasca Eksitasi RF 90°",
        desc: "Tepat di t=0 setelah eksitasi 90°, magnetisasi longitudinal Mz = 0 (belum pulih), sedangkan magnetisasi transversal Mxy mencapai nilai puncak 100%.",
        badge: "t = 0 ms",
        overlay: "Mz = 0 | Mxy = 100%",
        type: "t1t2_step1"
      },
      {
        subJudul: "Tahap 2: Waktu Pemulihan T1 (63%) & Peluruhan T2 (37%)",
        desc: "Pada waktu t = T1, Mz telah pulih sebesar 63% dari nilai setimbang (Spin-Lattice). Pada waktu t = T2, sinyal transversal Mxy meluruh hingga tersisa hanya 37% (Spin-Spin).",
        badge: "Titik 63% & 37%",
        overlay: "Mz pulih 63% | Mxy sisa 37%",
        type: "t1t2_step2"
      },
      {
        subJudul: "Tahap 3: Pemulihan Penuh Menuju Setimbang (t > 5 x T1)",
        desc: "Setelah interval TR yang cukup panjang (> 5x T1), magnetisasi longitudinal Mz kembali pulih mendekati 100%, siap untuk pulsa eksitasi siklus berikutnya.",
        badge: "Pemulihan Total",
        overlay: "Mz ≈ 100% | Mxy ≈ 0",
        type: "t1t2_step3"
      }
    ]
  },
  {
    topik: "Trajektori Pengisian Ruang-k (k-Space)",
    totalSteps: 4,
    steps: [
      {
        subJudul: "Langkah 1: Matriks Kosong & Gradien Pengodean Fase",
        desc: "Ruang-k adalah matriks data mentah berukuran Nx x Ny. Setiap siklus TR dengan pulsa gradien Gy akan menentukan nomor baris ky yang akan diisi.",
        badge: "Inisialisasi Ruang-k",
        overlay: "Gradien Gy Aktif (Baris ky)",
        type: "kspace_step1"
      },
      {
        subJudul: "Langkah 2: Pengisian Baris Tepi (Resolusi & Frekuensi Tinggi)",
        desc: "Garis di bagian tepi matriks (ky tinggi) menyimpan informasi frekuensi spasial tinggi yang menentukan ketajaman batas dan resolusi anatomi.",
        badge: "Tepi Matriks (Detail)",
        overlay: "Sampling Garis Tepi",
        type: "kspace_step2"
      },
      {
        subJudul: "Langkah 3: Pengisian Garis Pusat (Kontras & Amplitudo Sinyal)",
        desc: "Ketika gradien mendekati pusat (ky = 0), amplitudo sinyal echo mencapai nilai maksimum. Wilayah pusat inilah yang menentukan kontras jaringan dan SNR citra.",
        badge: "Pusat Matriks (Kontras)",
        overlay: "Sampling Pusat Matriks (Echo Puncak)",
        type: "kspace_step3"
      },
      {
        subJudul: "Langkah 4: Rekonstruksi Citra Akhir Melalui 2D FFT",
        desc: "Setelah seluruh baris ruang-k terisi penuh, algoritma 2D Fast Fourier Transform (FFT) mengonversi domain frekuensi menjadi citra anatomi visual siap diagnostik.",
        badge: "Rekonstruksi FFT",
        overlay: "Citra Rekonstruksi 2D Selesai",
        type: "kspace_step4"
      }
    ]
  }
];

function pilihAnimasiTopik(idx) {
  animTopicIndex = idx;
  animStep = 0;
  animIsPlaying = false;
  if (animTimer) clearInterval(animTimer);

  [0, 1, 2].forEach(i => {
    const btn = document.getElementById(`btn-anim-${i}`);
    if (btn) {
      if (i === idx) {
        btn.className = "flex-1 py-1.5 px-2.5 text-xs font-semibold rounded-lg bg-cyan-600 text-white whitespace-nowrap";
      } else {
        btn.className = "flex-1 py-1.5 px-2.5 text-xs font-semibold rounded-lg text-slate-400 hover:text-white whitespace-nowrap";
      }
    }
  });

  const btnPlay = document.getElementById('btn-toggle-putar-anim');
  if (btnPlay) btnPlay.innerHTML = `<i class="fa-solid fa-play"></i> Putar`;

  renderAnimasiUI();
}

function togglePutarAnimasi() {
  animIsPlaying = !animIsPlaying;
  const btn = document.getElementById('btn-toggle-putar-anim');

  if (animIsPlaying) {
    btn.innerHTML = `<i class="fa-solid fa-pause"></i> Jeda`;
    btn.className = "flex-1 py-2 bg-amber-600 hover:bg-amber-500 text-white rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 shadow";

    if (animTimer) clearInterval(animTimer);
    animTimer = setInterval(() => {
      const topData = ANIM_DATA[animTopicIndex];
      animStep = (animStep + 1) % topData.totalSteps;
      renderAnimasiUI();
    }, 2400);
  } else {
    btn.innerHTML = `<i class="fa-solid fa-play"></i> Putar`;
    btn.className = "flex-1 py-2 bg-cyan-600 hover:bg-cyan-500 text-white rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 shadow";
    if (animTimer) clearInterval(animTimer);
  }
}

function langkahBerikutnyaAnim() {
  const topData = ANIM_DATA[animTopicIndex];
  animStep = (animStep + 1) % topData.totalSteps;
  renderAnimasiUI();
}

function langkahSebelumnyaAnim() {
  const topData = ANIM_DATA[animTopicIndex];
  animStep = (animStep - 1 + topData.totalSteps) % topData.totalSteps;
  renderAnimasiUI();
}

function resetAnimasi() {
  animStep = 0;
  if (animIsPlaying) togglePutarAnimasi();
  renderAnimasiUI();
}

function renderAnimasiUI() {
  const topData = ANIM_DATA[animTopicIndex];
  const stepData = topData.steps[animStep];

  document.getElementById('anim-topic-title').innerText = topData.topik;
  document.getElementById('anim-step-badge').innerText = `Langkah ${animStep + 1} dari ${topData.totalSteps}`;
  document.getElementById('anim-step-headline').innerText = stepData.subJudul;
  document.getElementById('anim-step-desc').innerText = stepData.desc;
  document.getElementById('anim-canvas-overlay').innerText = stepData.overlay;

  // Step Indicators
  const indContainer = document.getElementById('anim-step-indicators');
  indContainer.innerHTML = "";
  for (let s = 0; s < topData.totalSteps; s++) {
    const bar = document.createElement('div');
    bar.className = `h-1 rounded-full transition-all duration-300 ${s === animStep ? 'bg-cyan-400' : 'bg-slate-800'}`;
    indContainer.appendChild(bar);
  }

  renderAnimasiCanvas();
}

function renderAnimasiCanvas() {
  const canvas = document.getElementById('canvas-animasi-belajar');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;
  ctx.clearRect(0, 0, w, h);

  const topData = ANIM_DATA[animTopicIndex];
  const stepData = topData.steps[animStep];

  if (animTopicIndex === 0) {
    // 1. Sekuens Spin Echo Visualization
    const cx = w / 2;
    const cy = h / 2 + 15;

    // Sumbu Koordinat 3D Proyeksi
    ctx.strokeStyle = '#334155';
    ctx.lineWidth = 1.5;
    // Z-axis (Vertikal)
    ctx.beginPath();
    ctx.moveTo(cx, cy);
    ctx.lineTo(cx, cy - 80);
    ctx.stroke();

    // X-axis (Diagonal Kiri)
    ctx.beginPath();
    ctx.moveTo(cx, cy);
    ctx.lineTo(cx - 70, cy + 35);
    ctx.stroke();

    // Y-axis (Kanan)
    ctx.beginPath();
    ctx.moveTo(cx, cy);
    ctx.lineTo(cx + 85, cy + 10);
    ctx.stroke();

    ctx.fillStyle = '#64748b';
    ctx.font = '10px monospace';
    ctx.fillText('Z (B0)', cx + 5, cy - 75);
    ctx.fillText('X', cx - 80, cy + 40);
    ctx.fillText('Y', cx + 90, cy + 15);

    if (stepData.type === 'se_step1') {
      // M0 along Z
      ctx.strokeStyle = '#06b6d4';
      ctx.lineWidth = 4;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.lineTo(cx, cy - 70);
      ctx.stroke();
      // Arrow head
      ctx.fillStyle = '#06b6d4';
      ctx.beginPath();
      ctx.moveTo(cx, cy - 75);
      ctx.lineTo(cx - 5, cy - 65);
      ctx.lineTo(cx + 5, cy - 65);
      ctx.fill();

      ctx.fillStyle = '#22d3ee';
      ctx.font = 'bold 11px sans-serif';
      ctx.fillText('Vektor Netto M0', cx + 10, cy - 40);
    } else if (stepData.type === 'se_step2') {
      // Nutasi 90 to Y
      ctx.strokeStyle = '#10b981';
      ctx.lineWidth = 4;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.lineTo(cx + 70, cy + 8);
      ctx.stroke();

      ctx.fillStyle = '#10b981';
      ctx.beginPath();
      ctx.moveTo(cx + 75, cy + 8);
      ctx.lineTo(cx + 65, cy + 3);
      ctx.lineTo(cx + 65, cy + 13);
      ctx.fill();

      ctx.fillStyle = '#34d399';
      ctx.font = 'bold 11px sans-serif';
      ctx.fillText('Pulsa RF 90° (Koheren)', cx + 15, cy - 10);
    } else if (stepData.type === 'se_step3') {
      // Dephasing spins in XY plane
      const angles = [-0.4, -0.15, 0.1, 0.35];
      const colors = ['#f43f5e', '#fb923c', '#facc15', '#a3e635'];
      angles.forEach((ang, idx) => {
        const len = 55;
        const tx = cx + Math.cos(ang) * len;
        const ty = cy + Math.sin(ang) * len * 0.4 + 10;
        ctx.strokeStyle = colors[idx];
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(tx, ty);
        ctx.stroke();
      });

      ctx.fillStyle = '#f87171';
      ctx.font = 'bold 11px sans-serif';
      ctx.fillText('Dephasing T2* (Kecepatan Berbeda)', cx - 40, cy - 35);
    } else if (stepData.type === 'se_step4') {
      // Refocused Spin Echo
      ctx.strokeStyle = '#f59e0b';
      ctx.lineWidth = 4;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.lineTo(cx + 72, cy + 8);
      ctx.stroke();

      ctx.fillStyle = '#f59e0b';
      ctx.beginPath();
      ctx.moveTo(cx + 78, cy + 8);
      ctx.lineTo(cx + 68, cy + 3);
      ctx.lineTo(cx + 68, cy + 13);
      ctx.fill();

      // Echo waveform below
      ctx.strokeStyle = '#22d3ee';
      ctx.lineWidth = 2;
      ctx.beginPath();
      for (let x = 30; x < 150; x++) {
        const env = Math.exp(-Math.pow((x - 90) / 18, 2));
        const y = cy - 45 - Math.sin((x - 90) * 0.45) * 18 * env;
        if (x === 30) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();

      ctx.fillStyle = '#fde047';
      ctx.font = 'bold 11px sans-serif';
      ctx.fillText('Refokusing 180° -> Echo', cx + 15, cy - 10);
    }

  } else if (animTopicIndex === 1) {
    // 2. Relaksasi T1 & T2 Curves
    const ox = 40;
    const oy = h - 35;
    const cw = w - 70;
    const ch = h - 65;

    // Axes
    ctx.strokeStyle = '#475569';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(ox, oy - ch);
    ctx.lineTo(ox, oy);
    ctx.lineTo(ox + cw, oy);
    ctx.stroke();

    ctx.fillStyle = '#94a3b8';
    ctx.font = '10px monospace';
    ctx.fillText('Waktu (t)', ox + cw - 25, oy + 18);
    ctx.fillText('Sinyal', ox - 35, oy - ch + 5);

    // T1 Curve (Longitudinal Recovery)
    ctx.strokeStyle = '#06b6d4';
    ctx.lineWidth = 2.5;
    ctx.beginPath();
    for (let x = 0; x <= cw; x += 3) {
      const norm = x / (cw * 0.35);
      const val = 1 - Math.exp(-norm);
      const px = ox + x;
      const py = oy - val * (ch * 0.85);
      if (x === 0) ctx.moveTo(px, py);
      else ctx.lineTo(px, py);
    }
    ctx.stroke();

    // T2 Curve (Transverse Decay)
    ctx.strokeStyle = '#f43f5e';
    ctx.lineWidth = 2.5;
    ctx.beginPath();
    for (let x = 0; x <= cw; x += 3) {
      const norm = x / (cw * 0.22);
      const val = Math.exp(-norm);
      const px = ox + x;
      const py = oy - val * (ch * 0.85);
      if (x === 0) ctx.moveTo(px, py);
      else ctx.lineTo(px, py);
    }
    ctx.stroke();

    // Legend
    ctx.fillStyle = '#22d3ee';
    ctx.fillText('● T1 Pemulihan (63%)', ox + 15, oy - ch + 15);
    ctx.fillStyle = '#fb7185';
    ctx.fillText('● T2 Peluruhan (37%)', ox + 140, oy - ch + 15);

    if (animStep >= 1) {
      // Mark T1 63% and T2 37%
      const t1x = ox + cw * 0.35;
      const t1y = oy - 0.63 * (ch * 0.85);
      ctx.fillStyle = '#22d3ee';
      ctx.beginPath();
      ctx.arc(t1x, t1y, 4, 0, Math.PI * 2);
      ctx.fill();

      const t2x = ox + cw * 0.22;
      const t2y = oy - 0.37 * (ch * 0.85);
      ctx.fillStyle = '#fb7185';
      ctx.beginPath();
      ctx.arc(t2x, t2y, 4, 0, Math.PI * 2);
      ctx.fill();
    }

  } else {
    // 3. Ruang-k Trajectory Canvas
    const mx = w / 2;
    const my = h / 2;
    const ksize = 90;

    // Box
    ctx.strokeStyle = '#334155';
    ctx.lineWidth = 1;
    ctx.strokeRect(mx - ksize, my - ksize / 2, ksize * 2, ksize);

    // Grid lines
    ctx.strokeStyle = '#1e293b';
    for (let y = -3; y <= 3; y++) {
      ctx.beginPath();
      ctx.moveTo(mx - ksize, my + y * 12);
      ctx.lineTo(mx + ksize, my + y * 12);
      ctx.stroke();
    }

    if (animStep === 0) {
      // Empty with scanning line indicator
      ctx.fillStyle = '#38bdf8';
      ctx.font = '10px monospace';
      ctx.fillText('Gradien Fase Gy disiapkan...', mx - 75, my - 55);
    } else if (animStep === 1) {
      // Edge lines sampled
      ctx.strokeStyle = '#38bdf8';
      ctx.lineWidth = 2;
      [-36, -24, 24, 36].forEach(offset => {
        ctx.beginPath();
        ctx.moveTo(mx - ksize + 5, my + offset);
        ctx.lineTo(mx + ksize - 5, my + offset);
        ctx.stroke();
      });
      ctx.fillStyle = '#38bdf8';
      ctx.font = '10px sans-serif';
      ctx.fillText('Baris Frekuensi Tinggi (Detail)', mx - 65, my - 55);
    } else if (animStep === 2) {
      // Center lines sampled bright
      ctx.fillStyle = 'rgba(6, 182, 212, 0.25)';
      ctx.fillRect(mx - ksize, my - 15, ksize * 2, 30);

      ctx.strokeStyle = '#22d3ee';
      ctx.lineWidth = 2.5;
      [-10, 0, 10].forEach(offset => {
        ctx.beginPath();
        ctx.moveTo(mx - ksize + 5, my + offset);
        ctx.lineTo(mx + ksize - 5, my + offset);
        ctx.stroke();
      });
      ctx.fillStyle = '#22d3ee';
      ctx.font = '10px sans-serif';
      ctx.fillText('Pusat Ruang-k: Kontras & SNR', mx - 65, my - 55);
    } else {
      // Full fill + FFT result icon
      ctx.fillStyle = 'rgba(16, 185, 129, 0.2)';
      ctx.fillRect(mx - ksize, my - ksize / 2, ksize * 2, ksize);

      ctx.fillStyle = '#34d399';
      ctx.font = 'bold 11px sans-serif';
      ctx.fillText('2D FFT Rekonstruksi Selesai! ✓', mx - 80, my - 55);
    }
  }
}

/* ============================================================
   5. CATATAN SAYA (NOTES CRUD ENGINE & BLANK EDITOR)
============================================================ */
function renderDaftarCatatan() {
  const container = document.getElementById('daftar-catatan-web');
  if (!container) return;
  container.innerHTML = "";

  const q = (document.getElementById('cari-catatan-web')?.value || "").toLowerCase();
  const list = CATATAN_LIST.filter(c => {
    const matchCat = filterKategoriCatatan === "Semua" || c.kategori === filterKategoriCatatan;
    const matchQuery = c.judul.toLowerCase().includes(q) || c.isi.toLowerCase().includes(q);
    return matchCat && matchQuery;
  });

  if (list.length === 0) {
    container.innerHTML = `
      <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 text-center">
        <i class="fa-solid fa-note-sticky text-3xl text-slate-600 mb-2"></i>
        <h4 class="text-xs font-bold text-slate-300">Belum Ada Catatan</h4>
        <p class="text-[11px] text-slate-500 mt-1 mb-3">Klik tombol "Tambah Catatan" di atas untuk menulis catatan kosong.</p>
        <button onclick="bukaEditorCatatanBaru()" class="px-3.5 py-1.5 bg-teal-600 text-white text-xs font-bold rounded-xl shadow">
          + Buat Catatan Kosong
        </button>
      </div>
    `;
    return;
  }

  list.forEach(c => {
    const safeId = escapeHtml(c.id);
    const card = document.createElement('div');
    card.className = "bg-slate-900 border border-slate-800 rounded-2xl p-3.5 hover:border-slate-700 transition";
    card.innerHTML = `
      <div class="flex items-start justify-between gap-2 mb-2">
        <div>
          <span class="text-[9px] font-mono px-2 py-0.5 rounded bg-teal-950 border border-teal-800 text-teal-300 font-semibold uppercase">${escapeHtml(c.kategori)}</span>
          <h4 class="text-xs font-bold text-white mt-1.5">${escapeHtml(c.judul)}</h4>
        </div>
        <div class="flex items-center gap-1">
          <button onclick="editCatatan('${safeId}')" class="w-7 h-7 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 flex items-center justify-center text-xs" title="Edit Catatan">
            <i class="fa-solid fa-pen"></i>
          </button>
          <button onclick="hapusCatatan('${safeId}')" class="w-7 h-7 rounded-lg bg-slate-800 hover:bg-rose-900/60 text-slate-400 hover:text-rose-300 flex items-center justify-center text-xs" title="Hapus Catatan">
            <i class="fa-solid fa-trash"></i>
          </button>
        </div>
      </div>
      <p class="text-xs text-slate-300 whitespace-pre-line leading-relaxed mb-2 bg-[#090d16] p-2.5 rounded-xl border border-slate-800/80 font-sans">${escapeHtml(c.isi)}</p>
      <div class="flex items-center justify-between text-[10px] text-slate-500">
        <span>${escapeHtml(c.tanggal)}</span>
        <span class="text-teal-400/80 font-mono">Tersimpan</span>
      </div>
    `;
    container.appendChild(card);
  });
}

function filterCatatan(kat) {
  filterKategoriCatatan = kat;
  const categories = ['Semua', 'Fisika Dasar', 'Sekuens Klinis', 'Protokol Scanner', 'Safety MR'];
  categories.forEach(k => {
    const idMap = {
      'Semua': 'btn-cat-semua',
      'Fisika Dasar': 'btn-cat-fisika',
      'Sekuens Klinis': 'btn-cat-sekuens',
      'Protokol Scanner': 'btn-cat-protokol',
      'Safety MR': 'btn-cat-safety'
    };
    const b = document.getElementById(idMap[k]);
    if (b) {
      if (k === kat) {
        b.className = "px-3 py-1 rounded-lg text-xs font-semibold bg-teal-600 text-white whitespace-nowrap";
      } else {
        b.className = "px-3 py-1 rounded-lg text-xs font-semibold bg-slate-800 text-slate-300 hover:text-white whitespace-nowrap";
      }
    }
  });
  renderDaftarCatatan();
}

function saringCatatanWeb() {
  renderDaftarCatatan();
}

function bukaEditorCatatanBaru() {
  document.getElementById('input-catatan-id').value = "";
  document.getElementById('input-catatan-judul').value = "";
  document.getElementById('input-catatan-kategori').value = "Fisika Dasar";
  document.getElementById('input-catatan-isi').value = ""; // Tempat Catatan Kosong!
  document.getElementById('modal-catatan-judul-header').innerHTML = `<i class="fa-solid fa-pen-to-square text-teal-400"></i> Buat Catatan Baru`;
  document.getElementById('modal-editor-catatan').classList.remove('hidden');
}

function editCatatan(id) {
  const item = CATATAN_LIST.find(c => c.id === id);
  if (!item) return;
  document.getElementById('input-catatan-id').value = item.id;
  document.getElementById('input-catatan-judul').value = item.judul;
  document.getElementById('input-catatan-kategori').value = item.kategori;
  document.getElementById('input-catatan-isi').value = item.isi;
  document.getElementById('modal-catatan-judul-header').innerHTML = `<i class="fa-solid fa-pen-to-square text-teal-400"></i> Edit Catatan`;
  document.getElementById('modal-editor-catatan').classList.remove('hidden');
}

function tutupEditorCatatan() {
  document.getElementById('modal-editor-catatan').classList.add('hidden');
}

function simpanCatatanWeb() {
  const id = document.getElementById('input-catatan-id').value;
  const judul = document.getElementById('input-catatan-judul').value.trim();
  const kategori = document.getElementById('input-catatan-kategori').value;
  const isi = document.getElementById('input-catatan-isi').value.trim();

  if (!judul || !isi) {
    alert("Harap isi judul dan konten catatan Anda.");
    return;
  }

  if (id) {
    const idx = CATATAN_LIST.findIndex(c => c.id === id);
    if (idx !== -1) {
      CATATAN_LIST[idx].judul = judul;
      CATATAN_LIST[idx].kategori = kategori;
      CATATAN_LIST[idx].isi = isi;
      CATATAN_LIST[idx].tanggal = "Diedit baru saja";
    }
  } else {
    const newNote = {
      id: idCatatanBaru(),
      judul: judul,
      kategori: kategori,
      isi: isi,
      tanggal: "Baru saja"
    };
    CATATAN_LIST.unshift(newNote);
  }

  simpanCatatanKeStorage();
  tutupEditorCatatan();
  renderDaftarCatatan();
}

function hapusCatatan(id) {
  if (confirm("Hapus catatan ini?")) {
    CATATAN_LIST = CATATAN_LIST.filter(c => c.id !== id);
    simpanCatatanKeStorage();
    renderDaftarCatatan();
  }
}

/* ============================================================
   6. GLOSARIUM / KAMUS MEDIS ENGINE & DETAIL MODAL
============================================================ */
function renderKamusWeb(list) {
  const container = document.getElementById('daftar-kamus-web');
  if (!container) return;
  container.innerHTML = "";

  if (list.length === 0) {
    container.innerHTML = `<p class="text-xs text-slate-400 text-center py-6">Istilah glosarium tidak ditemukan.</p>`;
    return;
  }

  list.forEach(k => {
    const item = document.createElement('div');
    item.onclick = () => bukaDetailGlosarium(k);
    item.className = "bg-slate-900 border border-slate-800 hover:border-cyan-500/60 rounded-2xl p-3.5 cursor-pointer transition active:scale-[0.99]";
    item.innerHTML = `
      <div class="flex items-center justify-between mb-1">
        <h4 class="text-xs font-bold text-white flex items-center gap-1.5">
          ${k.istilah}
          <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-slate-800 text-cyan-300">${k.lambang}</span>
        </h4>
        <span class="text-[10px] text-cyan-400 font-mono bg-cyan-950 px-2 py-0.5 rounded border border-cyan-800">${k.mod}</span>
      </div>
      <p class="text-xs text-slate-300 leading-relaxed line-clamp-2">${k.arti}</p>
      <div class="mt-2 text-[10px] text-teal-400 font-medium flex items-center gap-1">
        <i class="fa-solid fa-stethoscope"></i> Klik untuk melihat korelasi klinis
      </div>
    `;
    container.appendChild(item);
  });
}

function saringKamusWeb() {
  const q = document.getElementById('cari-kamus-web').value.toLowerCase();
  const res = KAMUS_DATA.filter(k => 
    k.istilah.toLowerCase().includes(q) || 
    k.lambang.toLowerCase().includes(q) || 
    k.arti.toLowerCase().includes(q)
  );
  renderKamusWeb(res);
}

function bukaDetailGlosarium(item) {
  document.getElementById('glo-detail-mod').innerText = item.mod;
  document.getElementById('glo-detail-term').innerText = `${item.istilah} (${item.lambang})`;
  document.getElementById('glo-detail-arti').innerText = item.arti;
  document.getElementById('glo-detail-klinis').innerText = item.klinis || "Penting dalam optimasi rasio sinyal kontras jaringan.";
  document.getElementById('modal-glosarium-detail').classList.remove('hidden');
}

function tutupModalGlosarium() {
  document.getElementById('modal-glosarium-detail').classList.add('hidden');
}

/* ============================================================
   7. BERANDA & MODUL KURIKULUM MEDIS
============================================================ */
function renderBeranda() {
  const container = document.getElementById('daftar-modul-beranda');
  if (!container) return;
  container.innerHTML = "";

  DAFTAR_MODUL.forEach(m => {
    const card = document.createElement('div');
    card.onclick = () => {
      pindahTab('materi');
      bukaModul(m.id);
    };
    card.className = "bg-slate-900 border border-slate-800 hover:border-cyan-500/60 rounded-2xl p-3.5 flex items-center justify-between cursor-pointer transition active:scale-[0.99]";
    card.innerHTML = `
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-cyan-950 border border-cyan-800 flex items-center justify-center font-mono font-bold text-xs text-cyan-400">
          ${m.kode}
        </div>
        <div>
          <h4 class="text-xs font-bold text-white">${m.judul}</h4>
        </div>
      </div>
      <div class="text-slate-500 text-xs">
        <i class="fa-solid fa-chevron-right"></i>
      </div>
    `;
    container.appendChild(card);
  });
}

function bukaModul(modId) {
  modulAktifId = modId;
  const mod = DAFTAR_MODUL.find(m => m.id === modId);
  if (!mod) return;

  const header = document.getElementById('header-modul-detail');
  header.innerHTML = `
    <span class="text-[10px] font-mono font-bold text-cyan-400 bg-cyan-950 border border-cyan-800 px-2 py-0.5 rounded">${mod.kode}</span>
    <h2 class="text-base font-bold text-white mt-1.5">${mod.judul}</h2>
    <p class="text-xs text-slate-400 mt-0.5">${mod.sub}</p>
  `;

  renderSimpulMateri();
}

function filterTingkat(t) {
  filterTingkatModul = t;
  [0, 1, 2, 3].forEach(i => {
    const b = document.getElementById(`f-tingkat-${i}`);
    if (b) {
      if (i === t) {
        b.className = "flex-1 py-1 text-xs font-medium rounded-lg bg-cyan-600 text-white";
      } else {
        b.className = "flex-1 py-1 text-xs font-medium rounded-lg text-slate-400";
      }
    }
  });
  renderSimpulMateri();
}

function renderSimpulMateri() {
  const mod = DAFTAR_MODUL.find(m => m.id === modulAktifId);
  if (!mod) return;

  const wadah = document.getElementById('wadah-simpul-materi');
  wadah.innerHTML = "";

  const filtered = mod.simpul.filter(s => filterTingkatModul === 0 || s.tingkat === filterTingkatModul);

  filtered.forEach(s => {
    const card = document.createElement('div');
    card.className = "bg-slate-900 border border-slate-800 rounded-2xl p-4";

    let isiTeks = s.singkat;
    if (modeBelajarModul === 'lengkap') isiTeks = s.lengkap;
    else if (modeBelajarModul === 'analogi') isiTeks = `💡 Analogi: ${s.analogi}`;

    card.innerHTML = `
      <div class="flex items-center justify-between mb-2">
        <span class="text-[10px] font-mono text-cyan-400 bg-cyan-950 px-2 py-0.5 rounded border border-cyan-800">${s.kode}</span>
        <div class="flex gap-1 text-[10px] bg-slate-950 p-0.5 rounded-lg border border-slate-800">
          <button onclick="setModeMateri('singkat')" class="px-2 py-0.5 rounded ${modeBelajarModul === 'singkat' ? 'bg-cyan-600 text-white' : 'text-slate-400'}">Singkat</button>
          <button onclick="setModeMateri('lengkap')" class="px-2 py-0.5 rounded ${modeBelajarModul === 'lengkap' ? 'bg-cyan-600 text-white' : 'text-slate-400'}">Lengkap</button>
          <button onclick="setModeMateri('analogi')" class="px-2 py-0.5 rounded ${modeBelajarModul === 'analogi' ? 'bg-cyan-600 text-white' : 'text-slate-400'}">Analogi</button>
        </div>
      </div>
      <h3 class="text-xs font-bold text-white mb-1.5">${s.judul}</h3>
      <p class="text-xs text-slate-300 leading-relaxed">${isiTeks}</p>
    `;
    wadah.appendChild(card);
  });
}

function setModeMateri(mode) {
  modeBelajarModul = mode;
  renderSimpulMateri();
}

/* ============================================================
   8. SIMULATOR INTERAKTIF
============================================================ */
function bukaSimulasi(jenis) {
  pindahTab('simulasi');
  gantiSubSimulasi(jenis);
}

function gantiSubSimulasi(jenis) {
  document.querySelectorAll('.subsim-panel').forEach(p => p.classList.add('hidden'));
  const target = document.getElementById(`subsim-${jenis}`);
  if (target) target.classList.remove('hidden');

  ['spin', 'kontras', 'ruangk', 'sar'].forEach(k => {
    const b = document.getElementById(`btn-subsim-${k}`);
    if (b) {
      if (k === jenis) {
        b.className = "px-3 py-1.5 rounded-xl bg-cyan-600 text-white text-xs font-semibold whitespace-nowrap";
      } else {
        b.className = "px-3 py-1.5 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold whitespace-nowrap";
      }
    }
  });

  if (jenis === 'spin') inisialisasiCanvasPresesi();
  else if (jenis === 'kontras') { renderChipsJaringan(); hitungKontrasWeb(); }
  else if (jenis === 'ruangk') inisialisasiCanvasKSpace();
  else if (jenis === 'sar') hitungSarWeb();
}

function inisialisasiCanvasPresesi() {
  const canvas = document.getElementById('canvas-presesi');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  let angle = 0;

  function render() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const cx = canvas.width / 2;
    const cy = canvas.height / 2 + 10;

    // Coordinate lines
    ctx.strokeStyle = '#334155';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(cx, cy - 60);
    ctx.lineTo(cx, cy + 60);
    ctx.moveTo(cx - 70, cy);
    ctx.lineTo(cx + 70, cy);
    ctx.stroke();

    // Precession cone
    ctx.strokeStyle = 'rgba(6, 182, 212, 0.2)';
    ctx.beginPath();
    ctx.ellipse(cx, cy - 40, 40, 12, 0, 0, Math.PI * 2);
    ctx.stroke();

    // Proton vector
    const speed = (b0Terpilih * 0.04);
    angle += speed;

    const tipX = cx + Math.cos(angle) * 40;
    const tipY = cy - 40 + Math.sin(angle) * 12;

    ctx.strokeStyle = pulsaRfAktif ? '#10b981' : '#06b6d4';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(cx, cy);
    ctx.lineTo(tipX, tipY);
    ctx.stroke();

    // Arrow tip
    ctx.fillStyle = pulsaRfAktif ? '#34d399' : '#22d3ee';
    ctx.beginPath();
    ctx.arc(tipX, tipY, 4.5, 0, Math.PI * 2);
    ctx.fill();

    presesiAnimId = requestAnimationFrame(render);
  }

  if (presesiAnimId) cancelAnimationFrame(presesiAnimId);
  render();
}

function pilihB0(b0) {
  b0Terpilih = b0;
  document.querySelectorAll('.btn-b0').forEach(b => {
    b.className = "btn-b0 py-1.5 rounded-lg bg-slate-800 text-slate-300 text-xs font-bold";
  });
  event.target.className = "btn-b0 py-1.5 rounded-lg bg-cyan-600 text-white text-xs font-bold";
  updateLarmorUI();
}

function pilihInti(nama, gamma) {
  intiTerpilih = nama;
  gammaTerpilih = gamma;
  document.querySelectorAll('.btn-inti').forEach(b => {
    b.className = "btn-inti p-2 rounded-lg bg-slate-800 border border-slate-700 text-slate-300 text-xs font-bold text-left";
  });
  event.currentTarget.className = "btn-inti p-2 rounded-lg bg-cyan-950 border border-cyan-700 text-cyan-300 text-xs font-bold text-left";
  updateLarmorUI();
}

function updateLarmorUI() {
  const f0 = (gammaTerpilih * b0Terpilih).toFixed(2);
  document.getElementById('badge-larmor-freq').innerText = `${f0} MHz`;
  document.getElementById('label-canvas-b0').innerText = `B0: ${b0Terpilih} T`;
  document.getElementById('label-canvas-nucleus').innerText = `${intiTerpilih} (${gammaTerpilih} MHz/T)`;
}

function togglePulsaRF() {
  pulsaRfAktif = !pulsaRfAktif;
  const btn = document.getElementById('btn-pulsa-rf');
  if (pulsaRfAktif) {
    btn.className = "w-full py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold flex items-center justify-center gap-2";
    btn.innerHTML = `<i class="fa-solid fa-check"></i> RF Aktif (Nutasi 90° ke Transversal XY)`;
  } else {
    btn.className = "w-full py-2.5 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-bold flex items-center justify-center gap-2";
    btn.innerHTML = `<i class="fa-solid fa-bolt"></i> Kirim Pulsa RF Eksitasi 90° (Nutasi ke XY)`;
  }
}

function renderChipsJaringan() {
  const container = document.getElementById('wadah-chips-jaringan');
  if (!container) return;
  container.innerHTML = "";

  Object.keys(KONSTANTA.jaringan).forEach(k => {
    const j = KONSTANTA.jaringan[k];
    const isSel = k === jaringanTerpilih;
    const btn = document.createElement('button');
    btn.className = `p-2 rounded-xl text-left border transition text-xs ${isSel ? 'bg-cyan-950 border-cyan-500 text-cyan-200' : 'bg-slate-800 border-slate-700 text-slate-300'}`;
    btn.onclick = () => {
      jaringanTerpilih = k;
      renderChipsJaringan();
      hitungKontrasWeb();
    };
    btn.innerHTML = `
      <span class="font-bold block text-[11px] truncate">${j.nama.split(' ')[0]}</span>
      <span class="text-[9px] text-slate-400">T1: ${j.t1}ms</span>
    `;
    container.appendChild(btn);
  });
}

function hitungKontrasWeb() {
  const j = KONSTANTA.jaringan[jaringanTerpilih];
  const trInput = document.getElementById('input-sim-tr');
  const teInput = document.getElementById('input-sim-te');
  if (!trInput || !teInput) return;

  const tr = parseInt(trInput.value, 10);
  const te = parseInt(teInput.value, 10);

  document.getElementById('label-sim-tr').innerText = `${tr} ms`;
  document.getElementById('label-sim-te').innerText = `${te} ms`;

  document.getElementById('detail-jaringan-box').innerHTML = `
    <h4 class="font-bold text-white text-xs">${j.nama}</h4>
    <p class="text-slate-300 my-1">${j.desc}</p>
    <div class="text-[10px] text-cyan-400 font-mono">T1: ${j.t1}ms | T2: ${j.t2}ms | PD: ${j.pd}</div>
  `;

  const s = j.pd * (1 - Math.exp(-tr / j.t1)) * Math.exp(-te / j.t2);
  const grayVal = Math.min(255, Math.max(10, Math.round(s * 270)));

  const circle = document.getElementById('preview-lingkaran-sinyal');
  circle.style.backgroundColor = `rgb(${grayVal}, ${grayVal}, ${grayVal})`;
  circle.style.color = grayVal > 140 ? '#090d16' : '#ffffff';
  circle.innerText = `${Math.round(s * 100)}%`;

  document.getElementById('preview-skor-sinyal').innerText = s.toFixed(3);

  let bobot = "PDw (Proton Density)";
  if (tr < 800 && te < 30) bobot = "T1-Weighted (T1w)";
  else if (tr > 1800 && te > 70) bobot = "T2-Weighted (T2w)";
  document.getElementById('badge-bobot-kontras').innerText = bobot;

  let kat = "Isointens (Intermediet)";
  if (grayVal > 180) kat = "Hiperintens (Sangat Terang)";
  else if (grayVal < 80) kat = "Hipointens (Gelap)";
  document.getElementById('preview-kategori-sinyal').innerText = kat;
}

function inisialisasiCanvasKSpace() {
  const canvas = document.getElementById('canvas-kspace');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  const cx = canvas.width / 2;
  const cy = canvas.height / 2;
  const size = 100;

  // Grid
  ctx.strokeStyle = '#1e293b';
  ctx.lineWidth = 1;
  for (let i = -4; i <= 4; i++) {
    const y = cy + i * (size / 8);
    ctx.beginPath();
    ctx.moveTo(cx - size / 2, y);
    ctx.lineTo(cx + size / 2, y);
    ctx.stroke();
  }

  // High amplitude center
  if (kspaceFilter !== "edge") {
    const grad = ctx.createRadialGradient(cx, cy, 2, cx, cy, 32);
    grad.addColorStop(0, '#22d3ee');
    grad.addColorStop(0.6, 'rgba(6, 182, 212, 0.4)');
    grad.addColorStop(1, 'transparent');
    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.arc(cx, cy, 32, 0, Math.PI * 2);
    ctx.fill();
  }

  // Cross axes
  ctx.strokeStyle = '#475569';
  ctx.lineWidth = 1.5;
  ctx.beginPath();
  ctx.moveTo(cx - size / 2, cy);
  ctx.lineTo(cx + size / 2, cy);
  ctx.moveTo(cx, cy - size / 2);
  ctx.lineTo(cx, cy + size / 2);
  ctx.stroke();
}

function setFilterKSpace(mode) {
  kspaceFilter = mode;
  ['full', 'center', 'edge'].forEach(m => {
    const b = document.getElementById(`kbtn-${m}`);
    if (b) {
      b.className = m === mode 
        ? "py-1.5 rounded-lg bg-cyan-600 text-white text-xs font-bold"
        : "py-1.5 rounded-lg bg-slate-800 text-slate-300 text-xs font-bold";
    }
  });

  const desc = document.getElementById('kspace-expl-text');
  if (mode === 'center') {
    desc.innerText = "Mask Pusat: Hanya frekuensi spasial rendah. Citra mengalami pengaburan (blur), namun kontras jaringan tetap utuh.";
  } else if (mode === 'edge') {
    desc.innerText = "Mask Tepi: Hanya frekuensi spasial tinggi. Kontras jaringan hilang dan tampak seperti deteksi kontur garis pensil.";
  } else {
    desc.innerText = "Full k-Space: Rekonstruksi FFT optimal dengan kontras utuh dan batas anatomi tajam.";
  }
  inisialisasiCanvasKSpace();
}

function hitungSarWeb() {
  const wInput = document.getElementById('input-sar-weight');
  if (!wInput) return;
  const w = parseInt(wInput.value, 10);
  const isTse = document.getElementById('check-sar-tse')?.checked ?? true;
  document.getElementById('label-sar-weight').innerText = `${w} kg`;

  const rfFactor = isTse ? 1.85 : 0.85;
  const sar = (0.95 * rfFactor * (70 / Math.max(30, w))).toFixed(2);
  document.getElementById('badge-sar-value').innerText = `${sar} W/kg`;

  const banner = document.getElementById('banner-sar-status');
  if (sar < 2.0) {
    banner.className = "bg-[#090d16] border border-emerald-800/80 p-3 rounded-xl mb-3 text-xs text-emerald-300";
    banner.innerHTML = "<strong>Mode Normal:</strong> Aman untuk pemeriksaan klinis rutin. Batas whole-body < 2.0 W/kg.";
  } else if (sar <= 4.0) {
    banner.className = "bg-[#090d16] border border-amber-800/80 p-3 rounded-xl mb-3 text-xs text-amber-300";
    banner.innerHTML = "<strong>First Level Controlled:</strong> Diperlukan pertimbangan radiolog dan monitoring ketat kondisi pasien.";
  } else {
    banner.className = "bg-[#090d16] border border-rose-800/80 p-3 rounded-xl mb-3 text-xs text-rose-300";
    banner.innerHTML = "<strong>Second Level:</strong> Melebihi batas aman pemanasan jaringan tubuh!";
  }
}

/* ============================================================
   9. MODAL INFO
============================================================ */
function bukaModalInfo() {
  document.getElementById('modal-info').classList.remove('hidden');
}
function tutupModalInfo() {
  document.getElementById('modal-info').classList.add('hidden');
}

/* ============================================================
   10. INITIALIZATION
============================================================ */
window.addEventListener('DOMContentLoaded', () => {
  muatCatatanDariStorage();
  renderBeranda();
  renderStreakModalUI();
  pilihAnimasiTopik(0);
  renderDaftarCatatan();
  renderKamusWeb(KAMUS_DATA);
});
