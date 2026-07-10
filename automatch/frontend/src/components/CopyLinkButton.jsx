import { useRef, useState } from 'react';

/** Copia la URL actual al portapapeles, con confirmación breve en el propio botón. */
export default function CopyLinkButton({ className = 'btn btn-ghost btn-sm' }) {
  const [copied, setCopied] = useState(false);
  const timer = useRef(null);

  async function copy() {
    const url = window.location.href;
    try {
      await navigator.clipboard.writeText(url);
    } catch {
      // Fallback para contextos sin Clipboard API (http, navegadores antiguos)
      const el = document.createElement('textarea');
      el.value = url;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
    }
    setCopied(true);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setCopied(false), 2000);
  }

  return (
    <button type="button" className={className} onClick={copy}>
      {copied ? '¡Enlace copiado!' : '🔗 Copiar enlace'}
    </button>
  );
}
