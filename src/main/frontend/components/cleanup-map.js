import { t } from './localization.js';
let leafletPromise;

function loadLeaflet() {
  if (leafletPromise) return leafletPromise;
  leafletPromise = Promise.all([
    new Promise((resolve, reject) => {
      if (document.querySelector('link[data-toza-leaflet]')?.sheet) return resolve();
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.dataset.tozaLeaflet = '';
      link.href = '/vendor/leaflet/leaflet.css';
      link.onload = resolve;
      link.onerror = () => { link.remove(); reject(new Error('Map CSS unavailable')); };
      document.head.append(link);
    }),
    new Promise((resolve, reject) => {
      if (window.L) return resolve();
      const script = document.createElement('script');
      script.src = '/vendor/leaflet/leaflet.js';
      script.onload = resolve;
      script.onerror = () => { script.remove(); reject(new Error('Map unavailable')); };
      document.head.append(script);
    })
  ]).catch(error => { leafletPromise = undefined; throw error; });
  return leafletPromise;
}

class CleanupMap extends HTMLElement {
  set markers(value) { this._markers = value; this.renderMarkers(); }
  get markers() { return this._markers || '[]'; }

  async connectedCallback() {
    if (!this.onLocaleChange) {
      this.onLocaleChange = () => { if (this.map) this.renderMarkers(false); };
      window.addEventListener('toza-language-changed', this.onLocaleChange);
    }
    const version = this.version = (this.version || 0) + 1;
    window.tozaSetText(this, 'Xarita yuklanmoqda…');
    try {
      await loadLeaflet();
      if (!this.isConnected || version !== this.version) return;
      delete this.tozaTextSource;
      this.replaceChildren();
      this.canvas = document.createElement('div');
      this.canvas.className = 'eco-map-canvas';
      this.append(this.canvas);
      const L = window.L;
      this.map = L.map(this.canvas, { scrollWheelZoom: false }).setView([41.2995, 69.2401], 11);
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        maxZoom: 19
      }).addTo(this.map);
      this.layer = L.layerGroup().addTo(this.map);
      this.observer = new ResizeObserver(() => this.map?.invalidateSize());
      this.observer.observe(this);
      this.renderMarkers();
    } catch {
      if (!this.isConnected || version !== this.version) return;
      delete this.tozaTextSource;
      this.replaceChildren();
      const error = document.createElement('p');
      window.tozaSetText(error, "Xaritani yuklab bo'lmadi. Internet aloqasini tekshiring. ");
      const retry = document.createElement('button');
      window.tozaSetText(retry, 'Qayta urinish');
      retry.className = 'eco-button';
      retry.onclick = () => this.connectedCallback();
      this.append(error, retry);
    }
  }

  renderMarkers(fitBounds = true) {
    if (!this.layer) return;
    this.layer.clearLayers();
    let points;
    try { points = JSON.parse(this.markers); } catch { return; }
    const bounds = [];
    for (const point of points) {
      if (!Number.isFinite(point.lat) || !Number.isFinite(point.lng)) continue;
      bounds.push([point.lat, point.lng]);
      const popup = document.createElement('div');
      const title = document.createElement('strong');
      title.textContent = t('Tozalash uchun berilgan');
      const address = document.createElement('p');
      address.textContent = point.address || t('Manzil xaritada belgilangan');
      const link = document.createElement('a');
      link.href = '/user/map'; link.textContent = t('Xaritani ochish');
      popup.append(title, address, link);
      window.L.circleMarker([point.lat, point.lng], {
        radius: 10, color: '#fff', weight: 3, fillColor: '#719d31', fillOpacity: 1
      }).addTo(this.layer).bindPopup(popup);
    }
    if (fitBounds && bounds.length) this.map.fitBounds(bounds, { padding: [40, 40], maxZoom: 13 });
  }

  disconnectedCallback() {
    window.removeEventListener('toza-language-changed', this.onLocaleChange);
    this.onLocaleChange = null;
    this.version = (this.version || 0) + 1;
    this.observer?.disconnect();
    this.map?.remove();
    this.map = this.layer = undefined;
  }
}

if (!customElements.get('toza-cleanup-map')) customElements.define('toza-cleanup-map', CleanupMap);
