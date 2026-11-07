import {t} from './localization.js';

class JungleBackground extends HTMLElement {
    connectedCallback() {
        if (this.scene) return;
        this.scene = document.createElement('div');
        this.scene.className = 'user-jungle-scene';
        this.scene.setAttribute('aria-hidden', 'true');
        this.video = document.createElement('video');
        this.video.className = 'user-jungle-video';
        this.video.muted = true;
        this.video.defaultMuted = true;
        this.video.loop = true;
        this.video.playsInline = true;
        this.video.preload = 'none';
        this.scene.append(this.video);
        document.body.append(this.scene);
        this.button = document.createElement('button');
        this.button.type = 'button';
        this.button.className = 'jungle-motion-toggle';
        this.append(this.button);
        this.reduced = matchMedia('(prefers-reduced-motion: reduce)');
        this.connection = navigator.connection;
        try {
            this.userPaused = localStorage.getItem('toza-background-paused') === 'true';
        } catch {
            this.userPaused = false;
        }
        this.abort = new AbortController();
        const options = {signal: this.abort.signal};
        this.button.addEventListener('click', () => {
            this.userPaused = !this.userPaused;
            try {
                localStorage.setItem('toza-background-paused', String(this.userPaused));
            } catch {
            }
            this.sync();
        }, options);
        this.reduced.addEventListener('change', () => this.sync(), options);
        this.connection?.addEventListener('change', () => this.sync(), options);
        document.addEventListener('visibilitychange', () => this.sync(), options);
        window.addEventListener('toza-language-changed', () => this.sync(), options);
        this.video.addEventListener('playing', () => this.scene?.classList.add('is-playing'), options);
        this.video.addEventListener('error', () => {
            this.failed = true;
            this.scene?.classList.remove('is-playing');
            this.sync();
        }, options);
        this.sync();
    }

    sync() {
        const restricted = this.reduced.matches || this.connection?.saveData || this.failed;
        const paused = restricted || this.userPaused || document.hidden;
        this.scene.dataset.paused = String(paused);
        this.button.disabled = !!restricted;
        const label = restricted ? 'Harakat kamaytirilgan' : this.userPaused
            ? "Fon harakatini yoqish" : "Fon harakatini to'xtatish";
        this.button.textContent = t(label);
        this.button.setAttribute('aria-label', t(label));
        this.button.setAttribute('aria-pressed', String(!this.userPaused && !restricted));
        this.button.dataset.playing = String(!paused);
        if (paused) this.video.pause();
        else {
            if (!this.video.getAttribute('src')) this.video.src = new URL('media/jungle-loop.mp4', document.baseURI).href;
            this.video.play().catch(() => {
                // Autoplay restrictions leave the still image and an explicit play control.
                if (!this.isConnected || document.hidden || this.reduced.matches) return;
                this.userPaused = true;
                this.sync();
            });
        }
    }

    disconnectedCallback() {
        this.abort?.abort();
        if (this.video) {
            this.video.pause();
            this.video.removeAttribute('src');
            this.video.load();
        }
        this.scene?.remove();
        this.scene = null;
        this.replaceChildren();
    }
}

customElements.define('toza-jungle-background', JungleBackground);
