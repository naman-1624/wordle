/* ============================================================
   WORDLE — script.js
   Client for the server-authoritative backend (JWT auth)
   ============================================================ */

/* ---------- CONSTANTS ---------- */
const WORD_LENGTH = 5;
const MAX_ATTEMPTS = 6;
const TIMER_SECONDS = 60;

const WIN_MESSAGES = [
  "Genius", "Magnificent", "Impressive",
  "Splendid", "Great", "Phew"
];

const MODE_NAMES = { normal: 'Normal', time: '60s Time', sudden: 'Sudden Death', pack: 'Custom Pack' };

/* ---------- STATE ---------- */
let board = [];
let row = 0;
let col = 0;
let gameMode = null;
let gameId = null;
let gameOver = true;
let isSubmitting = false;
let keyColors = {};
let playedToday = { normal: false, time: false, sudden: false };
let packs = [];
let timerInterval = null;
let timeLeft = TIMER_SECONDS;
let gameStartedAt = null;

/* ---------- AUTH HELPERS ---------- */
function getToken() { return localStorage.getItem('token'); }
function getUsername() { return localStorage.getItem('user'); }

async function api(path, options = {}) {
  const headers = Object.assign({
    'Authorization': 'Bearer ' + (getToken() || ''),
    'Content-Type': 'application/json'
  }, options.headers || {});
  const opts = Object.assign({}, options, { headers });
  if (opts.body && typeof opts.body !== 'string') opts.body = JSON.stringify(opts.body);
  const res = await fetch(path, opts);
  if (res.status === 401) {
    clearSession();
    window.location.replace('/login');
    throw new Error('Session expired');
  }
  return res;
}

function clearSession() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('role');
  localStorage.removeItem('gameInProgress');
}

function logout(event) {
  if (event) event.preventDefault();
  if (confirm('Are you sure you want to logout?')) {
    clearSession();
    window.location.replace('/');
  }
}

/* ---------- INIT ---------- */
document.addEventListener("DOMContentLoaded", init);

async function init() {
  if (!getToken() || !getUsername()) {
    alert('Please login to play!');
    window.location.replace('/');
    return;
  }

  history.pushState(null, null, location.href);
  window.onpopstate = function () {
    if (!getToken()) window.location.replace('/');
    else history.pushState(null, null, location.href);
  };

  createBoard();
  createKeyboard();
  updateMenuItems();

  try {
    const me = await api('/api/auth/me');
    const user = await me.json();
    localStorage.setItem('role', user.role || 'USER');
  } catch (e) { /* token invalid handled already */ }

  await refreshStatus();
}

async function refreshStatus() {
  try {
    const res = await api('/api/games/status');
    const data = await res.json();
    playedToday = data.playedToday || playedToday;

    if (data.currentGame) {
      configureGame(data.currentGame);
    } else {
      disableGameInput();
      updateModeButtons();
      showDefaultBoardState();
      setTimeout(() => openModeModal(), 300);
    }
  } catch (e) {
    console.error('Failed to load game status', e);
  }
}

/* ---------- DATE HELPERS ---------- */
function getTodayString() {
  const now = new Date();
  return `${now.getFullYear()}-${now.getMonth() + 1}-${now.getDate()}`;
}

/* ---------- GAME CONFIGURATION (from server) ---------- */
function configureGame(view) {
  gameMode = view.mode;
  gameId = view.gameId;
  gameOver = view.status === 'IN_PROGRESS' ? false : true;
  isSubmitting = false;
  localStorage.setItem('lastPlayedMode', gameMode);
  localStorage.setItem('gameInProgress', gameOver ? 'false' : 'true');

  resetBoard();

  if (view.history && view.history.length) {
    view.history.forEach((entry, i) => {
      const result = entry.result.split('');
      result.forEach((r, c) => {
        const tile = board[i][c];
        tile.innerText = entry.guess[c];
        tile.classList.add(r === 'G' ? 'green' : r === 'Y' ? 'yellow' : 'gray');
        updateKeyColor(entry.guess[c], r === 'G' ? 'green' : r === 'Y' ? 'yellow' : 'gray');
      });
    });
    row = view.history.length;
    col = 0;
  }

  gameStartedAt = Date.now();

  if (gameOver) {
    disableGameInput();
    showDefaultBoardState(true);
  } else {
    enableGameInput();
  }

  loadHintForMode(gameMode);
  if (gameMode === 'time') startTimer();
  else clearTimer();
}

function resetBoard() {
  row = 0; col = 0; keyColors = {};
  for (let r = 0; r < MAX_ATTEMPTS; r++) {
    for (let c = 0; c < WORD_LENGTH; c++) {
      board[r][c].innerText = "";
      board[r][c].className = "tile";
    }
  }
  document.querySelectorAll(".key").forEach(k => k.classList.remove("green", "yellow", "gray"));
}

/* ---------- SHOW DEFAULT BOARD (last completed game) ---------- */
function showDefaultBoardState(skipCheck) {
  const today = getTodayString();
  const priority = ['normal', 'time', 'sudden'];
  if (!skipCheck) {
    for (const mode of priority) {
      if (playedToday[mode]) {
        const saved = localStorage.getItem(`completedBoard_${getUsername()}_${mode}_${today}`);
        if (saved) { renderSavedBoard(saved); return; }
      }
    }
  } else {
    const saved = localStorage.getItem(`completedBoard_${getUsername()}_${gameMode}_${today}`);
    if (saved) renderSavedBoard(saved);
    else {
      const last = localStorage.getItem('lossGameState');
      if (last) renderSavedBoard(last);
    }
  }
}

function renderSavedBoard(json) {
  try {
    const gs = JSON.parse(json);
    if (!gs.boardState) return;
    for (let r = 0; r < MAX_ATTEMPTS; r++) {
      for (let c = 0; c < WORD_LENGTH; c++) {
        if (gs.boardState[r] && gs.boardState[r][c]) {
          board[r][c].innerText = gs.boardState[r][c].text || "";
          board[r][c].className = gs.boardState[r][c].classes || "tile";
        }
      }
    }
    if (gs.keyColors) {
      Object.entries(gs.keyColors).forEach(([letter, color]) => {
        const el = document.getElementById("key-" + letter);
        if (el) { el.classList.remove("green", "yellow", "gray"); el.classList.add(color); }
      });
    }
  } catch (e) { /* ignore */ }
}

/* ---------- ACCESS / SESSION ---------- */
function hasModeBeenPlayedToday(mode) {
  return !!playedToday[mode];
}

/* ---------- HINT ---------- */
async function loadHintForMode(mode) {
  const banner = document.getElementById('hintBanner');
  const fallback = mode === 'pack'
    ? '💡 Solve the custom word pack word!'
    : '💡 Guess the 5-letter word!';

  if (mode === 'pack') {
    if (banner) { banner.textContent = fallback; banner.classList.add('visible'); }
    return;
  }

  try {
    const response = await api(`/api/hint?mode=${mode}`);
    const data = await response.json();
    if (banner) { banner.textContent = data.hint; banner.classList.add('visible'); }
  } catch (error) {
    console.error('Error loading hint:', error);
    if (banner) { banner.textContent = fallback; banner.classList.add('visible'); }
  }
}

/* ---------- MODE SELECTION ---------- */
function resetGame() {
  resetBoard();
  gameOver = false;
}

async function startGame(mode, packId) {
  if (!gameOver) { showToast("Can't change mode during an active game"); return; }
  if (gameId) {
    // finished game is being replaced by a new one
  }

  isSubmitting = true;
  try {
    const res = await fetch('/api/games/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
      body: JSON.stringify({ mode, packId })
    });

    if (res.status === 409) {
      const data = await res.json();
      showToast(data.message || 'Already played today');
      showAlreadyPlayedPopup(mode);
      isSubmitting = false;
      return;
    }
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      showToast(data.message || 'Could not start game');
      isSubmitting = false;
      return;
    }

    const view = await res.json();
    document.getElementById("mode-modal").style.display = "none";
    configureGame(view);
  } catch (err) {
    console.error(err);
    showToast('Network error. Try again.');
  }
  isSubmitting = false;
}

function openModeModal() {
  if (!gameOver && gameId) { showToast("Can't change mode during an active game"); return; }
  updateModeButtons();
  renderPackButtons();
  document.getElementById("mode-modal").style.display = "flex";
}

function updateModeButtons() {
  const modes = ['normal', 'time', 'sudden'];
  const labels = { normal: 'Normal', time: '60s Time Mode', sudden: 'Sudden Death' };
  const modeBox = document.getElementById('mode-box');
  if (!modeBox) return;
  const buttons = modeBox.querySelectorAll('.mode-btn');
  modes.forEach((mode, idx) => {
    if (!buttons[idx]) return;
    const played = hasModeBeenPlayedToday(mode);
    buttons[idx].style.background = played ? '#787c7e' : '#6aaa64';
    buttons[idx].style.opacity = played ? '0.75' : '1';
    buttons[idx].style.cursor = played ? 'not-allowed' : 'pointer';
    buttons[idx].textContent = played ? `${labels[mode]} ✓ Done` : labels[mode];
    buttons[idx].disabled = played;
  });
}

async function renderPackButtons() {
  const container = document.getElementById('mode-packs');
  if (!container) return;
  try {
    const res = await api('/api/packs');
    packs = await res.json();
  } catch (e) { packs = []; }

  if (!packs || packs.length === 0) {
    container.style.display = 'none';
    return;
  }
  container.style.display = 'block';
  container.innerHTML = '<h3>Custom Packs</h3>';
  packs.forEach(p => {
    const btn = document.createElement('button');
    btn.className = 'pack-btn';
    btn.textContent = p.name + ` (${p.wordCount})`;
    btn.title = p.description || '';
    btn.onclick = () => startGame('pack', p.id);
    container.appendChild(btn);
  });
}

/* ---------- ALREADY PLAYED POPUP ---------- */
function showAlreadyPlayedPopup(mode) {
  const modeName = MODE_NAMES[mode] || mode;

  const now = new Date(); const midnight = new Date(); midnight.setHours(24, 0, 0, 0);
  const msLeft = midnight - now;
  const h = Math.floor(msLeft / (1000 * 60 * 60));
  const m = Math.floor((msLeft % (1000 * 60 * 60)) / (1000 * 60));

  const existing = document.getElementById('already-played-msg');
  if (existing) existing.remove();

  const popup = document.createElement('div');
  popup.id = 'already-played-msg';
  popup.style.cssText = `
    position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);
    background:white;border:2px solid #6aaa64;border-radius:12px;
    padding:24px 28px;text-align:center;z-index:9999;
    box-shadow:0 10px 30px rgba(0,0,0,0.25);
    max-width:340px;width:90%;max-height:90vh;overflow-y:auto;
  `;
  popup.innerHTML = `
    <div style="font-size:36px;margin-bottom:8px;">✅</div>
    <h2 style="margin-bottom:6px;color:#121212;font-size:18px;">Already Played!</h2>
    <p style="color:#555;font-size:13px;margin-bottom:4px;">
      You already completed <strong>${modeName}</strong> today.
    </p>
    <p style="color:#888;font-size:12px;margin-bottom:8px;">
      Next reset in <strong>${h}h ${m}m</strong>
    </p>
    <div style="display:flex;gap:8px;justify-content:center;flex-wrap:wrap;margin-top:14px;">
      <button onclick="document.getElementById('already-played-msg').remove();openModeModal();"
        style="padding:9px 16px;background:#6aaa64;color:white;border:none;
               border-radius:6px;cursor:pointer;font-weight:bold;font-size:13px;">
        Try Another Mode
      </button>
      <button onclick="document.getElementById('already-played-msg').remove();"
        style="padding:9px 16px;background:#eee;color:#333;border:none;
               border-radius:6px;cursor:pointer;font-weight:bold;font-size:13px;">
        Close
      </button>
    </div>
  `;
  document.body.appendChild(popup);
}

/* ---------- INPUT ENABLE / DISABLE ---------- */
function disableGameInput() {
  board.forEach(r => r.forEach(t => (t.style.pointerEvents = "none")));
  document.getElementById("keyboard").style.pointerEvents = "none";
}
function enableGameInput() {
  board.forEach(r => r.forEach(t => (t.style.pointerEvents = "auto")));
  document.getElementById("keyboard").style.pointerEvents = "auto";
}

/* ---------- TIMER ---------- */
function startTimer() {
  if (hasModeBeenPlayedToday('time') && row === 0 && !gameStartedAt) {
    gameOver = true;
    disableGameInput();
    clearGameState();
    showAlreadyPlayedPopup('time');
    return;
  }

  const now = Date.now();
  let elapsed = gameStartedAt ? Math.floor((now - gameStartedAt) / 1000) : 0;
  timeLeft = Math.max(0, TIMER_SECONDS - elapsed);

  updateTimer();
  if (timerInterval) clearInterval(timerInterval);

  timerInterval = setInterval(() => {
    timeLeft--;
    updateTimer();
    if (timeLeft <= 0) {
      handleTimeUp();
    }
  }, 1000);
}

function clearTimer() {
  clearInterval(timerInterval); timerInterval = null;
  document.getElementById("timer").innerText = "";
}

function updateTimer() {
  document.getElementById("timer").innerText = `⏱ ${timeLeft}s`;
}

async function handleTimeUp() {
  if (gameOver) return;
  gameOver = true;
  clearTimer();
  disableGameInput();
  showToast("⏰ Time's up!");

  const duration = gameStartedAt ? Math.floor((Date.now() - gameStartedAt) / 1000) : TIMER_SECONDS;
  try {
    await api(`/api/games/${gameId}/forfeit`, {
      method: 'POST',
      body: JSON.stringify({ durationSeconds: duration })
    });
  } catch (e) { /* ignore */ }

  saveCompletedBoard('time');
  setTimeout(() => { window.location.href = '/loss'; }, 1600);
}

/* ---------- BOARD ---------- */
function createBoard() {
  const boardDiv = document.getElementById("board");
  for (let r = 0; r < MAX_ATTEMPTS; r++) {
    const rowDiv = document.createElement("div");
    rowDiv.className = "row";
    board[r] = [];
    for (let c = 0; c < WORD_LENGTH; c++) {
      const tile = document.createElement("div");
      tile.className = "tile";
      rowDiv.appendChild(tile);
      board[r][c] = tile;
    }
    boardDiv.appendChild(rowDiv);
  }
}

/* ---------- KEYBOARD ---------- */
function createKeyboard() {
  const layout = [
    ["Q","W","E","R","T","Y","U","I","O","P"],
    ["A","S","D","F","G","H","J","K","L"],
    ["ENTER","Z","X","C","V","B","N","M","⌫"]
  ];
  const kb = document.getElementById("keyboard");
  layout.forEach((r, i) => {
    const rowDiv = document.createElement("div");
    rowDiv.className = "keyboard-row";
    if (i === 1) rowDiv.style.marginLeft = "18px";
    r.forEach(k => {
      const key = document.createElement("div");
      key.className = "key";
      if (k === "ENTER" || k === "⌫") key.classList.add("wide");
      key.innerText = k;
      key.id = "key-" + k;
      key.onclick = () => handle(k);
      rowDiv.appendChild(key);
    });
    kb.appendChild(rowDiv);
  });
}

/* ---------- KEY COLOR ---------- */
function updateKeyColor(letter, color) {
  const priority = { green: 3, yellow: 2, gray: 1 };
  const current = keyColors[letter];
  if (!current || priority[color] > priority[current]) {
    keyColors[letter] = color;
    const el = document.getElementById("key-" + letter);
    if (el) { el.classList.remove("green", "yellow", "gray"); el.classList.add(color); }
  }
}

/* ---------- INPUT ---------- */
document.addEventListener("keydown", onKey);

function onKey(e) {
  if (gameOver || !gameMode || isSubmitting) return;
  if (e.key === "Enter") handle("ENTER");
  else if (e.key === "Backspace") handle("⌫");
  else if (/^[A-Za-z]$/.test(e.key)) handle(e.key.toUpperCase());
}

function handle(k) {
  if (gameOver || isSubmitting) return;
  if (k === "ENTER") submit();
  else if (k === "⌫") back();
  else add(k);
}

function add(ch) {
  if (col < WORD_LENGTH) { board[row][col].innerText = ch; col++; }
}

function back() {
  if (col > 0) { col--; board[row][col].innerText = ""; }
}

/* ---------- SUBMIT ---------- */
async function isValidWord(word) {
  if (!word || word.length !== WORD_LENGTH) return false;
  try {
    const response = await api(`/api/validate-word?word=${encodeURIComponent(word)}`);
    const data = await response.json();
    return data.valid;
  } catch (error) {
    return /^[A-Z]{5}$/i.test(word);
  }
}

async function submit() {
  if (col !== WORD_LENGTH || isSubmitting || !gameId) return;
  isSubmitting = true;

  const guess = board[row].map(t => t.innerText).join("");
  const valid = await isValidWord(guess);
  if (!valid) {
    showInvalidWord();
    isSubmitting = false;
    return;
  }

  const durationSeconds = gameStartedAt ? Math.floor((Date.now() - gameStartedAt) / 1000) : 0;

  const res = await api(`/api/games/${gameId}/guess`, {
    method: "POST",
    body: JSON.stringify({ guess, durationSeconds })
  });

  if (res.status === 400) {
    showInvalidWord();
    isSubmitting = false;
    return;
  }
  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    showToast(data.message || "Could not submit guess");
    isSubmitting = false;
    return;
  }

  const result = await res.json();
  const resultArr = result.result.split("");
  const animPromises = resultArr.map((r, i) => new Promise(resolve => {
    const tile = board[row][i];
    const letter = tile.innerText;
    const status = r === "G" ? "green" : r === "Y" ? "yellow" : "gray";

    setTimeout(() => {
      tile.style.transition = 'transform 0.22s ease-in';
      tile.style.transform = 'rotateX(90deg)';

      setTimeout(() => {
        tile.classList.add(status);
        updateKeyColor(letter, status);

        tile.style.transition = 'transform 0.22s ease-out';
        tile.style.transform = 'rotateX(0deg)';

        setTimeout(() => {
          tile.style.transition = '';
          tile.style.transform = '';
          resolve();
        }, 230);
      }, 230);
    }, i * 110);
  }));

  await Promise.all(animPromises);
  await new Promise(r => setTimeout(r, 120));

  row++;
  col = 0;

  if (result.status === 'WON') {
    handleWin(result.attemptsUsed);
  } else if (result.status === 'LOST') {
    handleLoss(result.attemptsUsed);
  } else {
    localStorage.setItem('gameInProgress', 'true');
    isSubmitting = false;
  }
}

/* ---------- SAVE COMPLETED BOARD (for display pages) ---------- */
function saveCompletedBoard(mode) {
  const today = getTodayString();
  const snapshot = { boardState: [], keyColors };
  for (let r = 0; r < MAX_ATTEMPTS; r++) {
    snapshot.boardState[r] = [];
    for (let c = 0; c < WORD_LENGTH; c++) {
      snapshot.boardState[r][c] = {
        text: board[r][c].innerText,
        classes: board[r][c].className
      };
    }
  }
  localStorage.setItem(`completedBoard_${getUsername()}_${mode}_${today}`, JSON.stringify(snapshot));
  localStorage.setItem('lossGameState', JSON.stringify(snapshot));
  localStorage.setItem('gameInProgress', 'false');
}

/* ---------- SHOW INVALID WORD ---------- */
function showInvalidWord() {
  const rowElement = board[row][0].parentElement;
  rowElement.classList.add('shake');
  showToast("Word not in list");
  setTimeout(() => rowElement.classList.remove('shake'), 500);
}

/* ---------- HANDLE WIN ---------- */
function handleWin(attempts) {
  const winMessage = WIN_MESSAGES[attempts - 1] || 'Genius';
  const winRowIdx = attempts - 1;

  for (let c = 0; c < WORD_LENGTH; c++) {
    const tile = board[winRowIdx][c];
    setTimeout(() => {
      tile.style.transition = 'transform 0.12s ease-out';
      tile.style.transform = 'translateY(-28px) scale(1.15)';
      setTimeout(() => {
        tile.style.transition = 'transform 0.1s ease-in';
        tile.style.transform = 'translateY(0) scale(1)';
        setTimeout(() => {
          tile.style.transition = '';
          tile.style.transform = '';
        }, 100);
      }, 150);
    }, c * 80);
  }

  const totalBounceMs = WORD_LENGTH * 80 + 300;
  setTimeout(() => showToast("🎉 " + winMessage + " 🎉"), totalBounceMs);

  clearGameState();
  setTimeout(() => { window.location.href = '/statistics'; }, totalBounceMs + 1600);
}

/* ---------- HANDLE LOSS ---------- */
function handleLoss(attempts) {
  const message = gameMode === 'sudden'
    ? "💀 Sudden Death!"
    : attempts >= MAX_ATTEMPTS ? "Game Over" : "Game Over";
  showToast(message);
  saveCompletedBoard(gameMode);
  clearGameState();
  setTimeout(() => { window.location.href = '/loss'; }, 1800);
}

/* ---------- GAME STATE (local display helpers only) ---------- */
function clearGameState() {
  localStorage.removeItem('gameInProgress');
}

/* ---------- TOAST ---------- */
function showToast(msg) {
  const toast = document.getElementById("toast");
  toast.innerText = msg;
  toast.style.display = 'block';
  toast.style.opacity = '1';
  toast.classList.add("show");
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.classList.remove("show");
    setTimeout(() => { toast.style.display = 'none'; }, 300);
  }, 2000);
}

/* ---------- MENU / MODAL HELPERS ---------- */
function toggleMenu() {
  const modal = document.getElementById('menuModal');
  modal.classList.toggle('active');
  updateMenuItems();
}

function updateMenuItems() {
  const user = getUsername();
  const role = localStorage.getItem('role');
  if (!user || !getToken()) return;

  const userInfo = document.getElementById('userInfoMenu');
  if (userInfo) {
    userInfo.style.display = user ? 'block' : 'none';
    if (user) document.getElementById('menuUsername').textContent = user;
  }
  const loginItem = document.getElementById('menuLogin');
  const statsItem = document.getElementById('menuStats');
  const logoutItem = document.getElementById('menuLogout');
  const adminItem = document.getElementById('menuAdmin');

  if (loginItem) loginItem.style.display = user ? 'none' : 'flex';
  if (statsItem) statsItem.style.display = user ? 'flex' : 'none';
  if (logoutItem) logoutItem.style.display = user ? 'flex' : 'none';
  if (adminItem) adminItem.style.display = (user && role === 'ADMIN') ? 'flex' : 'none';
}

function closeMenuOnBackdrop(event) {
  if (event.target.id === 'menuModal') toggleMenu();
}

function showHelp() {
  document.getElementById('helpModal').classList.add('active');
}
function closeHelp() {
  document.getElementById('helpModal').classList.remove('active');
}
function closeHelpOnBackdrop(event) {
  if (event.target.id === 'helpModal') closeHelp();
}

function goToStats() {
  if (getToken()) window.location.href = '/statistics';
  else { alert('Please login to view statistics!'); window.location.href = '/login'; }
}

function goToLeaderboard() {
  window.location.href = '/leaderboard';
}

function goToHints() {
  window.location.href = '/hints';
}

function openSettings() {
  alert('Settings coming soon!');
}