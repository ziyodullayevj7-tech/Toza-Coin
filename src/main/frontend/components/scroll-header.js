/** Reveal navigation on upward scrolling without sending scroll events to the server. */
class ScrollHeaderBehavior extends HTMLElement {
  connectedCallback() {
    this.header = this.parentElement;
    this.layout = this.closest('.eco-public-layout');
    this.previousY = Math.max(0, window.scrollY);
    this.directionStart = this.previousY;
    this.direction = 0;
    this.holdUntil = 0;
    this.hiddenHeader = false;
    this.onScroll = () => {
      if (!this.frame) this.frame = requestAnimationFrame(() => {
        this.frame = null;
        this.update();
      });
    };
    this.show = () => {
      this.hiddenHeader = false;
      this.holdUntil = performance.now() + 1200;
      this.header.classList.remove('eco-header-hidden');
    };
    this.onKey = event => { if (event.key === 'Tab') this.show(); };
    this.onClick = event => { if (event.target.closest('a')) this.show(); };
    window.addEventListener('scroll', this.onScroll, { passive: true });
    window.addEventListener('resize', this.onScroll, { passive: true });
    document.addEventListener('keydown', this.onKey, true);
    this.header.addEventListener('focusin', this.show);
    this.header.addEventListener('click', this.onClick);
    // Vaadin keeps the layout while replacing routes; reset the header on each route change.
    this.observer = new MutationObserver(() => {
      this.show();
      this.onScroll();
    });
    if (this.layout) this.observer.observe(this.layout, { attributes: true, attributeFilter: ['class'] });
    this.update();
  }

  update() {
    const y = Math.max(0, window.scrollY);
    const delta = y - this.previousY;
    const direction = Math.sign(delta);
    if (direction && direction !== this.direction) {
      this.directionStart = this.previousY;
      this.direction = direction;
    }
    const distance = Math.abs(y - this.directionStart);
    if (y <= 48 || this.header.querySelector(':focus-visible') || performance.now() < this.holdUntil) {
      this.hiddenHeader = false;
    } else if (direction > 0 && y > 160 && distance > 18) {
      this.hiddenHeader = true;
    } else if (direction < 0 && distance > 10) {
      this.hiddenHeader = false;
    }
    this.header.classList.toggle('eco-header-scrolled', y > 48);
    this.header.classList.toggle('eco-header-hidden', this.hiddenHeader);
    this.previousY = y;
  }

  disconnectedCallback() {
    window.removeEventListener('scroll', this.onScroll);
    window.removeEventListener('resize', this.onScroll);
    document.removeEventListener('keydown', this.onKey, true);
    this.header?.removeEventListener('focusin', this.show);
    this.header?.removeEventListener('click', this.onClick);
    this.observer?.disconnect();
    if (this.frame) cancelAnimationFrame(this.frame);
  }
}

if (!customElements.get('toza-scroll-header')) customElements.define('toza-scroll-header', ScrollHeaderBehavior);
