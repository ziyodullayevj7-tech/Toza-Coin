// Bridge for Leaflet and camera elements rendered by JavaScript rather than Flow components.
export function t(source) {
  const messages = window.tozaMessages || {};
  if (Object.hasOwn(messages, source)) return messages[source];
  const match = /^([\s\p{So}\p{Sk}\p{Sm}\p{Mn}]*)(.*?)([\s\p{So}\p{Sk}\p{Sm}\p{Mn}]*)$/su.exec(source);
  if (match && Object.hasOwn(messages, match[2])) return match[1] + messages[match[2]] + match[3];
  return source;
}
const nodes = new Set();
window.tozaTranslate = t;
window.tozaSetText = (node, source) => {
  node.tozaTextSource = source;
  nodes.add(new WeakRef(node));
  node.textContent = t(source);
};
window.addEventListener('toza-language-changed', () => {
  for (const reference of nodes) {
    const node = reference.deref();
    if (!node?.isConnected || node.tozaTextSource === undefined) nodes.delete(reference);
    else if (node.tozaTextSource !== undefined) node.textContent = t(node.tozaTextSource);
  }
});
