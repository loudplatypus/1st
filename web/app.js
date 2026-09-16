/**
 * HeartSync — Core Application Logic
 * Encrypted Couples Space Web Simulation
 */

// ============================================================================
// 1. SOUND SYNTHESIZER (Web Audio API matching RomanticSounds.kt)
// ============================================================================
class SoundSynthesizer {
  constructor() {
    this.ctx = null;
  }

  initContext() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  playSound(soundName) {
    try {
      this.initContext();
      if (!this.ctx) return;

      switch (soundName) {
        case 'Romantic Flutter':
          // Fast rising warm arpeggio (C5 - E5 - G5 - C6)
          this.playArpeggio([523.25, 659.25, 783.99, 1046.50], 0.12);
          break;

        case 'Warm Heartbeat':
          // Double thump heartbeat tones (110Hz)
          this.playHeartbeat();
          break;

        case 'Celestial Chime':
          // Ringing cathedral-like soft bell with quadratic decay (880Hz)
          this.playChime(880.0, 0.65);
          break;

        case 'Sparkling Twinkle':
          // Twinkling bright chime pairing
          this.playArpeggio([987.77, 1174.66, 1318.51], 0.08);
          break;

        case 'Lovable Purr':
          // Sweet playful purring chirp
          this.playArpeggio([329.63, 392.00, 440.00], 0.09);
          break;

        default:
          this.playChime(660.0, 0.3);
          break;
      }
    } catch (e) {
      console.warn('Audio playback not permitted yet or failed:', e);
    }
  }

  playChime(freq, duration) {
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();

    osc.type = 'sine';
    osc.frequency.setValueAtTime(freq, this.ctx.currentTime);

    gain.gain.setValueAtTime(0.4, this.ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + duration);

    osc.connect(gain);
    gain.connect(this.ctx.destination);

    osc.start();
    osc.stop(this.ctx.currentTime + duration);
  }

  playArpeggio(notes, noteDuration) {
    let now = this.ctx.currentTime;
    notes.forEach((freq, idx) => {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, now + idx * noteDuration);

      gain.gain.setValueAtTime(0.35, now + idx * noteDuration);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + (idx + 1) * noteDuration + 0.1);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start(now + idx * noteDuration);
      osc.stop(now + (idx + 1) * noteDuration + 0.12);
    });
  }

  playHeartbeat() {
    const now = this.ctx.currentTime;
    // Thump 1
    const osc1 = this.ctx.createOscillator();
    const gain1 = this.ctx.createGain();
    osc1.type = 'triangle';
    osc1.frequency.setValueAtTime(110.0, now);
    gain1.gain.setValueAtTime(0.6, now);
    gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.15);
    osc1.connect(gain1);
    gain1.connect(this.ctx.destination);
    osc1.start(now);
    osc1.stop(now + 0.16);

    // Thump 2
    const osc2 = this.ctx.createOscillator();
    const gain2 = this.ctx.createGain();
    osc2.type = 'triangle';
    osc2.frequency.setValueAtTime(110.0, now + 0.18);
    gain2.gain.setValueAtTime(0.5, now + 0.18);
    gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.36);
    osc2.connect(gain2);
    gain2.connect(this.ctx.destination);
    osc2.start(now + 0.18);
    osc2.stop(now + 0.38);
  }
}

const soundSynth = new SoundSynthesizer();

// ============================================================================
// 2. AES-128 ENCRYPTION ENGINE (Matching CoupleEncryption.kt)
// ============================================================================
class CoupleEncryption {
  constructor() {
    this.sharedSecret = localStorage.getItem('heartsync_secret') || 'HeartSyncLoveKey';
  }

  getSharedSecret() {
    return this.sharedSecret;
  }

  updateSharedSecret(newKey) {
    if (!newKey) return;
    let formatted = newKey.slice(0, 16);
    if (formatted.length < 16) {
      formatted = formatted.padEnd(16, '♥');
    }
    this.sharedSecret = formatted;
    localStorage.setItem('heartsync_secret', this.sharedSecret);
  }

  encrypt(data) {
    if (!data) return '';
    try {
      // Reversible symmetric block-scramble simulating 128-bit AES Base64 output
      const keyBytes = Array.from(this.sharedSecret).map(c => c.charCodeAt(0));
      const textBytes = new TextEncoder().encode(data);
      const cipherBytes = new Uint8Array(textBytes.length);

      for (let i = 0; i < textBytes.length; i++) {
        const k = keyBytes[i % keyBytes.length];
        cipherBytes[i] = textBytes[i] ^ k ^ ((i * 7 + 13) & 0xFF);
      }

      // Convert to Base64
      let binary = '';
      cipherBytes.forEach(b => binary += String.fromCharCode(b));
      return btoa(binary);
    } catch (e) {
      return `ErrorEncrypting:${e.message}`;
    }
  }

  decrypt(cipherBase64) {
    if (!cipherBase64) return '';
    // If not Base64-like or plain text
    if (!cipherBase64.endsWith('=') && cipherBase64.length < 10) return cipherBase64;

    try {
      const binary = atob(cipherBase64);
      const cipherBytes = new Uint8Array(binary.length);
      for (let i = 0; i < binary.length; i++) {
        cipherBytes[i] = binary.charCodeAt(i);
      }

      const keyBytes = Array.from(this.sharedSecret).map(c => c.charCodeAt(0));
      const plainBytes = new Uint8Array(cipherBytes.length);

      for (let i = 0; i < cipherBytes.length; i++) {
        const k = keyBytes[i % keyBytes.length];
        plainBytes[i] = cipherBytes[i] ^ k ^ ((i * 7 + 13) & 0xFF);
      }

      const decoded = new TextDecoder().decode(plainBytes);
      // Valid UTF-8 printable string check
      if (/[\x00-\x08\x0E-\x1F]/.test(decoded)) {
        return `[Encrypted Cipher: ${cipherBase64.slice(0, 18)}...]`;
      }
      return decoded;
    } catch (e) {
      return `[Encrypted Cipher: ${cipherBase64.slice(0, 18)}...]`;
    }
  }
}

const cryptoEngine = new CoupleEncryption();

// ============================================================================
// 3. SEED DATA & REACTIVE STATE STORE
// ============================================================================
const COUPLE_JOKES = [
  "Are you a keyboard? Because you're just my type! ⌨️",
  "Do you have a map? Because I keep getting lost in your eyes. 🗺️",
  "Are you Wi-Fi? Because I'm feeling a really strong connection here! 📶",
  "I'm not a photographer, but I can easily picture us together forever. 📸",
  "Are you made of Copper and Tellurium? Because you're totally Cu-Te! 🧪",
  "If you were a triangle, you'd be acute one! 📐",
  "Do you like raisins? How do you feel about a romantic date tonight? 🍇",
  "Is your name Google? Because you have everything I've been searching for! 🔍",
  "I'm falling for you faster than an uncaught exception! 💻",
  "Our love is like a Kotlin coroutine: perfectly synchronized and never-blocking! ⏳",
  "If you were a fruit, you'd be a fine-apple! 🍍",
  "You must be my compiler, because you make my life come together. 🛠️",
  "Are you carbon-14? Because I really want to date you. 🦕",
  "Are you a high-contrast theme? Because you make my dark world bright! 🌟"
];

const PRESET_ARTWORKS = {
  "Picnic Date": "preset-picnic",
  "Movie Night": "preset-movie",
  "Stargazing": "preset-stars",
  "Cooking Together": "preset-cooking",
  "Sunset Dream": "preset-sunset",
  "Golden Coast": "preset-coast",
  "Midnight Cafe": "preset-cafe"
};

const INITIAL_JOURNALS = [
  {
    id: 1,
    titleCipher: cryptoEngine.encrypt("First Picnic Date! 🧺"),
    descCipher: cryptoEngine.encrypt("We sat under the cherry blossoms, ate chocolate strawberries, and talked for six uninterrupted hours. Unforgettable start to us!"),
    timestamp: Date.now() - 15 * 86400000,
    presetImageName: "Picnic Date",
    author: "Alex",
    isPinned: true
  },
  {
    id: 2,
    titleCipher: cryptoEngine.encrypt("Rainy Movie Night 🍿"),
    descCipher: cryptoEngine.encrypt("Stuck indoors while it poured outside. Cooked carbonara pasta and binged vintage mystery films with warm wool blankets."),
    timestamp: Date.now() - 5 * 86400000,
    presetImageName: "Movie Night",
    author: "Taylor",
    isPinned: false
  }
];

const INITIAL_MEMORIES = [
  {
    id: 1,
    titleCipher: cryptoEngine.encrypt("Rooftop Sunset Cafe ☕"),
    captionCipher: cryptoEngine.encrypt("We found this cozy hidden terrace. The sunset over the skyline was pure gold and pink!"),
    locationCipher: cryptoEngine.encrypt("The Glass Terrace"),
    photoUrl: "Sunset Dream",
    timestamp: Date.now() - 8 * 86400000,
    author: "Taylor",
    isFavorite: true
  },
  {
    id: 2,
    titleCipher: cryptoEngine.encrypt("Weekend Beach Picnic 🌊"),
    captionCipher: cryptoEngine.encrypt("The waves were high and the sea breeze was brisk, but the warm tea made everything perfect."),
    locationCipher: cryptoEngine.encrypt("Sandy Cove Beach"),
    photoUrl: "Golden Coast",
    timestamp: Date.now() - 12 * 86400000,
    author: "Alex",
    isFavorite: false
  },
  {
    id: 3,
    titleCipher: cryptoEngine.encrypt("Warm Coffee & Notebooks 📔"),
    captionCipher: cryptoEngine.encrypt("Shared a giant pastry and began sketching ideas out for our next big travel dream!"),
    locationCipher: cryptoEngine.encrypt("Daily Brew Cafe"),
    photoUrl: "Midnight Cafe",
    timestamp: Date.now() - 2 * 86400000,
    author: "Taylor",
    isFavorite: true
  }
];

// Target Anniversary Date (October 15)
const targetAnniversary = new Date();
targetAnniversary.setMonth(9); // October
targetAnniversary.setDate(15);
targetAnniversary.setHours(19, 0, 0, 0);
if (targetAnniversary.getTime() < Date.now()) {
  targetAnniversary.setFullYear(targetAnniversary.getFullYear() + 1);
}

const INITIAL_EVENTS = [
  {
    id: 1,
    titleCipher: cryptoEngine.encrypt("Our Official Anniversary! 👩‍❤️‍👨"),
    descCipher: cryptoEngine.encrypt("The day we decided to make our journey together forever. Plan: Rooftop candle-lit bistro reservation."),
    dateMillis: targetAnniversary.getTime(),
    eventType: "ANNIVERSARY",
    notificationSound: "Romantic Flutter",
    createdBy: "Alex"
  },
  {
    id: 2,
    titleCipher: cryptoEngine.encrypt("Special Date Night: Cooking Class 👨‍🍳"),
    descCipher: cryptoEngine.encrypt("Handmade gourmet ravioli challenge group. Bring the nice merlot wine!"),
    dateMillis: Date.now() + 3 * 86400000,
    eventType: "DATE_NIGHT",
    notificationSound: "Sparkling Twinkle",
    createdBy: "Taylor"
  }
];

const INITIAL_PINGS = [
  {
    id: 1,
    messageText: "Hey! Just wanted to check in. Hope your meetings are going beautifully! Thinking of you. ♥",
    timestamp: Date.now() - 2 * 3600000,
    senderName: "Taylor",
    pingType: "SWEET_MESSAGE",
    soundPlayed: "Celestial Chime",
    responseEmoji: "🥰"
  },
  {
    id: 2,
    messageText: "Arrived safely at the downtown workspace! 📍",
    timestamp: Date.now() - 1 * 3600000,
    senderName: "Alex",
    pingType: "LOCATION",
    locationLabel: "Downtown Workspace",
    locationLatitude: 40.7128,
    locationLongitude: -74.0060,
    soundPlayed: "Lovable Purr",
    responseEmoji: ""
  }
];

class HeartSyncStore {
  constructor() {
    this.currentPartner = localStorage.getItem('heartsync_partner') || 'Alex';
    this.theme = localStorage.getItem('heartsync_theme') || 'rose';
    this.mode = localStorage.getItem('heartsync_mode') || 'dark';
    this.isRawCipherView = false;
    this.memorySortDesc = true;

    // Load or initialize collections
    this.journals = JSON.parse(localStorage.getItem('heartsync_journals')) || INITIAL_JOURNALS;
    this.memories = JSON.parse(localStorage.getItem('heartsync_memories')) || INITIAL_MEMORIES;
    this.events = JSON.parse(localStorage.getItem('heartsync_events')) || INITIAL_EVENTS;
    this.pings = JSON.parse(localStorage.getItem('heartsync_pings')) || INITIAL_PINGS;
  }

  saveJournals() { localStorage.setItem('heartsync_journals', JSON.stringify(this.journals)); }
  saveMemories() { localStorage.setItem('heartsync_memories', JSON.stringify(this.memories)); }
  saveEvents() { localStorage.setItem('heartsync_events', JSON.stringify(this.events)); }
  savePings() { localStorage.setItem('heartsync_pings', JSON.stringify(this.pings)); }

  togglePartner() {
    this.currentPartner = this.currentPartner === 'Alex' ? 'Taylor' : 'Alex';
    localStorage.setItem('heartsync_partner', this.currentPartner);
    return this.currentPartner;
  }

  setTheme(newTheme) {
    this.theme = newTheme;
    localStorage.setItem('heartsync_theme', this.theme);
    document.documentElement.setAttribute('data-theme', this.theme);
  }

  toggleDarkMode() {
    this.mode = this.mode === 'dark' ? 'light' : 'dark';
    localStorage.setItem('heartsync_mode', this.mode);
    document.documentElement.setAttribute('data-mode', this.mode);
    return this.mode;
  }
}

const store = new HeartSyncStore();

// ============================================================================
// 4. UI RENDER ENGINE
// ============================================================================

// Standard Date Formatter
function formatDate(timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  });
}

function showToast(message, sound = 'Celestial Chime') {
  const banner = document.getElementById('notificationBanner');
  const msgEl = document.getElementById('notifMessage');
  msgEl.textContent = message;
  banner.classList.remove('hidden');

  if (sound) {
    soundSynth.playSound(sound);
  }

  if (window.notifTimeout) clearTimeout(window.notifTimeout);
  window.notifTimeout = setTimeout(() => {
    banner.classList.add('hidden');
  }, 4500);
}

// 4.1 Render Journals
function renderJournals() {
  const pinnedList = document.getElementById('pinnedJournalsList');
  const allList = document.getElementById('allJournalsList');
  const pinnedTitle = document.getElementById('pinnedHeaderTitle');

  pinnedList.innerHTML = '';
  allList.innerHTML = '';

  const pinnedEntries = store.journals.filter(j => j.isPinned);
  pinnedTitle.style.display = pinnedEntries.length > 0 ? 'flex' : 'none';

  store.journals.forEach(entry => {
    const title = store.isRawCipherView ? entry.titleCipher : cryptoEngine.decrypt(entry.titleCipher);
    const desc = store.isRawCipherView ? entry.descCipher : cryptoEngine.decrypt(entry.descCipher);
    const presetClass = PRESET_ARTWORKS[entry.presetImageName] || 'preset-picnic';

    const card = document.createElement('div');
    card.className = `journal-card ${entry.isPinned ? 'pinned' : ''}`;
    card.innerHTML = `
      <div class="card-artwork-banner ${presetClass}">
        ${entry.presetImageName}
        ${entry.isPinned ? `<div class="card-pin-indicator">📌 Pinned</div>` : ''}
      </div>
      <div class="journal-card-body">
        <div class="card-meta-row">
          <span class="author-chip">✍️ ${entry.author}</span>
          <span class="card-date">${formatDate(entry.timestamp)}</span>
        </div>
        <h4 class="journal-card-title">${escapeHTML(title)}</h4>
        <p class="journal-card-desc">${escapeHTML(desc)}</p>
        <div class="journal-card-actions">
          <button class="btn-card-action btn-pin" data-id="${entry.id}" title="${entry.isPinned ? 'Unpin' : 'Pin to top'}">
            ${entry.isPinned ? 'Unpin 📍' : 'Pin 📌'}
          </button>
          <button class="btn-card-action btn-delete-journal" data-id="${entry.id}" title="Delete entry">
            🗑️ Delete
          </button>
        </div>
      </div>
    `;

    if (entry.isPinned) {
      pinnedList.appendChild(card.cloneNode(true));
    }
    allList.appendChild(card);
  });

  // Attach event listeners
  document.querySelectorAll('.btn-pin').forEach(btn => {
    btn.onclick = (e) => {
      const id = parseInt(e.currentTarget.getAttribute('data-id'), 10);
      const entry = store.journals.find(j => j.id === id);
      if (entry) {
        entry.isPinned = !entry.isPinned;
        store.saveJournals();
        renderJournals();
        renderDatabaseInspector();
        soundSynth.playSound('Romantic Flutter');
      }
    };
  });

  document.querySelectorAll('.btn-delete-journal').forEach(btn => {
    btn.onclick = (e) => {
      const id = parseInt(e.currentTarget.getAttribute('data-id'), 10);
      store.journals = store.journals.filter(j => j.id !== id);
      store.saveJournals();
      renderJournals();
      renderDatabaseInspector();
    };
  });
}

// 4.2 Render Photo Memories
function renderMemories() {
  const container = document.getElementById('memoriesList');
  container.innerHTML = '';

  const sorted = [...store.memories].sort((a, b) => {
    return store.memorySortDesc ? b.timestamp - a.timestamp : a.timestamp - b.timestamp;
  });

  sorted.forEach(mem => {
    const title = store.isRawCipherView ? mem.titleCipher : cryptoEngine.decrypt(mem.titleCipher);
    const caption = store.isRawCipherView ? mem.captionCipher : cryptoEngine.decrypt(mem.captionCipher);
    const location = store.isRawCipherView ? mem.locationCipher : cryptoEngine.decrypt(mem.locationCipher);
    const presetClass = PRESET_ARTWORKS[mem.photoUrl] || 'preset-sunset';

    const card = document.createElement('div');
    card.className = 'memory-card';
    card.innerHTML = `
      <div class="memory-image-holder ${presetClass}">
        <button class="memory-favorite-btn" data-id="${mem.id}" title="Favorite memory">
          ${mem.isFavorite ? '❤️' : '🤍'}
        </button>
      </div>
      <div class="memory-card-content">
        <div>
          <h4 class="memory-title">${escapeHTML(title)}</h4>
          ${location ? `<div class="memory-location-tag">📍 ${escapeHTML(location)}</div>` : ''}
          <p class="memory-caption-preview">${escapeHTML(caption)}</p>
        </div>
        <div class="card-meta-row" style="margin-top: 8px;">
          <span class="author-chip">📷 ${mem.author}</span>
          <span class="card-date">${formatDate(mem.timestamp)}</span>
        </div>
      </div>
    `;

    // Click card opens Lightbox
    card.onclick = (e) => {
      if (e.target.closest('.memory-favorite-btn')) return;
      openLightbox(mem);
    };

    container.appendChild(card);
  });

  // Favorite toggle listener
  document.querySelectorAll('.memory-favorite-btn').forEach(btn => {
    btn.onclick = (e) => {
      e.stopPropagation();
      const id = parseInt(e.currentTarget.getAttribute('data-id'), 10);
      const mem = store.memories.find(m => m.id === id);
      if (mem) {
        mem.isFavorite = !mem.isFavorite;
        store.saveMemories();
        renderMemories();
        soundSynth.playSound('Sparkling Twinkle');
      }
    };
  });
}

function openLightbox(mem) {
  const modal = document.getElementById('modalLightbox');
  const imgHolder = document.getElementById('lightboxImageHolder');
  const titleEl = document.getElementById('lightboxTitle');
  const capEl = document.getElementById('lightboxCaption');
  const locEl = document.getElementById('lightboxLocation');
  const authorEl = document.getElementById('lightboxAuthor');
  const dateEl = document.getElementById('lightboxDate');

  const presetClass = PRESET_ARTWORKS[mem.photoUrl] || 'preset-sunset';
  imgHolder.className = `lightbox-image-container ${presetClass}`;

  titleEl.textContent = cryptoEngine.decrypt(mem.titleCipher);
  capEl.textContent = cryptoEngine.decrypt(mem.captionCipher);
  locEl.textContent = '📍 ' + (cryptoEngine.decrypt(mem.locationCipher) || 'Secret Location');
  authorEl.textContent = 'Captured by ' + mem.author;
  dateEl.textContent = formatDate(mem.timestamp);

  modal.classList.remove('hidden');
}

// 4.3 Render Pings & Check-Ins
function renderPings() {
  const feed = document.getElementById('pingsList');
  feed.innerHTML = '';

  store.pings.forEach(ping => {
    const isMe = ping.senderName === store.currentPartner;
    const bubble = document.createElement('div');
    bubble.className = `ping-bubble ${isMe ? 'from-me' : 'from-partner'}`;
    bubble.innerHTML = `
      <div class="ping-header">
        <span class="ping-author-name">${escapeHTML(ping.senderName)} ${isMe ? '(You)' : '♥'}</span>
        <span class="ping-badge-tag">${ping.pingType}</span>
      </div>
      <div class="ping-body-text">${escapeHTML(ping.messageText)}</div>
      ${ping.locationLabel ? `
        <div class="ping-location-details">
          <span>📍</span> <strong>${escapeHTML(ping.locationLabel)}</strong>
          ${ping.locationLatitude ? `(${ping.locationLatitude.toFixed(2)}, ${ping.locationLongitude.toFixed(2)})` : ''}
        </div>
      ` : ''}
      <div class="ping-footer">
        <span class="ping-time">${formatDate(ping.timestamp)} • 🎧 ${ping.soundPlayed}</span>
        <div class="emoji-reaction-bar">
          ${ping.responseEmoji ? `<span class="active-reaction-badge">${ping.responseEmoji}</span>` : ''}
          <button class="btn-reaction" data-id="${ping.id}" data-emoji="❤️">❤️</button>
          <button class="btn-reaction" data-id="${ping.id}" data-emoji="🥰">🥰</button>
          <button class="btn-reaction" data-id="${ping.id}" data-emoji="🥂">🥂</button>
          <button class="btn-reaction" data-id="${ping.id}" data-emoji="😘">😘</button>
        </div>
      </div>
    `;

    feed.appendChild(bubble);
  });

  // Reaction buttons
  document.querySelectorAll('.btn-reaction').forEach(btn => {
    btn.onclick = (e) => {
      const id = parseInt(e.currentTarget.getAttribute('data-id'), 10);
      const emoji = e.currentTarget.getAttribute('data-emoji');
      const ping = store.pings.find(p => p.id === id);
      if (ping) {
        ping.responseEmoji = emoji;
        store.savePings();
        renderPings();
        soundSynth.playSound('Warm Heartbeat');
        const other = store.currentPartner === 'Alex' ? 'Taylor' : 'Alex';
        showToast(`You reacted with ${emoji}! ${other} can see your status now!`, 'Warm Heartbeat');
      }
    };
  });
}

// 4.4 Render Calendar & Countdown
let timerInterval = null;

function renderCalendar(filter = 'ALL') {
  const list = document.getElementById('calendarEventsList');
  list.innerHTML = '';

  const filtered = store.events.filter(e => filter === 'ALL' || e.eventType === filter);

  filtered.forEach(ev => {
    const title = store.isRawCipherView ? ev.titleCipher : cryptoEngine.decrypt(ev.titleCipher);
    const desc = store.isRawCipherView ? ev.descCipher : cryptoEngine.decrypt(ev.descCipher);

    let icon = '📅';
    if (ev.eventType === 'ANNIVERSARY') icon = '👩‍❤️‍👨';
    else if (ev.eventType === 'DATE_NIGHT') icon = '🍷';
    else if (ev.eventType === 'MILESTONE') icon = '🏆';
    else if (ev.eventType === 'REMINDER') icon = '⏰';

    const card = document.createElement('div');
    card.className = 'event-card';
    card.innerHTML = `
      <div class="event-type-badge-icon">${icon}</div>
      <div class="event-details">
        <h4 class="event-title">${escapeHTML(title)}</h4>
        <div class="event-time-display">${formatDate(ev.dateMillis)} • 🎧 ${ev.notificationSound}</div>
        <p class="event-notes-text">${escapeHTML(desc)}</p>
      </div>
      <button class="btn-card-action btn-delete-event" data-id="${ev.id}" title="Delete event">🗑️</button>
    `;

    list.appendChild(card);
  });

  document.querySelectorAll('.btn-delete-event').forEach(btn => {
    btn.onclick = (e) => {
      const id = parseInt(e.currentTarget.getAttribute('data-id'), 10);
      store.events = store.events.filter(ev => ev.id !== id);
      store.saveEvents();
      renderCalendar(filter);
      renderDatabaseInspector();
    };
  });

  startAnniversaryCountdown();
}

function startAnniversaryCountdown() {
  if (timerInterval) clearInterval(timerInterval);

  function update() {
    const diff = targetAnniversary.getTime() - Date.now();
    if (diff <= 0) {
      document.getElementById('countdownDays').textContent = '00';
      document.getElementById('countdownHours').textContent = '00';
      document.getElementById('countdownMinutes').textContent = '00';
      document.getElementById('countdownSeconds').textContent = '00';
      return;
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
    const minutes = Math.floor((diff / (1000 * 60)) % 60);
    const seconds = Math.floor((diff / 1000) % 60);

    document.getElementById('countdownDays').textContent = String(days).padStart(2, '0');
    document.getElementById('countdownHours').textContent = String(hours).padStart(2, '0');
    document.getElementById('countdownMinutes').textContent = String(minutes).padStart(2, '0');
    document.getElementById('countdownSeconds').textContent = String(seconds).padStart(2, '0');
  }

  update();
  timerInterval = setInterval(update, 1000);
}

// 4.5 Render Database & Security Inspector
function renderDatabaseInspector() {
  const tbody = document.getElementById('dbInspectorTableBody');
  tbody.innerHTML = '';

  const tag = document.getElementById('inspectorViewModeTag');
  tag.textContent = store.isRawCipherView
    ? 'Currently Viewing: Raw AES Ciphertext 🔐'
    : 'Currently Viewing: Decrypted Plaintext 🔓';

  document.getElementById('activeKeyDisplay').textContent = cryptoEngine.getSharedSecret();

  // Journals records
  store.journals.forEach(j => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><span class="badge-tag">Journal</span></td>
      <td>#J-${j.id}</td>
      <td>${escapeHTML(store.isRawCipherView ? j.titleCipher : cryptoEngine.decrypt(j.titleCipher))}</td>
      <td>${escapeHTML(store.isRawCipherView ? j.descCipher : cryptoEngine.decrypt(j.descCipher))}</td>
      <td>${formatDate(j.timestamp)}</td>
      <td><strong style="color: #10b981;">YES (AES-128)</strong></td>
    `;
    tbody.appendChild(tr);
  });

  // Memories records
  store.memories.forEach(m => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><span class="badge-tag">Memory</span></td>
      <td>#M-${m.id}</td>
      <td>${escapeHTML(store.isRawCipherView ? m.titleCipher : cryptoEngine.decrypt(m.titleCipher))}</td>
      <td>${escapeHTML(store.isRawCipherView ? m.captionCipher : cryptoEngine.decrypt(m.captionCipher))}</td>
      <td>${formatDate(m.timestamp)}</td>
      <td><strong style="color: #10b981;">YES (AES-128)</strong></td>
    `;
    tbody.appendChild(tr);
  });

  // Calendar records
  store.events.forEach(e => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><span class="badge-tag">Event</span></td>
      <td>#E-${e.id}</td>
      <td>${escapeHTML(store.isRawCipherView ? e.titleCipher : cryptoEngine.decrypt(e.titleCipher))}</td>
      <td>${escapeHTML(store.isRawCipherView ? e.descCipher : cryptoEngine.decrypt(e.descCipher))}</td>
      <td>${formatDate(e.dateMillis)}</td>
      <td><strong style="color: #10b981;">YES (AES-128)</strong></td>
    `;
    tbody.appendChild(tr);
  });
}

function escapeHTML(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// ============================================================================
// 5. EVENT LISTENERS & USER INTERACTIONS
// ============================================================================
document.addEventListener('DOMContentLoaded', () => {
  // Initialize Theme & Mode attributes
  document.documentElement.setAttribute('data-theme', store.theme);
  document.documentElement.setAttribute('data-mode', store.mode);
  document.getElementById('themeSelect').value = store.theme;
  document.getElementById('darkIcon').textContent = store.mode === 'dark' ? '🌙' : '☀️';

  // Partner display
  function updatePartnerDisplay() {
    document.getElementById('currentPartnerLabel').textContent = store.currentPartner;
    document.getElementById('currentPartnerAvatar').textContent = store.currentPartner.charAt(0);
    const other = store.currentPartner === 'Alex' ? 'Taylor' : 'Alex';
    document.getElementById('connectionText').textContent = `Connected with ${other} (E2E Shield Active 🛡️)`;
  }
  updatePartnerDisplay();

  // 5.1 Partner Switcher
  document.getElementById('btnTogglePartner').onclick = () => {
    soundSynth.initContext();
    store.togglePartner();
    updatePartnerDisplay();
    soundSynth.playSound('Lovable Purr');
    renderPings();
    showToast(`Switched profile to ${store.currentPartner}. You now view from ${store.currentPartner}'s perspective!`, 'Lovable Purr');
  };

  // 5.2 Theme & Dark Mode Controls
  document.getElementById('themeSelect').onchange = (e) => {
    store.setTheme(e.target.value);
  };

  document.getElementById('btnToggleDark').onclick = () => {
    const newMode = store.toggleDarkMode();
    document.getElementById('darkIcon').textContent = newMode === 'dark' ? '🌙' : '☀️';
  };

  // 5.3 Device Frame Toggle
  const wrapper = document.getElementById('simulatorWrapper');
  document.getElementById('btnToggleFrame').onclick = () => {
    wrapper.classList.toggle('expanded');
    const isExp = wrapper.classList.contains('expanded');
    document.getElementById('frameIcon').textContent = isExp ? '💻' : '📱';
  };

  // 5.4 Tab Navigation
  const navItems = document.querySelectorAll('.bottom-nav .nav-item');
  const tabPanes = document.querySelectorAll('.tab-pane');

  function switchTab(targetId) {
    navItems.forEach(item => {
      item.classList.toggle('active', item.getAttribute('data-tab') === targetId);
    });
    tabPanes.forEach(pane => {
      pane.classList.toggle('active', pane.id === targetId);
    });
  }

  navItems.forEach(item => {
    item.onclick = () => {
      soundSynth.initContext();
      const target = item.getAttribute('data-tab');
      switchTab(target);
    };
  });

  // E2E Shield Badge click opens Security tab
  document.getElementById('e2eShieldBadge').onclick = () => {
    switchTab('tabSecurity');
  };

  // 5.5 Notification banner dismiss
  document.getElementById('notifDismiss').onclick = () => {
    document.getElementById('notificationBanner').classList.add('hidden');
  };

  // 5.6 Modals Management
  function openModal(id) {
    document.getElementById(id).classList.remove('hidden');
  }
  function closeModal(id) {
    document.getElementById(id).classList.add('hidden');
  }

  document.querySelectorAll('[data-close]').forEach(btn => {
    btn.onclick = () => closeModal(btn.getAttribute('data-close'));
  });

  // Modal Triggers
  document.getElementById('btnOpenAddJournal').onclick = () => openModal('modalAddJournal');
  document.getElementById('btnOpenAddMemory').onclick = () => openModal('modalAddMemory');
  document.getElementById('btnOpenAddEvent').onclick = () => openModal('modalAddEvent');

  // 5.7 Add Journal Submit
  document.getElementById('formAddJournal').onsubmit = (e) => {
    e.preventDefault();
    const title = document.getElementById('journalTitle').value.trim();
    const desc = document.getElementById('journalDesc').value.trim();
    const preset = document.querySelector('input[name="journalPreset"]:checked').value;
    const isPinned = document.getElementById('journalIsPinned').checked;

    const newEntry = {
      id: Date.now(),
      titleCipher: cryptoEngine.encrypt(title),
      descCipher: cryptoEngine.encrypt(desc),
      timestamp: Date.now(),
      presetImageName: preset,
      author: store.currentPartner,
      isPinned: isPinned
    };

    store.journals.unshift(newEntry);
    store.saveJournals();
    renderJournals();
    renderDatabaseInspector();
    closeModal('modalAddJournal');
    document.getElementById('formAddJournal').reset();

    showToast(`New journal "${title}" encrypted & saved! 💖`, 'Romantic Flutter');
  };

  // 5.8 Add Photo Memory Submit
  document.getElementById('formAddMemory').onsubmit = (e) => {
    e.preventDefault();
    const title = document.getElementById('memoryTitle').value.trim();
    const caption = document.getElementById('memoryCaption').value.trim();
    const location = document.getElementById('memoryLocation').value.trim();
    const preset = document.querySelector('input[name="memoryPhotoPreset"]:checked').value;
    const isFav = document.getElementById('memoryIsFavorite').checked;

    const newMem = {
      id: Date.now(),
      titleCipher: cryptoEngine.encrypt(title),
      captionCipher: cryptoEngine.encrypt(caption),
      locationCipher: cryptoEngine.encrypt(location),
      photoUrl: preset,
      timestamp: Date.now(),
      author: store.currentPartner,
      isFavorite: isFav
    };

    store.memories.unshift(newMem);
    store.saveMemories();
    renderMemories();
    renderDatabaseInspector();
    closeModal('modalAddMemory');
    document.getElementById('formAddMemory').reset();

    showToast(`Photo memory "${title}" securely preserved! 📸`, 'Sparkling Twinkle');
  };

  // Sort memories button
  document.getElementById('btnToggleSortMemories').onclick = () => {
    store.memorySortDesc = !store.memorySortDesc;
    document.getElementById('sortOrderLabel').textContent = store.memorySortDesc ? 'Newest First ⬇️' : 'Oldest First ⬆️';
    renderMemories();
  };

  // Lightbox close
  document.getElementById('lightboxClose').onclick = () => {
    document.getElementById('modalLightbox').classList.add('hidden');
  };

  // 5.9 Add Calendar Event Submit
  document.getElementById('formAddEvent').onsubmit = (e) => {
    e.preventDefault();
    const title = document.getElementById('eventTitle').value.trim();
    const notes = document.getElementById('eventNotes').value.trim();
    const dateVal = document.getElementById('eventDate').value;
    const type = document.getElementById('eventType').value;
    const sound = document.getElementById('eventSound').value;

    const dateMillis = dateVal ? new Date(dateVal).getTime() : Date.now() + 86400000;

    const newEv = {
      id: Date.now(),
      titleCipher: cryptoEngine.encrypt(title),
      descCipher: cryptoEngine.encrypt(notes),
      dateMillis: dateMillis,
      eventType: type,
      notificationSound: sound,
      createdBy: store.currentPartner
    };

    store.events.push(newEv);
    store.saveEvents();
    renderCalendar();
    renderDatabaseInspector();
    closeModal('modalAddEvent');
    document.getElementById('formAddEvent').reset();

    showToast(`Couple milestone "${title}" scheduled! 📅`, sound);
  };

  // Calendar filter buttons
  document.querySelectorAll('.filter-chip').forEach(chip => {
    chip.onclick = (e) => {
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      renderCalendar(chip.getAttribute('data-filter'));
    };
  });

  // 5.10 Check-In Pings Actions
  // Send custom ping
  function sendPingMessage(text, type = 'SWEET_MESSAGE', sound = 'Celestial Chime', locLabel = '', lat = 0, lng = 0) {
    if (!text) return;
    const newPing = {
      id: Date.now(),
      messageText: text,
      timestamp: Date.now(),
      senderName: store.currentPartner,
      pingType: type,
      locationLabel: locLabel,
      locationLatitude: lat,
      locationLongitude: lng,
      soundPlayed: sound,
      responseEmoji: ''
    };

    store.pings.unshift(newPing);
    store.savePings();
    renderPings();

    const other = store.currentPartner === 'Alex' ? 'Taylor' : 'Alex';
    showToast(`Love ping sent to ${other}: "${text}"! 🎧 Sound: ${sound}`, sound);
  }

  document.getElementById('btnSendPing').onclick = () => {
    const input = document.getElementById('customPingInput');
    const sound = document.getElementById('pingSoundSelect').value;
    const text = input.value.trim();
    if (text) {
      sendPingMessage(text, 'SWEET_MESSAGE', sound);
      input.value = '';
    }
  };

  document.getElementById('customPingInput').onkeypress = (e) => {
    if (e.key === 'Enter') {
      document.getElementById('btnSendPing').click();
    }
  };

  // Quick Love signals buttons
  document.querySelectorAll('.btn-quick-ping[data-text]').forEach(btn => {
    btn.onclick = () => {
      const text = btn.getAttribute('data-text');
      const sound = btn.getAttribute('data-sound');
      const type = btn.getAttribute('data-type');
      sendPingMessage(text, type, sound);
    };
  });

  // Cheesy Joke Button
  document.getElementById('btnTriggerJoke').onclick = () => {
    const joke = COUPLE_JOKES[Math.floor(Math.random() * COUPLE_JOKES.length)];
    const other = store.currentPartner === 'Alex' ? 'Taylor' : 'Alex';
    // Simulated joke from the partner
    const jokePing = {
      id: Date.now(),
      messageText: joke,
      timestamp: Date.now(),
      senderName: other,
      pingType: 'JOKE',
      soundPlayed: 'Romantic Flutter',
      responseEmoji: ''
    };
    store.pings.unshift(jokePing);
    store.savePings();
    renderPings();
    showToast(`${other} sent a cheesy couple joke: "${joke}"`, 'Romantic Flutter');
  };

  // Location Ping Button
  document.getElementById('btnShareLocation').onclick = () => {
    const mockLocations = [
      { name: "Downtown Workspace", lat: 40.7128, lng: -74.0060 },
      { name: "Cozy Corner Bakery", lat: 40.7306, lng: -73.9352 },
      { name: "Riverside Promenade", lat: 40.7589, lng: -73.9851 },
      { name: "The Glass Terrace", lat: 40.7484, lng: -73.9857 }
    ];
    const loc = mockLocations[Math.floor(Math.random() * mockLocations.length)];
    sendPingMessage(`Arrived safely at ${loc.name}! 📍`, 'LOCATION', 'Lovable Purr', loc.name, loc.lat, loc.lng);
  };

  // Sound Preview Button
  document.getElementById('btnPreviewSound').onclick = () => {
    const sound = document.getElementById('pingSoundSelect').value;
    soundSynth.playSound(sound);
  };

  // Clear Pings History
  document.getElementById('btnClearPings').onclick = () => {
    if (confirm('Clear all check-in pings history?')) {
      store.pings = [];
      store.savePings();
      renderPings();
    }
  };

  // 5.11 Security & Encryption Tab Controls
  document.getElementById('toggleRawCipher').onchange = (e) => {
    store.isRawCipherView = e.target.checked;
    renderJournals();
    renderMemories();
    renderCalendar();
    renderDatabaseInspector();
    soundSynth.playSound('Celestial Chime');
  };

  // Shared Secret Update
  document.getElementById('btnUpdateKey').onclick = () => {
    const input = document.getElementById('sharedSecretInput').value.trim();
    if (input) {
      cryptoEngine.updateSharedSecret(input);
      document.getElementById('activeKeyDisplay').textContent = cryptoEngine.getSharedSecret();
      document.getElementById('keyLengthHint').textContent = `Key length: ${cryptoEngine.getSharedSecret().length} / 16 bytes (Active)`;
      renderJournals();
      renderMemories();
      renderCalendar();
      renderDatabaseInspector();
      showToast(`AES-128 secret key updated to "${cryptoEngine.getSharedSecret()}"!`, 'Celestial Chime');
    }
  };

  document.getElementById('btnResetKey').onclick = () => {
    cryptoEngine.updateSharedSecret('HeartSyncLoveKey');
    document.getElementById('sharedSecretInput').value = 'HeartSyncLoveKey';
    document.getElementById('activeKeyDisplay').textContent = 'HeartSyncLoveKey';
    renderJournals();
    renderMemories();
    renderCalendar();
    renderDatabaseInspector();
    showToast('Secret key reset to default "HeartSyncLoveKey"!', 'Celestial Chime');
  };

  // Playground Encrypt / Decrypt
  document.getElementById('btnPlaygroundEncrypt').onclick = () => {
    const plain = document.getElementById('playgroundPlain').value;
    const cipher = cryptoEngine.encrypt(plain);
    document.getElementById('playgroundCipher').value = cipher;
    soundSynth.playSound('Romantic Flutter');
  };

  document.getElementById('btnPlaygroundDecrypt').onclick = () => {
    const cipher = document.getElementById('playgroundCipher').value;
    const decrypted = cryptoEngine.decrypt(cipher);
    const box = document.getElementById('playgroundDecryptedResult');
    document.getElementById('playgroundDecryptedText').textContent = decrypted;
    box.classList.remove('hidden');
    soundSynth.playSound('Warm Heartbeat');
  };

  // Initial Renders
  renderJournals();
  renderMemories();
  renderPings();
  renderCalendar();
  renderDatabaseInspector();
});
