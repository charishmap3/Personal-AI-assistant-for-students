// mood-theme.js — show modal only when askMood=1 or when called explicitly
(function(){
  function applyTheme(mood){
    if(!mood) mood = 'neutral';
    document.documentElement.setAttribute('data-theme', mood);
    document.body.setAttribute('data-theme', mood);
    const e = document.querySelector('.mood-badge .emoji');
    if(e){
      switch(mood){
        case 'happy': e.textContent='😊'; break;
        case 'calm': e.textContent='😌'; break;
        case 'energetic': e.textContent='⚡'; break;
        case 'sad': e.textContent='😔'; break;
        default: e.textContent='🙂';
      }
    }
  }

  function persistMood(mood){
    try {
      const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
      const tokenMeta = document.querySelector('meta[name="_csrf"]');
      const headerMeta = document.querySelector('meta[name="_csrf_header"]');
      if(tokenMeta && headerMeta){
        headers[ headerMeta.getAttribute('content') ] = tokenMeta.getAttribute('content');
      }
      fetch('/user/mood', { method:'POST', headers: headers, body: new URLSearchParams({ mood: mood }) }).catch(()=>{});
    } catch(e){}
  }

  function showModal(){
    if(document.getElementById('mood-modal')) return;
    const modal = document.createElement('div');
    modal.id='mood-modal';
    modal.style.position='fixed'; modal.style.left='0'; modal.style.top='0'; modal.style.right='0'; modal.style.bottom='0';
    modal.style.display='flex'; modal.style.alignItems='center'; modal.style.justifyContent='center';
    modal.style.background='rgba(2,6,23,0.45)'; modal.style.zIndex=9999;
    modal.innerHTML=`
      <div style="background:#fff;padding:20px;border-radius:12px;max-width:520px;width:94%;text-align:center;box-shadow:0 18px 48px rgba(2,6,23,0.18)">
        <h3 style="margin:0 0 8px;color:#123c82">How was your day?</h3>
        <p style="color:#6b7280;margin:0 0 14px">Pick one and we'll set a theme for you.</p>
        <div style="display:flex;gap:10px;flex-wrap:wrap;justify-content:center">
          <button class="m-btn" data-mood="happy" style="padding:10px;border-radius:10px;background:#ffb74d;border:none;cursor:pointer">😊 Happy</button>
          <button class="m-btn" data-mood="calm" style="padding:10px;border-radius:10px;background:#2fb6c9;border:none;cursor:pointer">😌 Calm</button>
          <button class="m-btn" data-mood="energetic" style="padding:10px;border-radius:10px;background:#ff5c8a;border:none;cursor:pointer">⚡ Energetic</button>
          <button class="m-btn" data-mood="sad" style="padding:10px;border-radius:10px;background:#6c8bd6;border:none;cursor:pointer">😔 Sad</button>
          <button class="m-btn" data-mood="neutral" style="padding:10px;border-radius:10px;background:#9aa7c7;border:none;cursor:pointer">😐 Neutral</button>
        </div>
        <div style="margin-top:12px"><button id="m-skip" style="background:transparent;border:none;color:#6b7280;cursor:pointer">Skip</button></div>
      </div>
    `;
    document.body.appendChild(modal);
    modal.querySelectorAll('.m-btn').forEach(b=>{
      b.addEventListener('click', function(){
        const m = this.getAttribute('data-mood'); applyTheme(m); persistMood(m); modal.remove();
      });
    });
    document.getElementById('m-skip').addEventListener('click', ()=>{ applyTheme('neutral'); persistMood('neutral'); modal.remove(); });
  }

  document.addEventListener('DOMContentLoaded', ()=>{
    const serverTheme = document.body.getAttribute('data-theme') || document.documentElement.getAttribute('data-theme');
    if(serverTheme) applyTheme(serverTheme);

    // only show modal when ?askMood=1 is present — no auto-show after login
    const params = new URLSearchParams(window.location.search);
    if (params.get('askMood') === '1') {
      showModal();
      // clean the param so refresh won't re-open
      params.delete('askMood');
      history.replaceState(null, '', window.location.pathname + (params.toString() ? '?' + params.toString() : ''));
    }
  });

  window.showMoodPrompt = showModal; // expose function if you want to call it manually from console
})();
