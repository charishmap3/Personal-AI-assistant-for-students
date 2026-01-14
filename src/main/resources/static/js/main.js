// main.js - guaranteed show mood prompt after login if askMood=1 in URL

function applyMood(mood){
  if (!mood) mood = 'neutral';
  document.documentElement.setAttribute('data-theme', mood);
  document.body.setAttribute('data-theme', mood);
}

function persistMoodToServer(mood){
  try {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    if (tokenMeta && headerMeta) {
      headers[headerMeta.getAttribute('content')] = tokenMeta.getAttribute('content');
    }
    fetch('/user/mood', {
      method: 'POST',
      headers: headers,
      body: new URLSearchParams({ mood: mood })
    }).catch(e => console.warn('Mood persist failed', e));
  } catch(e) {
    console.warn('persistMoodToServer error', e);
  }
}

function showMoodPrompt(){
  if (document.getElementById('mood-modal')) return;
  const modal = document.createElement('div');
  modal.id = 'mood-modal';
  modal.style.position = 'fixed';
  modal.style.left = '0';
  modal.style.top = '0';
  modal.style.right = '0';
  modal.style.bottom = '0';
  modal.style.display = 'flex';
  modal.style.alignItems = 'center';
  modal.style.justifyContent = 'center';
  modal.style.background = 'rgba(2,6,23,0.45)';
  modal.style.zIndex = 2000;
  modal.innerHTML = `
    <div style="background:#fff;padding:22px;border-radius:12px;max-width:480px;width:94%;text-align:center;box-shadow:0 18px 48px rgba(2,6,23,0.28)">
      <h3 style="margin:0 0 8px;color:#123c82">How was your day?</h3>
      <p style="color:#6b7280;margin:0 0 14px">Choose how you feel and we'll tune the theme for you.</p>
      <div style="display:flex;gap:10px;flex-wrap:wrap;justify-content:center">
        <button data-mood="happy" style="padding:10px 12px;border-radius:10px;background:#ffb74d;border:none;cursor:pointer">😊 Happy</button>
        <button data-mood="calm" style="padding:10px 12px;border-radius:10px;background:#2fb6c9;border:none;cursor:pointer">😌 Calm</button>
        <button data-mood="energetic" style="padding:10px 12px;border-radius:10px;background:#ff5c8a;border:none;cursor:pointer">⚡ Energetic</button>
        <button data-mood="sad" style="padding:10px 12px;border-radius:10px;background:#6c8bd6;border:none;cursor:pointer">😔 Sad</button>
        <button data-mood="neutral" style="padding:10px 12px;border-radius:10px;background:#9aa7c7;border:none;cursor:pointer">😐 Neutral</button>
      </div>
      <div style="margin-top:12px"><button id="mood-skip" style="background:transparent;border:none;color:#6b7280;cursor:pointer">Skip</button></div>
    </div>
  `;
  document.body.appendChild(modal);

  modal.querySelectorAll('button[data-mood]').forEach(btn => {
    btn.addEventListener('click', function(){
      const mood = this.getAttribute('data-mood');
      applyMood(mood);
      persistMoodToServer(mood);
      modal.remove();
    });
  });

  modal.querySelector('#mood-skip').addEventListener('click', function(){
    applyMood('neutral');
    persistMoodToServer('neutral');
    modal.remove();
  });
}

function getUrlParameter(name) {
  const params = new URLSearchParams(window.location.search);
  return params.get(name);
}

function removeQueryParam(param) {
  const url = new URL(window.location.href);
  url.searchParams.delete(param);
  // also remove 'success' param to keep URL clean if you want:
  url.searchParams.delete('success');
  history.replaceState(null, '', url.pathname + (url.search ? '?' + url.searchParams.toString() : ''));
}

document.addEventListener('DOMContentLoaded', function(){
  // Immediately apply theme from server if present
  const srvTheme = document.body.getAttribute('data-theme') || document.documentElement.getAttribute('data-theme');
  if (srvTheme) applyMood(srvTheme);

  // If URL includes askMood=1 (we force it from login redirect), show the modal immediately
  const askMood = getUrlParameter('askMood');
  if (askMood === '1') {
    // show right away, small timeout for paint
    setTimeout(() => { showMoodPrompt(); removeQueryParam('askMood'); }, 250);
    return;
  }

  // fallback: only prompt once per session if theme neutral
  if (!sessionStorage.getItem('moodAsked')) {
    const currentTheme = document.body.getAttribute('data-theme') || document.documentElement.getAttribute('data-theme');
    if (!currentTheme || currentTheme === 'neutral') {
      setTimeout(() => { showMoodPrompt(); sessionStorage.setItem('moodAsked','1'); }, 900);
    }
  }
});
